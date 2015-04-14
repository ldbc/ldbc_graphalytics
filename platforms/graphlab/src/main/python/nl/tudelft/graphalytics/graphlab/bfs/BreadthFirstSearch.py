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


def shortest_path_model(task):
    graph = task.inputs['data']
    task.outputs['sp_graph'] = gl.shortest_path.create(graph, source_vid=task.params['source_vertex'])


def main():
    # Parse arguments
    args = parse_args('Run the BreadthFirstSearch algorithm on a graph using GraphLab Create.', 'bfs',
                      source_vertex={'type': long, 'help': 'The source vertex to start the BFS search from.'})
    use_hadoop = args.target == 'hadoop'

    if not args.edge_based:
        print("Vertex based graph format not supported yet", file=sys.stderr)
        exit(2)

    if not args.directed:
        print("Undirected graph format is not supported yet", file=sys.stderr)
        exit(3)

    if use_hadoop:  # Deployed execution
        # Create hadoop environment object
        hadoop_home = os.environ.get('HADOOP_HOME')
        hadoop = create_environment(hadoop_home=hadoop_home, memory_mb=args.heap_size, virtual_cores=args.virtual_cores)

        # Define the graph loading task
        load_graph = gl.deploy.Task('load_graph')
        load_graph.set_params({'csv': args.graph_file})
        load_graph.set_code(load_graph_task)
        load_graph.set_outputs(['graph'])

        # Define the shortest_path model create task
        shortest = gl.deploy.Task('shortest_path')
        shortest.set_params({'source_vertex': args.source_vertex})
        shortest.set_inputs({'data': ('load_graph', 'graph')})
        shortest.set_code(shortest_path_model)
        shortest.set_outputs(['sp_graph'])

        # Create the job and deploy it to the Hadoop cluster
        hadoop_job = gl.deploy.job.create(['load_graph', 'shortest_path'], environment=hadoop)
        while hadoop_job.get_status() in ['Pending', 'Running']:
            time.sleep(2)  # sleep for 2s while polling for job to be complete.

        output_graph = shortest.outputs['sp_graph'].get('graph')

    else:  # Local execution
        # Stub task class
        class Task:
            def __init__(self, **keywords):
                self.__dict__.update(keywords)

        # Stub task object to keep function definitions intact
        cur_task = Task(params={'csv': args.graph_file, 'source_vertex': args.source_vertex}, inputs={}, outputs={})

        load_graph_task(cur_task)
        cur_task.inputs['data'] = cur_task.outputs['graph']
        shortest_path_model(cur_task)
        output_graph = cur_task.outputs['sp_graph'].get('graph')

    if args.save_result:
        save_graph(output_graph, 'bfs', args.graph_file)


if __name__ == '__main__':
    main()
