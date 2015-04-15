#
# Copyright 2015 Delft University of Technology
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

from __future__ import division, print_function

import argparse
import sys
import os

import graphlab as gl
import graphlab.deploy.environment
import time
# import operator


__author__ = 'Jorai Rijsdijk'


def create_environment(hadoop_home, memory_mb, virtual_cores):
    """
    Create a (distributed) Hadoop environment with the given hadoop_home, memory_mb and virtual_cores

    :param hadoop_home: The location of the hadoop_home to get the hadoop config files from hadoop_home/etc/hadoop
    :param memory_mb: The amount of memory to use for processing the algorithm
    :param virtual_cores: The amount of virtual cores to use for graph processing
    :return: The created Hadoop environment object
    """
    return gl.deploy.environment.Hadoop('Hadoop', config_dir=hadoop_home + '/etc/hadoop', memory_mb=memory_mb,
                                        virtual_cores=virtual_cores, gl_source=None)


def parse_args(description, algorithm_name_short, **positional_args):
    """
    Parse the arguments of an algorithm, adding positional arguments specific to the algorithm.

    :param description: The description of the algorithm script
    :param algorithm_name_short: The short name of the algorithm (for graph output filename use)
    :param positional_args: Zero or more keyword arguments, where the keyword is the argument name,
                            and the value is a struct with the keys: type and help to indicate the respective
                            arguments of ArgumentParser.add_argument()
    :return: The result of ArgumentParser.parse_args()
    """
    parser = argparse.ArgumentParser(description=description)
    parser.add_argument('-t', '--target', default='local', choices=['local', 'hadoop'], required=True,
                        help='Whether to use hadoop or local execution/file storage')
    parser.add_argument('-C', '--cores', default=2, metavar='n', required=False,
                        help='Amount of virtual cores to use, must be at least 2. '
                             'Only used if target is hadoop (Default: 2)')
    parser.add_argument('-H', '--heap-size', default=4096, metavar='n', required=False,
                        help='Amount of memory in MB required for job execution. '
                             'Only used if target is hadoop (Default: 4096)')
    parser.add_argument('--save-result', action='store_true', required=False,
                        help='Save the result graph. Result stored in: target/%s_<graph_file>' % algorithm_name_short)

    parser.add_argument('-f', '--graph-file', metavar='file', required=True, help='The graph file to use')
    parser.add_argument('-d', '--directed', type=bool, required=True,
                        help='Whether or not the input graph is directed.')
    parser.add_argument('-e', '--edge-based', type=bool, required=True,
                        help='Whether or not the input graph is edge based or vertex based.')

    for arg_key in positional_args:
        parser.add_argument(arg_key, type=positional_args[arg_key]['type'], help=positional_args[arg_key]['help'])

    return parser.parse_args()


def save_graph(graph, algorithm_name_short, graph_file):
    """
    Save an SGraph to a file for the testing framework to use.

    :param graph: The graph to save
    :param algorithm_name_short: The short name of the algorithm (used for the filename)
    :param graph_file: The original graph input file path
    """
    graph.save('target/%s_%s' % (algorithm_name_short, graph_file[graph_file.rfind('/', 0, len(graph_file) - 2) + 1:]))


def load_graph_task(task):
    graph_data = gl.SFrame.read_csv(task.params['csv'], header=False, delimiter=' ', column_type_hints=long)
    graph = gl.SGraph().add_edges(graph_data, src_field='X1', dst_field='X2')
    if not task.params['directed']:
        graph.add_edges(graph_data, src_field='X2', dst_field='X1')

    task.outputs['graph'] = graph


def handle_edge(vertex, neighbor):
    c_label = neighbor['old_label']
    labels = vertex['surrounding_labels']

    # Add the weighted_score of dst to surrounding labels
    # If there's no entry yet, add it
    if c_label not in labels:
        labels[c_label] = [neighbor['weighted_score'], neighbor['old_score']]
    else:  # Else, add dst['weighted_score'] and insert max(old_max_score, dst['old_score'])
        current = labels[c_label]
        labels[c_label] = [current[0] + neighbor['weighted_score'],
                           max(current[1], neighbor['old_score'])]

    # If there's no best candidate yet (if it's the first edge added), set it to dst['old_label']
    if 'best_candidate' not in labels:
        labels['best_candidate'] = c_label
    else:  # If we already have a best candidate
        # If, by adding this edge, dst['old_label'] surpassed the best label, update it
        if labels[labels['best_candidate']][0] < labels[c_label][0]:
            labels['best_candidate'] = c_label

    # Increment the amount of edges processed for this vertex
    labels['edges_processed'] += 1

    # If this is the last edge, choose a new label
    if labels['edges_processed'] == vertex['edges']:
        vertex['label'] = labels['best_candidate']
        vertex['score'] = labels[vertex['label']][1] - (
            vertex['hop_attenuation'] if vertex['label'] != vertex['old_label'] else 0)

    vertex['surrounding_labels'] = labels
    return vertex


