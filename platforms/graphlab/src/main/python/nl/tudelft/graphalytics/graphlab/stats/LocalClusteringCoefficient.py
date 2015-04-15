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
import time

import graphlab as gl

import graphlab.deploy.environment


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
    task.outputs['graph'] = gl.SGraph().add_edges(graph_data, src_field='X1', dst_field='X2')


def local_clustering_coefficient(node, graph):
    node_id = node['__id']
    # Get all vertices connected to the node, ignoring edge directions if any
    local_graph = graph.get_neighborhood(ids=[node_id], radius=1, full_subgraph=True)
    # Get the amount of vertices in the neighborhood of the collection node
    num_neighbors = local_graph.summary()['num_vertices'] - 1

    if num_neighbors <= 1:
        return 0.0

    # Filter all edges in the edge list that either originate from or go to the node
    # This way, we only keep edges between neighbors of the node so we can count them
    tmp_edges = local_graph.get_edges().filter_by([node_id], '__src_id', exclude=True)
    num_neighbor_edges = 0 if tmp_edges.num_rows() == 0 else tmp_edges.filter_by([node_id], '__dst_id',
                                                                                 exclude=True).num_rows()

    # Calculate and return the local clustering coefficient based on the formula:
    # C_i = (1 if directed, 2 if undirected) * num_neighbor_edges / num_possible_neighbor_edges
    # The difference between directed/undirected is already taken care of in the input format,
    # So an undirected edge is already listed as 2 directed edges.
    return num_neighbor_edges / (num_neighbors * (num_neighbors - 1))


def local_clustering_coefficient_model(task):
    graph = task.inputs['data']

    # Lambda usage of graph.vertices['__id'].apply impossible, since graph.get_neighborhood is needed in the function
    # If called in that (generally better way), it raises a pickle.PicklingError
    num_nodes = 0
    lcc_sum = 0
    output = {}
    for cur_node in graph.vertices:
        lcc = local_clustering_coefficient(cur_node, graph)
        num_nodes += 1
        lcc_sum += lcc
        output[cur_node['__id']] = lcc

    # Since we couldn't use graph.vertices['__id'].apply to calculate the lcc value, we still need to change
    # the vertex fields to link the values to the graph
    graph.vertices['local_clustering_coefficient'] = graph.vertices['__id'].apply(
        lambda node_id: output[node_id] if node_id in output else 0.0)
    graph.vertices['average_clustering_coefficient'] = lcc_sum / num_nodes
    task.outputs['lcc_graph'] = graph


def main():
    # Parse arguments
    args = parse_args('Run the Connected Components algorithm on a graph using GraphLab Create', 'conn')
    use_hadoop = args.target == "hadoop"

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
        local_cc = gl.deploy.Task('local_clustering_coefficient')
        local_cc.set_params({'directed': args.directed})
        local_cc.set_inputs({'data': ('load_graph', 'graph')})
        local_cc.set_code(local_clustering_coefficient_model)
        local_cc.set_outputs(['lcc_graph'])

        # Create the job and deploy it to the Hadoop cluster
        hadoop_job = gl.deploy.job.create(['load_graph', 'local_clustering_coefficient'], environment=hadoop)
        while hadoop_job.get_status() in ['Pending', 'Running']:
            time.sleep(2)  # sleep for 2s while polling for job to be complete.

        output_graph = local_cc.outputs['lcc_graph']
    else:  # Local execution
        # Stub task class
        class Task:
            def __init__(self, **keywords):
                self.__dict__.update(keywords)

        # Stub task object to keep function definitions intact
        cur_task = Task(params={'csv': args.graph_file, 'directed': args.directed}, inputs={},
                        outputs={})

        load_graph_task(cur_task)
        cur_task.inputs['data'] = cur_task.outputs['graph']
        local_clustering_coefficient_model(cur_task)
        output_graph = cur_task.outputs['lcc_graph']

    if args.save_result:
        save_graph(output_graph, 'stats', args.graph_file)

if __name__ == '__main__':
    main()