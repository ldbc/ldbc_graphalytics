package org.tudelft.graphalytics.algorithms;

public class CDParameters {
	private final float nodePreference;
	private final float hopAttenuation;
	private final int maxIterations;
	
	public CDParameters(float nodePreference, float hopAttenuation, int maxIterations) {
		this.nodePreference = nodePreference;
		this.hopAttenuation = hopAttenuation;
		this.maxIterations = maxIterations;
	}
	
	public float getNodePreference() {
		return nodePreference;
	}
	
	public float getHopAttenuation() {
		return hopAttenuation;
	}
	
	public int getMaxIterations() {
		return maxIterations;
	}
}