def community_detection_propagate(src, edge, dst):
    handle_edge(src, dst)
    handle_edge(dst, src)

    return src, edge, dst


def count_edges(src, edge, dst):
    src['edges'] += 1
    dst['edges'] += 1
    return src, edge, dst


def community_detection_model(task):
    max_iterations = task.params['max_iterations']
    node_preference = task.params['node_preference']
    graph = task.inputs['data']
    graph.vertices['label'] = graph.vertices['__id']
    graph.vertices['edges'] = 0
    graph.vertices['score'] = 1.0
    graph.vertices['hop_attenuation'] = task.params['hop_attenuation']

    print('Counting edges per vertex')
    graph = graph.triple_apply(count_edges, mutated_fields=['edges'])

    iteration = 0
    while iteration < max_iterations:
        print('Start iteration %d' % (iteration + 1))
        # Backup label and score to ensure the current label and score get used for propagation
        graph.vertices['old_label'] = graph.vertices['label']
        graph.vertices['old_score'] = graph.vertices['score']
        # Calculate the weighted score of each vertex
        graph.vertices['weighted_score'] = graph.vertices.apply(
            lambda vertex: vertex['score'] * (vertex['edges'] ** node_preference))
        # Initialise the dictionary of surrounding labels with an empty one (except for the processed count)
        graph.vertices['surrounding_labels'] = graph.vertices.apply(lambda _: {'edges_processed': 0})

        # Apply the actual propagation step
        graph = graph.triple_apply(community_detection_propagate,
                                   mutated_fields=['label', 'score', 'surrounding_labels'])

        iteration += 1

    task.outputs['cd_graph'] = graph


def main():
    # Parse arguments
    args = parse_args('Run the Community Detection algorithm on a graph using GraphLab Create.', 'cd',
                      node_preference={'type': float,
                                       'help': 'How much to prefer labels from nodes with more neighbors.'},
                      hop_attenuation={'type': float, 'help': 'The score penalty when switching a community.'},
                      max_iterations={'type': int, 'help': 'How many iterations to run the community detection.'})
    use_hadoop = args.target == 'hadoop'

    if not args.edge_based:
        print("Vertex based graph format not supported yet", file=sys.stderr)
        exit(2)

    if use_hadoop:  # Deployed execution
        hadoop_home = os.environ.get('HADOOP_HOME')
        hadoop = create_environment(hadoop_home=hadoop_home, memory_mb=args.heap_size, virtual_cores=args.virtual_cores)

        # Define the graph loading task
        load_graph = gl.deploy.Task('load_graph')
        load_graph.set_params({'csv': args.graph_file, 'directed': args.directed})
        load_graph.set_code(load_graph_task)
        load_graph.set_outputs(['graph'])

        # Define the shortest_path model create task
        community = gl.deploy.Task('community_detection')
        community.set_params(
            {'node_preference': args.node_preference, 'hop_attenuation': args.hop_attenuation,
             'max_iterations': args.max_iterations})
        community.set_inputs({'data': ('load_graph', 'graph')})
        community.set_code(community_detection_model)
        community.set_outputs(['cd_graph'])

        # Create the job and deploy it to the Hadoop cluster
        hadoop_job = gl.deploy.job.create(['load_graph', 'community_detection'], environment=hadoop)
        while hadoop_job.get_status() in ['Pending', 'Running']:
            time.sleep(2)  # sleep for 2s while polling for job to be complete.

        output_graph = community.outputs['cd_graph']
    else:  # Local execution
        # Stub task class
        class Task:
            def __init__(self, **keywords):
                self.__dict__.update(keywords)

        # Stub task object to keep function definitions intact
        cur_task = Task(params={'csv': args.graph_file, 'directed': args.directed,
                                'node_preference': args.node_preference, 'hop_attenuation': args.hop_attenuation,
                                'max_iterations': args.max_iterations}, inputs={}, outputs={})

        load_graph_task(cur_task)
        cur_task.inputs['data'] = cur_task.outputs['graph']
        community_detection_model(cur_task)
        output_graph = cur_task.outputs['cd_graph']

    if args.save_result:
        save_graph(output_graph, 'cd', args.graph_file)

if __name__ == "__main__":
    main()