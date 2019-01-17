package org.aksw.facete.v3.api;

/**
 * Booleans are confusing
 */
public enum Direction {
	FORWARD(true), BACKWARD(false);
	
	boolean isForward;
	
	Direction(boolean isForward) {
		this.isForward = isForward; 
	}
	
	public boolean isForward() {
		return isForward;
	}
	
	public boolean isBackward() {
		return !isForward;
	}
}
