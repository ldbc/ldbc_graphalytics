import sys
import os
import time

import graphlab as gl
from graphlab.deploy.environment import Hadoop


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


if len(sys.argv) < 7:
    print >> sys.stderr, "Too few arguments, need at least 6: <virtual_cores> <heap_size> <graph_file> <directed> <edge_based> <source_vertex>"
    exit(1)

# Read arguments
virtual_cores = sys.argv[1]
heap_size = sys.argv[2]
graph_file = sys.argv[3]
directed = sys.argv[4] == "true"
edge_based = sys.argv[5] == "true"
source_vertex = sys.argv[6]

# Create hadoop environment object
hadoop_home = os.environ.get('HADOOP_HOME')
hadoop = create_environment(hadoop_home=hadoop_home, memory_mb=heap_size, virtual_cores=virtual_cores)

if not edge_based:
    print >> sys.stderr, "Vertex based graph format not supported yet"
    exit(2)

if not directed:
    print >> sys.stderr, "Undirected graph format is not supported yet"
    exit(3)


def load_graph_task(task):
    import graphlab as gl_

    graph_data = gl_.SFrame.read_csv(task.params['csv'], header=False, delimiter=' ')
    task.outputs['graph'] = gl.SGraph().add_edges(graph_data, src_field='X1', dst_field='X2')


def shortest_path_model(task):
    import graphlab as gl_

    graph = task.inputs['data']
    task.outputs['sp_graph'] = gl_.shortest_path.create(graph, source_vid=task.params['source_vertex'])


# Define the graph loading task
load_graph = gl.deploy.Task('load_graph')
load_graph.set_params({'csv': graph_file})
load_graph.set_code(load_graph_task)
load_graph.set_outputs(['graph'])

# Define the shortest_path model create task
shortest = gl.deploy.Task('shortest_path')
shortest.set_params({'source_vertex': source_vertex})
shortest.set_inputs({'data': ('load_graph', 'graph')})
shortest.set_code(shortest_path_model)
shortest.set_outputs(['sp_graph'])

# Create the job and deploy it to the Hadoop cluster
hadoop_job = gl.deploy.job.create(['load_graph', 'shortest_path'], environment=hadoop)
while hadoop_job.get_status() in ['Pending', 'Running']:
    time.sleep(2)  # sleep for 2s while polling for job to be complete.

print hadoop_job
