import sys
import os

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


if len(sys.argv) < 6:
    print >> sys.stderr, "Too few arguments, need at least 5: <virtual_cores> <heap_size> <graph_file> <directed> <edge_based>"
    exit(1)

# Read arguments
virtual_cores = sys.argv[1]
heap_size = sys.argv[2]
graph_file = sys.argv[3]
directed = sys.argv[4] == "true"
edge_based = sys.argv[5] == "true"

# Create hadoop environment object
hadoop_home = os.environ.get('HADOOP_HOME')
# TODO Switch back to using hadoop deployment when distributed execution is possible.
# hadoop = create_environment(hadoop_home=hadoop_home, memory_mb=heap_size, virtual_cores=virtual_cores)

if not edge_based:
    print >> sys.stderr, "Vertex based graph format not supported yet"
    exit(2)


def load_graph_task(task):
    import graphlab as gl_

    graph_data = gl_.SFrame.read_csv(task.params['csv'], header=False, delimiter=' ', column_type_hints=long)
    task.outputs['graph'] = gl.SGraph().add_edges(graph_data, src_field='X1', dst_field='X2')


def local_clustering_coefficient_model(task):
    graph = task.inputs['data']

    def local_clustering_coefficient(node):
        node_id = node['__id']
        # Get all vertices connected to the node, ignoring edge directions if any
        local_graph = graph.get_neighborhood(ids=[node_id], radius=1, full_subgraph=True)
        # Get the amount of vertices in the neighborhood of the collection node
        num_vertices = local_graph.summary()['num_vertices']

        # Filter all edges in the edge list that either originate from or go to the node
        # This way, we only keep edges between neighbors of the node so we can count them
        num_neighbor_edges = local_graph.get_edges() \
            .filter_by([node_id], '__src_id', exclude=True) \
            .filter_by([node_id], '__dst_id', exclude=True) \
            .num_rows()

        # Calculate and return the local clustering coefficient based on the formula:
        # C_i = (1 if directed, 2 if undirected) * num_neighbor_edges / num_possible_neighbor_edges
        return (1.0 if task.params['directed'] else 2.0) * num_neighbor_edges / (num_vertices * (num_vertices - 1))

    # Lambda usage of graph.vertices['__id'].apply impossible, since graph.get_neighborhood is needed in the function
    # If called in that (generally better way), it raises a pickle.PicklingError
    output = {}
    for cur_node in graph.vertices:
        output[cur_node['__id']] = local_clustering_coefficient(cur_node)

    # Since we couldn't use graph.vertices['__id'].apply to calculate the lcc value, we still need to change
    # the vertex fields to link the values to the graph
    graph.vertices['local_clustering_coefficient'] = graph.vertices['__id'].apply(lambda node_id: output[node_id] if node_id in output else 0.0)
    task.outputs['lcc_graph'] = graph


# Define the graph loading task
# TODO Switch back to using code hadoop deployment when distributed execution is possible. Note: files are still located on the HDFS.
# load_graph = gl.deploy.Task('load_graph')
# load_graph.set_params({'csv': graph_file, 'directed': directed})
# load_graph.set_code(load_graph_task)
# load_graph.set_outputs(['graph'])
#
# # Define the shortest_path model create task
# shortest = gl.deploy.Task('local_clustering_coefficient')
# shortest.set_params({'directed': directed})
# shortest.set_inputs({'data': ('load_graph', 'graph')})
# shortest.set_code(local_clustering_coefficient_model)
# shortest.set_outputs(['lcc_graph'])
#
# # Create the job and deploy it to the Hadoop cluster
# hadoop_job = gl.deploy.job.create(['load_graph', 'local_clustering_coefficient'], environment=hadoop)
# while hadoop_job.get_status() in ['Pending', 'Running']:
# time.sleep(2)  # sleep for 2s while polling for job to be complete.
#
# print hadoop_job


# Local implementation as distributed is not yet possible, so hadoop is only an extra amount of overhead
# Remove everything from these comments on when switching back to deployment execution

# Stub task class
class Task:
    def __init__(self, **keywords):
        self.__dict__.update(keywords)

# Stub task object to keep function definitions intact
cur_task = Task(params={'csv': graph_file, 'directed': directed}, inputs={},
                outputs={})

load_graph_task(cur_task)
cur_task.inputs['data'] = cur_task.outputs['graph']
local_clustering_coefficient_model(cur_task)
output_graph = cur_task.outputs['lcc_graph']