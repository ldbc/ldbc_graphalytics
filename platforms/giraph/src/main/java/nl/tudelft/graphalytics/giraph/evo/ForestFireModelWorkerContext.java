package nl.tudelft.graphalytics.giraph.evo;


import static nl.tudelft.graphalytics.giraph.evo.ForestFireModelConfiguration.AVAILABLE_VERTEX_ID;
import static nl.tudelft.graphalytics.giraph.evo.ForestFireModelConfiguration.NEW_VERTICES;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Random;

import org.apache.giraph.worker.WorkerContext;

/**
 * Per-worker context for the forest fire model algorithm. Used to select
 * ambassadors during the first superstep of the algorithm.
 *
 * @author Tim Hegeman
 */
public class ForestFireModelWorkerContext extends WorkerContext {

	private Random rnd = new Random();

	private boolean initialized = false;
	private long nextVertexId;
	private long numberOfNewVertices;
	private PriorityQueue<AmbassadorSelection> candidateAmbassadors = new PriorityQueue<>();
	private long[] selectedAmbassadors = null;

	/**
	 * @param vertexId a vertex in this worker's partition
	 */
	public synchronized void registerVertex(long vertexId) {
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

	/**
	 * @param vertexId a vertex in this worker's partition
	 * @return true iff the vertex was selected as an ambassador
	 */
	public boolean isAmbassador(long vertexId) {
		return Arrays.binarySearch(selectedAmbassadors, vertexId) >= 0;
	}

	/**
	 * @return a guaranteed unique vertex id
	 */
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
	public synchronized void preSuperstep() {
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
		if (getSuperstep() == 0) {
			// After the first superstep, create an ordered array with the selected ambassadors
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
			return obj instanceof AmbassadorSelection && this.compareTo((AmbassadorSelection) obj) == 0;
		}

		@Override
		public int hashCode() {
			return score != +0.0f ? Float.floatToIntBits(score) : 0;
		}
	}
	
}
