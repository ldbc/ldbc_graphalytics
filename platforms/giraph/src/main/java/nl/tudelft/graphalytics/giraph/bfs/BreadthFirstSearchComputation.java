package nl.tudelft.graphalytics.giraph.bfs;

import static nl.tudelft.graphalytics.giraph.bfs.BreadthFirstSearchConfiguration.SOURCE_VERTEX;

import java.io.IOException;

import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;

/**
 * Implementation of a simple BFS (SSSP) on an unweighted, directed graph.
 * 
 * @author Tim Hegeman
 */
public class BreadthFirstSearchComputation extends BasicComputation<LongWritable, LongWritable, NullWritable, LongWritable> {

	/** Constant vertex value representing an unvisited vertex */ 
	private static final LongWritable UNVISITED = new LongWritable(Long.MAX_VALUE);
	
	@Override
	public void compute(Vertex<LongWritable, LongWritable, NullWritable> vertex,
			Iterable<LongWritable> messages) throws IOException {
		LongWritable bfsDepth = new LongWritable(getSuperstep());
		
		if (getSuperstep() == 0) {
			// During the first superstep only the source vertex should be active
			if (vertex.getId().get() == SOURCE_VERTEX.get(getConf())) {
				vertex.setValue(bfsDepth);
				sendMessageToAllEdges(vertex, vertex.getValue());
			} else {
				vertex.setValue(UNVISITED);
			}
		} else {
			// If this vertex was not yet visited, set the vertex depth and propagate to neighbours
			if (vertex.getValue().get() == UNVISITED.get()) {
				vertex.setValue(bfsDepth);
				sendMessageToAllEdges(vertex, vertex.getValue());
			}
		}
		
		// Always halt so the compute method is only executed for those vertices
		// that have an incoming message
		vertex.voteToHalt();
	}

}
