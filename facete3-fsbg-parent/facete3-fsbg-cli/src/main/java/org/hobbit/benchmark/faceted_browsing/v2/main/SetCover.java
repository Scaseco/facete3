package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import org.aksw.commons.collections.tagmap.TagIndex;
import org.aksw.commons.collections.tagmap.TagIndexImpl;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

import jersey.repackaged.com.google.common.collect.Maps;

public class SetCover {

	
	public static void main(String[] args) {

		
		Model m = ModelFactory.createDefaultModel();

		Random rand = new Random(0);
		int n = 5;

		Multimap<Resource, Resource> mm = HashMultimap.create();
		// max number of resources and features
		int nr = 3;
		int nf = 3;
		for(int i = 0; i < n; ++i) {
			String r = "http://www.example.org/r" + rand.nextInt(nr);
			String f = "http://www.example.org/f" + rand.nextInt(nf);
			mm.put(m.createResource(r), m.createResource(f));

			mm.put(m.createResource(r), m.createResource("http://www.example.org/foo"));
			mm.put(m.createResource(r), m.createResource("http://www.example.org/bar"));
		}
		
		System.out.println(mm);
		
		
		covers(mm);
	}
	
	/**
	 * This method is a placeholder for any
	 * set cover / hitting set
	 * solver.
	 * 
	 * 
	 * 
	 * @param resourceToFeature
	 */
	public static <R, F> void covers(Multimap<R, F> resourceToFeatures) {
		//FeatureMap<F, R> index = new FeatureMapImpl<>();
		TagIndex<F, R> index = TagIndexImpl.create((a, b) -> Objects.toString(a).compareTo(Objects.toString(b)));
		
		for(Entry<R, Collection<F>> e : resourceToFeatures.asMap().entrySet()) {
			index.put((Set<F>)e.getValue(), e.getKey());
		}

		
		// TODO Get the set of all features, and then add to every resource NOT
		// having a certain feature a new feature that denotes the absence.
		
		// e.g. r1: {f1, f2}, r2: {f2, f3}
		// -> r1: {f1, f2, !f3}, r2: {!f1, f2, f3}
		Set<F> features = null;
		
		
		// map every feature to its set of resources  ...
		Multimap<F, R> featureToResources = Multimaps.invertFrom(resourceToFeatures, HashMultimap.create());

		// ... then find out the set of features that all have the same set of resources
		TagIndex<R, F> coocf = TagIndexImpl.create((a, b) -> Objects.toString(a).compareTo(Objects.toString(b)));
		
		for(Entry<F, Collection<R>> e : featureToResources.asMap().entrySet()) {
			coocf.put((Set<R>)e.getValue(), e.getKey());
		}

		// Internally, the tag index has allocated an id for every distinct set of features
		// So from here we use the IDs instead of the feature sets
		
		
		/* start of greedy approach */
		
		
		// Sketch:
		// Sort the feature sets by the number of resources they cover
		// Then pick the one with most covering resources
		// remove the covered resources, and continue with the set that covers most
		// of the remaining resources - repeat until everything is covered
		
		SetMultimap<Long, R> idToValues = null;

		List<Long> orderedFeatureIds = idToValues.asMap().entrySet().stream()
				.map(e -> Maps.immutableEntry(e.getKey(), e.getValue().size()))
				.sorted((a, b) -> a.getValue().compareTo(b.getValue()))
				.map(Entry::getKey)
				.collect(Collectors.toList())
				;
		
		
		System.out.println("co-occurent features: " + coocf);
		
//		Multimap<F, R> featureToResources = resourceToFeatures.entries().stream()
//				.collect(Multimaps.flatteningToMultimap(
//						Entry::getKey,
//						e -> Stream.of(e.getValue()),
//						HashMultimap::create));
		
		System.out.println(featureToResources);
		
//		Set<F> features = new HashSet<>(resourceToFeature.values());
//		for(F feature : features) {
//			
//		}
//		
//		
//		Multimap<F, R> featureToResource = HashMultimap.create();
//		Multimaps.index(
//		
//		// Find co-occurrent features - i.e. feature sets having the same set of resources)
//		for(Entry<Set<F>, Set<R>> e : index) {
//			if(e.get$)
//		}
//		
		
		//for(resourceToFeature.entries()
		
		
	}
}
