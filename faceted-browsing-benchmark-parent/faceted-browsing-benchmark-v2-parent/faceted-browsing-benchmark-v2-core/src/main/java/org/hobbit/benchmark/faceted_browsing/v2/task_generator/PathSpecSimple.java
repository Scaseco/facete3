package org.hobbit.benchmark.faceted_browsing.v2.task_generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.aksw.jena_sparql_api.util.sparql.syntax.path.PathUtils;
import org.apache.jena.sparql.path.P_Path0;

import com.google.common.collect.Maps;

public class PathSpecSimple {
	// Validation attributes - paths must meet these constraints for acceptance
	protected int minLength;
	protected int numRequiredReverseSteps;
	
	
	
	public int getNumRequiredReverseSteps() {
		return numRequiredReverseSteps;
	}

	public PathSpecSimple setNumRequiredReverseSteps(int numRequiredReverseSteps) {
		this.numRequiredReverseSteps = numRequiredReverseSteps;
		return this;
	}

	// TODO Rename into desired path length
	protected int maxLength;
	
	// true -> forward, false -> backwards
	// The draw with replacement pmf is consumed first. Afterwards, the fallback pmf kicks in.
	protected List<Entry<Boolean, Double>> drawWithReplacementPmf;
	protected List<Entry<Boolean, Double>> fallbackPmf;

	public int getMinLength() {
		return minLength;
	}

	public PathSpecSimple setMinLength(int minLength) {
		this.minLength = minLength;
		return this;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public PathSpecSimple setMaxLength(int maxLength) {
		this.maxLength = maxLength;
		return this;
	}

	public List<Entry<Boolean, Double>> getDrawWithReplacementPmf() {
		return drawWithReplacementPmf;
	}

	public PathSpecSimple setDrawWithReplacementPmf(List<Entry<Boolean, Double>> drawWithReplacementPmf) {
		this.drawWithReplacementPmf = drawWithReplacementPmf;
		return this;
	}

	public List<Entry<Boolean, Double>> getFallbackPmf() {
		return fallbackPmf;
	}

	public PathSpecSimple setFallbackPmf(List<Entry<Boolean, Double>> fallbackPmf) {
		this.fallbackPmf = fallbackPmf;
		return this;
	}

	
	/**
	 * numReversePool: The number of reverse traversals (must be <= the desired path length)
	 * 
	 * @param minPathLength
	 * @param desiredPathLength
	 * @param numRequiredReverseSteps
	 * @param bwdChance
	 * @param fwdChance
	 * @return
	 */
	public static PathSpecSimple create(int minPathLength, int desiredPathLength, int numRequiredReverseSteps, double bwdChance, double fwdChance) {
		if(numRequiredReverseSteps > desiredPathLength) {
			throw new RuntimeException("Cannot require more reverse traversals than maximum path length");
		}
		
		List<Entry<Boolean, Double>> consumingPmf = new ArrayList<>();
		for(int i = 0; i < numRequiredReverseSteps; ++i) {
			consumingPmf.add(Maps.immutableEntry(false, 1.0));
		}
		
		for(int i = numRequiredReverseSteps; i < desiredPathLength; ++i) {
			consumingPmf.add(Maps.immutableEntry(true, 1.0));			
		}
		
		List<Entry<Boolean, Double>> fallbackPmf = new ArrayList<>();
		fallbackPmf.add(Maps.immutableEntry(false, bwdChance));
		fallbackPmf.add(Maps.immutableEntry(true, fwdChance));
	
		PathSpecSimple result = new PathSpecSimple();
		result
			.setMinLength(minPathLength)
			.setMaxLength(desiredPathLength)
			.setNumRequiredReverseSteps(numRequiredReverseSteps)
			.setDrawWithReplacementPmf(consumingPmf)
			.setFallbackPmf(fallbackPmf);
	
		return result;
	}
	
	
	
	public static Predicate<List<P_Path0>> createValidator(PathSpecSimple pathSpec) {
		return steps -> {
			//List<P_Path0> steps = PathVisitorToList.toList(path);
			boolean result = steps.size() >= pathSpec.getMinLength() &&
					PathUtils.countReverseLinks(steps) >= pathSpec.getNumRequiredReverseSteps();
			return result;
		};
	}
	
	public static WeightedSelector<Boolean> createSelector(PathSpecSimple pathSpec) {
		WeightedSelector<Boolean> result = null;
		if(pathSpec.getDrawWithReplacementPmf() != null) {
			result = WeigthedSelectorDrawWithReplacement.create(pathSpec.getDrawWithReplacementPmf());
		}

		if(pathSpec.getFallbackPmf() != null) {
			WeightedSelector<Boolean> tmp = WeightedSelectorImmutable.create(pathSpec.getFallbackPmf());

			if(result != null) {
				result = new WeigthedSelectorFailover<>(result, tmp);
			}
		}
		
		return result;
	}
}