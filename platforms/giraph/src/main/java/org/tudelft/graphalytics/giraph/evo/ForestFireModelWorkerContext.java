package org.tudelft.graphalytics.giraph.evo;

import static org.tudelft.graphalytics.giraph.evo.ForestFireModelJob.NEW_VERTICES;
import static org.tudelft.graphalytics.giraph.evo.ForestFireModelJob.AVAILABLE_VERTEX_ID;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Random;

import org.apache.giraph.worker.WorkerContext;

public class ForestFireModelWorkerContext extends WorkerContext {

	private Random rnd = new Random();

	private boolean initialized = false;
	private long nextVertexId;
	private long numberOfNewVertices;
	private PriorityQueue<AmbassadorSelection> candidateAmbassadors = new PriorityQueue<>();
	private long[] selectedAmbassadors = null;
	
	public synchronized void registerVertex(long vertexId) {
		// TODO: Only allowed during the first superstep
		if (numberOfNewVertices == 0)
			return;
		
		// Consider the given vertex as a potential ambassador by assigning it a random score
		AmbassadorSelection candidate = new AmbassadorSelection(rnd.nextFloat(), vertexId);
		if (candidateAmbassadors.size() < numberOfNewVertices) {
			// Fill the list of candidates
			candidateAmbassadors.add(candidate);
		} else if (candidateAmbassadors.peek().compareTo(candidate) <= 0) {
			// Replace the lowest scoring candidate if the new vertex has a higher score
			candidateAmbassadors.add(candidate);
			candidateAmbassadors.poll();
		}
	}
	
	public boolean isAmbassador(long vertexId) {
		return Arrays.binarySearch(selectedAmbassadors, vertexId) >= 0;
	}
	
	public synchronized long getNewVertexId() {
		long newId = nextVertexId;
		nextVertexId += getWorkerCount();
		return newId;
	}
	
	@Override
	public void preApplication() throws InstantiationException,
			IllegalAccessException {
	}

	@Override
	public void postApplication() {
	}

	@Override
	public void preSuperstep() {
		if (!initialized) {
			// Calculate the number of new vertices this worker should create
			int numWorkers = getWorkerCount();
			long numNewVertices = NEW_VERTICES.get(getConf());
			long verticesPerWorker = numNewVertices / numWorkers;
			numberOfNewVertices = numNewVertices;
	
			// Split up the remaining < numWorkers vertices based on worker IDs
			long remainder = numNewVertices - verticesPerWorker * numWorkers;
			if (remainder != 0 && getMyWorkerIndex() < remainder)
				numberOfNewVertices++;
			
			// Get the first vertex ID to assign to a new vertex
			nextVertexId = AVAILABLE_VERTEX_ID.get(getConf()) + getMyWorkerIndex();
			
			initialized = true;
		}
	}

	@Override
	public void postSuperstep() {
		System.out.println("postSuperstep(): " + getSuperstep());
		if (getSuperstep() == 0) {
			selectedAmbassadors = new long[candidateAmbassadors.size()];
			int i = 0;
			for (AmbassadorSelection selection : candidateAmbassadors) {
				selectedAmbassadors[i++] = selection.getVertexId();
			}
			Arrays.sort(selectedAmbassadors);
		}
	}

	/**
	 * Represents a possible choice for an ambassador, with a vertex ID and a score.
	 * The selections are ordered by score values. 
	 */
	private static class AmbassadorSelection implements Comparable<AmbassadorSelection> {
		private float score;
		private long vertexId;
		
		public AmbassadorSelection(float score, long vertexId) {
			this.score = score;
			this.vertexId = vertexId;
		}
		
		public float getScore() {
			return score;
		}
		public long getVertexId() {
			return vertexId;
		}

		@Override
		public int compareTo(AmbassadorSelection o) {
			return Float.compare(getScore(), o.getScore());
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof AmbassadorSelection))
				return false;
			return this.compareTo((AmbassadorSelection)obj) == 0;
		}
	}
	
}
