package org.tudelft.graphalytics.giraph.stats;

import static org.tudelft.graphalytics.giraph.stats.StatsMasterComputation.LCC_AGGREGATOR_NAME;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;

import com.google.common.collect.Iterables;

/**
 * Computation for the local clustering coefficient algorithm on Giraph for directed graphs.
 *
 * @author Tim Hegeman
 */
public class DirectedStatsComputation extends
		BasicComputation<LongWritable, IntWritable, NullWritable, StatsMessage> {

	@Override
	public void compute(Vertex<LongWritable, IntWritable, NullWritable> vertex,
			Iterable<StatsMessage> messages) throws IOException {
		if (getSuperstep() == 0) {
			// First superstep: inform all neighbours (outgoing edges) that they have an incoming edge
			sendMessageToAllEdges(vertex, new StatsMessage(vertex.getId().get()));
			return;
		} else if (getSuperstep() == 1) {
			// Second superstep: create a set of neighbours, for each pair ask if they are connected
			Set<Long> neighbours = collectNeighbourSet(vertex, messages);
			sendConnectionInquiries(vertex.getId().get(), neighbours);
			vertex.setValue(new IntWritable(neighbours.size()));
			return;
		} else if (getSuperstep() == 2) {
			// Third superstep: for each inquiry reply iff the requested edge exists
			sendConnectionReplies(vertex.getEdges(), messages);
			return;
		} else if (getSuperstep() == 3) {
			// Fourth superstep: compute the ratio of responses to requests
			double lcc = computeLCC(vertex.getValue().get(), messages);
			aggregate(LCC_AGGREGATOR_NAME, new DoubleAverage(lcc));
			// Remove the vertices to stop the graph from being written back to disk 
			removeVertexRequest(vertex.getId());
		}
	}

	private static Set<Long> collectNeighbourSet(Vertex<LongWritable, IntWritable, NullWritable> vertex,
			Iterable<StatsMessage> messages) {
		Set<Long> neighbours = new HashSet<>();
		
		// Add all outgoing edges to the neighbours set
		for (Edge<LongWritable, NullWritable> edge : vertex.getEdges())
			neighbours.add(edge.getTargetVertexId().get());
		// Add all incoming edges to the neighbours set
		for (StatsMessage msg : messages)
			neighbours.add(msg.getSource());
		
		return neighbours;
	}
	
	private void sendConnectionInquiries(long sourceVertexId, Set<Long> neighbours) {
		// No messages to be sent if there is at most one neighbour
		if (neighbours.size() <= 1)
			return;
		
		// Send out inquiries in an all-pair fashion
		LongWritable messageDestinationId = new LongWritable();
		for (long destinationNeighbour : neighbours) {
			StatsMessage msg = new StatsMessage(sourceVertexId, destinationNeighbour);
			for (long inquiredNeighbour : neighbours) {
				// Do not ask if a node is connected to itself
				if (destinationNeighbour == inquiredNeighbour)
					continue;
				messageDestinationId.set(inquiredNeighbour);
				sendMessage(messageDestinationId, msg);
				
			}
		}
	}
	
	private void sendConnectionReplies(Iterable<Edge<LongWritable, NullWritable>> edges,
			Iterable<StatsMessage> inquiries) {
		// Construct a lookup set for the list of edges
		Set<Long> edgeLookup = new HashSet<>();
		for (Edge<LongWritable, NullWritable> edge : edges)
			edgeLookup.add(edge.getTargetVertexId().get());
		// Loop through the inquiries and reply to those for which an edge exists
		LongWritable destinationId = new LongWritable();
		StatsMessage confirmation = new StatsMessage();
		for (StatsMessage msg : inquiries) {
			if (edgeLookup.contains(msg.getDestination())) {
				destinationId.set(msg.getSource());
				sendMessage(destinationId, confirmation);
			}
		}
	}
	
	private static double computeLCC(long numberOfNeighbours, Iterable<StatsMessage> messages) {
		// Count the number of (positive) replies
		long numberOfMessages = Iterables.size(messages);
		// Compute the LCC as the ratio between the number of existing edges and number of possible edges
		return (double)numberOfMessages / numberOfNeighbours / (numberOfNeighbours - 1);
	}
}
