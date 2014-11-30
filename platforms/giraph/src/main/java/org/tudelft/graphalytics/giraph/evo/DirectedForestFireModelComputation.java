package org.tudelft.graphalytics.giraph.evo;

import static org.tudelft.graphalytics.giraph.evo.ForestFireModelJob.BACKWARD_PROBABILITY;
import static org.tudelft.graphalytics.giraph.evo.ForestFireModelJob.FORWARD_PROBABILITY;
import static org.tudelft.graphalytics.giraph.evo.ForestFireModelJob.MAX_ITERATIONS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.giraph.conf.ImmutableClassesGiraphConfiguration;
import org.apache.giraph.edge.EdgeFactory;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.tudelft.graphalytics.giraph.evo.ForestFireModelData.ForestFireModelState;

public class DirectedForestFireModelComputation extends
		BasicComputation<LongWritable, ForestFireModelData, NullWritable, ForestFireModelMessage> {

	private int maxIterations;
	private float forwardProbability;
	private float backwardProbability;
	private Random rnd = new Random();
	
	@Override
	public void setConf(
			ImmutableClassesGiraphConfiguration<LongWritable, ForestFireModelData, NullWritable> conf) {
		super.setConf(conf);
		maxIterations = MAX_ITERATIONS.get(getConf());
		forwardProbability = FORWARD_PROBABILITY.get(getConf());
		backwardProbability = BACKWARD_PROBABILITY.get(getConf());
	}
	
	@Override
	public void compute(
			Vertex<LongWritable, ForestFireModelData, NullWritable> vertex,
			Iterable<ForestFireModelMessage> messages) throws IOException {
		// Perform appropriate actions for the setup/teardown supersteps
		long superstep = getSuperstep();
		if (superstep == 0) {
			informNeighboursOfEdges(vertex);
			runForAmbassador(vertex.getId().get());
			return;
		} else if (superstep == 1) {
			collectInEdges(vertex, messages);
			createVertexIfAmbassador(vertex);
			return;
		} else if (superstep >= 2 + maxIterations * 3) {
			vertex.voteToHalt();
			return;
		}
		
		// Handle the state-based propagation of the forest fire
		int indexIntoStateLoop = (int)((superstep - 2) % 3);
		if (indexIntoStateLoop == 0) {
			// Process incoming messages ("catching fire")
			catchFire(vertex, messages);
			// Burning nodes need information about the liveness of their neighbours
			requestNeighbourLiveness(vertex);
		} else if (indexIntoStateLoop == 1) {
			// Handle liveness requests received from burning nodes
			replyToLivenessRequests(vertex, messages);
		} else {
			// Burn links where needed
			burnLinks(vertex, messages);
			// Finally, burn out the vertex if it was burning
			burnOut(vertex);
			
		}
		vertex.voteToHalt();
	}

	private void informNeighboursOfEdges(Vertex<LongWritable, ForestFireModelData, NullWritable> vertex) {
		sendMessageToAllEdges(vertex, ForestFireModelMessage.neighbourNotification(vertex.getId().get()));
	}
	
	private void runForAmbassador(long vertexId) {
		ForestFireModelWorkerContext context = this.<ForestFireModelWorkerContext>getWorkerContext();
		context.registerVertex(vertexId);
	}
	
	private void collectInEdges(Vertex<LongWritable, ForestFireModelData, NullWritable> vertex,
			Iterable<ForestFireModelMessage> messages) {
		List<Long> edges = new ArrayList<>();
		for (ForestFireModelMessage message : messages) {
			edges.add(message.getSourceId());
		}
		vertex.setValue(ForestFireModelData.fromInEdges(edges));
	}
	
	private void createVertexIfAmbassador(Vertex<LongWritable, ForestFireModelData, NullWritable> vertex)
			throws IOException {
		ForestFireModelWorkerContext context = this.<ForestFireModelWorkerContext>getWorkerContext();
		boolean isAmbassador = context.isAmbassador(vertex.getId().get());
		if (isAmbassador) {
			// Create a new vertex that connects to this ambassador
			long newVertexId = context.getNewVertexId();
			ForestFireModelData data = ForestFireModelData.fromInEdges(Collections.<Long>emptyList());
			// The new vertex is burned out, and the ambassador is burning
			data.setState(newVertexId, ForestFireModelState.BURNED);
			vertex.getValue().setState(newVertexId, ForestFireModelState.BURNING);
			// Finalize the creation
			addVertexRequest(new LongWritable(newVertexId), data);
			addEdgeRequest(new LongWritable(newVertexId), EdgeFactory.create(vertex.getId()));
		}
	}
	
	private void catchFire(Vertex<LongWritable, ForestFireModelData, NullWritable> vertex,
			Iterable<ForestFireModelMessage> messages) {
		for (ForestFireModelMessage message : messages) {
			vertex.getValue().setState(message.getInstigatorId(), ForestFireModelState.BURNING);
		}
	}
	
	private void requestNeighbourLiveness(Vertex<LongWritable, ForestFireModelData, NullWritable> vertex) {
		for (Map.Entry<Long, ForestFireModelState> state : vertex.getValue().getStates()) {
			if (state.getValue() == ForestFireModelState.BURNING) {
				// Request from out-edges
				ForestFireModelMessage livenessRequest =
						ForestFireModelMessage.livenessRequest(vertex.getId().get(), state.getKey());
				sendMessageToAllEdges(vertex, livenessRequest);
				// Request from in-edges
				for (long inEdge : vertex.getValue().getInEdges()) {
					sendMessage(new LongWritable(inEdge), livenessRequest);
				}
			}
		}
	}
	
	private void replyToLivenessRequests(Vertex<LongWritable, ForestFireModelData, NullWritable> vertex,
			Iterable<ForestFireModelMessage> messages) {
		for (ForestFireModelMessage message : messages) {
			if (vertex.getValue().getState(message.getInstigatorId()) == ForestFireModelState.ALIVE) {
				sendMessage(new LongWritable(message.getSourceId()),
						ForestFireModelMessage.aliveAcknowledgement(vertex.getId().get(),
								message.getInstigatorId()));
			}
		}
	}
	
	private void burnLinks(Vertex<LongWritable, ForestFireModelData, NullWritable> vertex,
			Iterable<ForestFireModelMessage> messages) throws IOException {
		// Create a fast lookup for incoming node IDs
		Set<Long> inLinks = new HashSet<>();
		for (long in : vertex.getValue().getInEdges())
			inLinks.add(in);
		// Group all incoming liveness messages by instigator ID and by incoming/outgoing link
		Map<Long, Set<Long>> aliveInLinks = new HashMap<>();
		Map<Long, Set<Long>> aliveOutLinks = new HashMap<>();
		for (ForestFireModelMessage message : messages) {
			long instigatorId = message.getInstigatorId();
			long sourceId = message.getSourceId();
			if (inLinks.contains(sourceId))
				addToLinksMap(aliveInLinks, instigatorId, sourceId);
			else
				addToLinksMap(aliveOutLinks, instigatorId, sourceId);
		}
		// Go through the set of possible instigator IDs (current burning states) and process them
		for (long instigatorId : vertex.getValue().getInstigatorIds()) {
			if (vertex.getValue().getState(instigatorId) == ForestFireModelState.BURNING) {
				LongWritable instigatorIdFull = new LongWritable(instigatorId);
				ForestFireModelMessage message = ForestFireModelMessage.burningNotification(instigatorId);
				// Handle out links
				if (aliveOutLinks.containsKey(instigatorId)) {
					int outBurningCount = getGeometricVariable(1.0f - forwardProbability);
					Set<Long> outBurning = selectLinksFromSet(aliveOutLinks.get(instigatorId), outBurningCount);
					for (Long target : outBurning) {
						LongWritable targetId = new LongWritable(target);
						addEdgeRequest(instigatorIdFull, EdgeFactory.create(targetId));
						sendMessage(targetId, message);
					}
				}
				// Handle in links
				if (aliveInLinks.containsKey(instigatorId)) {
					int inBurningCount = getGeometricVariable(1.0f - backwardProbability);
					Set<Long> inBurning = selectLinksFromSet(aliveInLinks.get(instigatorId), inBurningCount);
					for (Long target : inBurning) {
						LongWritable targetId = new LongWritable(target);
						addEdgeRequest(instigatorIdFull, EdgeFactory.create(targetId));
						sendMessage(targetId, message);
					}
				}
			}
		}
	}
	
	private static void addToLinksMap(Map<Long, Set<Long>> linksMap, long instigatorId, long sourceId) {
		if (!linksMap.containsKey(instigatorId))
			linksMap.put(instigatorId, new HashSet<Long>());
		linksMap.get(instigatorId).add(sourceId);
	}
	
	private Set<Long> selectLinksFromSet(Set<Long> links, int amount) {
		if (amount >= links.size())
			return links;
		
		List<Long> linksAsList = new ArrayList<>(links);
		Set<Long> selection = new HashSet<>();
		while (amount > 0) {
			int idx = rnd.nextInt(linksAsList.size());
			selection.add(linksAsList.get(idx));
			linksAsList.remove(idx);
			amount--;
		}
		return selection;
	}
	
	private int getGeometricVariable(float p) {
		if (p == 1.0f)
			return 0;
		return (int)(Math.log(rnd.nextFloat()) / Math.log(1 - p)); 
	}
	
	private void burnOut(Vertex<LongWritable, ForestFireModelData, NullWritable> vertex) {
		// Loop through the list of states and replacing burning with burned
		for (Map.Entry<Long, ForestFireModelState> state : vertex.getValue().getStates())
			if (state.getValue() == ForestFireModelState.BURNING)
				vertex.getValue().setState(state.getKey(), ForestFireModelState.BURNED);
	}
	
}
