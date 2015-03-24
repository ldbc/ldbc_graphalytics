import sys
import os

import graphlab as gl
from graphlab.deploy.environment import Hadoop
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


if len(sys.argv) < 8:
    print >> sys.stderr, "Too few arguments, need at least 7: <use_hadoop> [virtual_cores] [heap_size] <graph_file> <directed> <edge_based> <node_preference> <hop_attenuation> <max_iterations> [save_result_graph]"
    exit(1)

# Read arguments
use_hadoop = sys.argv[1] == "true"
save_result_graph = False

if use_hadoop:
    if len(sys.argv) < 10:
        print >> sys.stderr, "Too few arguments for use_hadoop=true, need at least 9: <use_hadoop=true> <virtual_cores> <heap_size> <graph_file> <directed> <edge_based> <node_preference> <hop_attenuation> <max_iterations> [save_result_graph]"
        exit(1)
    else:
        virtual_cores = sys.argv[2]
        heap_size = sys.argv[3]
        graph_file = sys.argv[4]
        directed = sys.argv[5] == "true"
        edge_based = sys.argv[6] == "true"
        node_preference = float(sys.argv[7])
        hop_attenuation = float(sys.argv[8])
        max_iterations = int(sys.argv[9])
        # Create hadoop environment object
        hadoop_home = os.environ.get('HADOOP_HOME')
        hadoop = create_environment(hadoop_home=hadoop_home, memory_mb=heap_size, virtual_cores=virtual_cores)
        if len(sys.argv) >= 11:
            save_result_graph = sys.argv[10] == "true"
else:
    graph_file = sys.argv[2]
    directed = sys.argv[3] == "true"
    edge_based = sys.argv[4] == "true"
    node_preference = float(sys.argv[5])
    hop_attenuation = float(sys.argv[6])
    max_iterations = int(sys.argv[7])
    if len(sys.argv) >= 9:
        save_result_graph = sys.argv[8] == "true"

if not edge_based:
    print >> sys.stderr, "Vertex based graph format not supported yet"
    exit(2)


def load_graph_task(task):
    import graphlab as gl_

    graph_data = gl_.SFrame.read_csv(task.params['csv'], header=False, delimiter=' ', column_type_hints=long)
    graph = gl.SGraph().add_edges(graph_data, src_field='X1', dst_field='X2')
    if not task.params['directed']:
        graph.add_edges(graph_data, src_field='X2', dst_field='X1')

    task.outputs['graph'] = graph


def community_detection_model(task):
    # def setup_edgelists(graph):
    # verts_in = graph.vertices.join(graph.edges, on={'__id': '__dst_id'}, how='left')
    # verts_in = verts_in.groupby('__id', {'neighbors_in': gl_.aggregate.CONCAT('__src_id')})
    #
    # verts_out = graph.vertices.join(graph.edges, on={'__id': '__src_id'}, how='left')
    #     verts_out = verts_out.groupby('__id', {'neighbors_out': gl_.aggregate.CONCAT('__dst_id')})
    #
    #     return verts_in.join(verts_out, on='__id', how='inner')
    #
    # def get_vertex(data_sframe, id):
    #     return data_sframe[(data_sframe['__id'] == id)]
    #
    # def community_detection_propagate(row, data_sframe):
    #     def update_scoreboard(scoreboard, neighbor):
    #         if not neighbor['label'] in scoreboard:
    #             scoreboard[neighbor['label']] = 0
    #         scoreboard[neighbor['label']] += neighbor['weighted_score']
    #         return scoreboard
    #
    #     scoreboard = dict()
    #     for neighbor_in in data_sframe.filter_by(row['neighbors_in'], '__id'):
    #         scoreboard = update_scoreboard(scoreboard, neighbor_in)
    #
    #     for neighbor_out in data_sframe.filter_by(row['neighbors_out'], '__id'):
    #         scoreboard = update_scoreboard(scoreboard, neighbor_out)
    #
    #     highest_score = max(scoreboard.iteritems(), key=operator.itemgetter(1))
    #     print "ID: %d | OLD_LABEL: %d" % (row['__id'], row['label'])
    #     print "NEW_LABEL: %d | NEW_SCORE: %f" % (highest_score[0], highest_score[1])
    #     print "TLabel: %s | TScore: %s" % (type(highest_score[0]), type(highest_score[1]))
    #
    #     return {'label': row['label'], 'score': row['score']}
    #
    # graph = task.inputs['data']
    #
    # data_sframe = setup_edgelists(graph)
    #
    # # Count the amount of edges per vertex
    # print "Calculating edge counts per vertex"
    # data_sframe['edges'] = data_sframe.apply(lambda row: len(row['neighbors_in']) + len(row['neighbors_out']))
    #
    # # Start with your own label
    # data_sframe['label'] = data_sframe['__id']
    # # Start with score = 1.0
    # data_sframe['score'] = 1.0
    #
    # iteration = 0
    # while iteration < max_iterations:
    #     print 'Start iteration %d' % (iteration + 1)
    #
    #     # Calculate the weighted score to consider
    #     data_sframe['weighted_score'] = data_sframe.apply(lambda row: row['score'] * (row['edges'] ** node_preference))
    #
    #     data_sframe.print_rows(num_rows=20)
    #
    #     data_sframe['tmp'] = data_sframe.apply(lambda row: community_detection_propagate(row, data_sframe))
    #     data_sframe['label'] = data_sframe.apply(lambda row: row['tmp']['label'])
    #     data_sframe['score'] = data_sframe.apply(lambda row: row['tmp']['score'])
    #
    #     # print data_sframe
    #     data_sframe.print_rows(num_rows=20)
    #     iteration += 1
    def count_edges(src, edge, dst):
        src['edges'] += 1
        dst['edges'] += 1
        return src, edge, dst

    def community_detection_propagate(src, edge, dst):
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
                    hop_attenuation if vertex['label'] != vertex['old_label'] else 0)

            vertex['surrounding_labels'] = labels
            return vertex

        handle_edge(src, dst)
        handle_edge(dst, src)

        return src, edge, dst

    graph = task.inputs['data']
    graph.vertices['label'] = graph.vertices['__id']
    graph.vertices['edges'] = 0
    graph.vertices['score'] = 1.0

    print 'Counting edges per vertex'
    graph = graph.triple_apply(count_edges, mutated_fields=['edges'])

    iteration = 0
    while iteration < max_iterations:
        print 'Start iteration %d' % (iteration + 1)
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


if use_hadoop:  # Deployed execution
    # Define the graph loading task
    load_graph = gl.deploy.Task('load_graph')
    load_graph.set_params({'csv': graph_file, 'directed': directed})
    load_graph.set_code(load_graph_task)
    load_graph.set_outputs(['graph'])

    # Define the shortest_path model create task
    community = gl.deploy.Task('community_detection')
    community.set_params(
        {'node_preference': node_preference, 'hop_attenuation': hop_attenuation, 'max_iterations': max_iterations})
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
    cur_task = Task(params={'csv': graph_file, 'directed': directed, 'node_preference': node_preference,
                            'hop_attenuation': hop_attenuation,
                            'max_iterations': max_iterations}, inputs={}, outputs={})

    load_graph_task(cur_task)
    cur_task.inputs['data'] = cur_task.outputs['graph']
    community_detection_model(cur_task)
    output_graph = cur_task.outputs['cd_graph']

if save_result_graph:
    output_graph.save('target/cd_%s' % (graph_file[graph_file.rfind('/', 0, len(graph_file) - 2) + 1:]))