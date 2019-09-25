package org.hobbit.benchmark.faceted_browsing.v2.task_generator;

import org.apache.jena.sparql.path.Path;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

//class RangeBuilder<T extends Comparable<T>> {
//	protected T hasLowerBound = true;
//	protected T hasUpperBound = true;
//	protected BoundType lowerBoundType = BoundType.CLOSED;
//	protected BoundType upperBoundType = BoundType.CLOSED;	
//
//	
//	RangeBuilder<T> hasLowerBound(boolean onOrOff) {
//		
//	}
//	
//	Range<T> create(T lower, T upper) {
//		Range<T> result;
//		Range.range(lower, lowerType, upper, upperType)
//
//		if(hasLowerBound) {
//			if(hasUpperBound) {
//				if(lowerBoundClosed) {
//					if(upperBoundClosed) {
//						result = Range.closed(lower, upper);
//					} else {
//						result = Range.closedOpen(lower, upper);
//					}
//				} else {
//					Range.
//					if(upperBoundClosed) {
//						result = Range.openClosed(lower, upper);
//					} else {
//						result = Range.open(lower, upper);
//					}
//				}
//			} else {
//				if(lowerBoundClosed) {
//					result = Range.atLeast(lower);
//				} else {
//					result = Range.greaterThan(lower);
//				}
//			}
//		} else {
//			if(hasUpperBound) {
//				if(upperBoundClosed) {
//					result = Range.atMost(upper);
//				} else {
//					result = Range.lessThan(upper);
//				}
//			} else {
//				result = Range.all();
//			}
//		}
//		
//		return result;
//	}
//	
//	
//}

public class TraversalGenerator {
	public void setPathPattern(Path path) {
		
	}
	
}
