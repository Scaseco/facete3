package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.impl.FacetedQueryBuilder;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;

import jersey.repackaged.com.google.common.collect.Sets;

public class QuickDl {

	public static Set<Resource> toSet(Model m, Collection<String> strs) {
		Set<Resource> result = strs.stream().map(m::expandPrefix).map(m::createResource).collect(Collectors.toSet());

		return result;
	}
	
	public static void main(String[] args) {
		Model m = RDFDataMgr.loadModel("/home/raven/Projects/Eclipse/DL-Learner/examples/poker/pair50.ttl");
		m.setNsPrefix("kb", "http://localhost/foo#");

		Set<Resource> pos = toSet(m, Arrays.asList(
		"kb:hand9",
		"kb:hand13",
		"kb:hand18",
		"kb:hand19",
		"kb:hand21",
		"kb:hand22",
		"kb:hand23",
		"kb:hand24",
		"kb:hand25",
		"kb:hand26",
		"kb:hand29",
		"kb:hand35",
		"kb:hand36",
		"kb:hand38",
		"kb:hand39",
		"kb:hand40",
		"kb:hand41",
		"kb:hand43",
		"kb:hand47",
		"kb:hand48"));
		

		Set<Resource> neg = toSet(m, Arrays.asList(
				"kb:hand0",
				"kb:hand1",
				"kb:hand2",
				"kb:hand3",
				"kb:hand4",
				"kb:hand5",
				"kb:hand6",
				"kb:hand7",
				"kb:hand8",
				"kb:hand10",
				"kb:hand11",
				"kb:hand12",
				"kb:hand14",
				"kb:hand15",
				"kb:hand16",
				"kb:hand17",
				"kb:hand20",
				"kb:hand27",
				"kb:hand28",
				"kb:hand30",
				"kb:hand31",
				"kb:hand32",
				"kb:hand33",
				"kb:hand34",
				"kb:hand37",
				"kb:hand42",
				"kb:hand44",
				"kb:hand45",
				"kb:hand46"
		));

		
		FacetedQuery fq = FacetedQueryBuilder.builder()
			.configDataConnection().setSource(m).end().create();

		
		UnaryRelation posConcept = ConceptUtils.createConceptFromRdfNodes(pos);
		UnaryRelation negConcept = ConceptUtils.createConceptFromRdfNodes(neg);
		
		//System.out.println();
		// Facet counts correspond to all available predicates and are the basis for creating
		// role restrictions (of any quantification)
		List<FacetCount> posFcs = fq.baseConcept(posConcept).focus().fwd().facetCounts().exec().toList().blockingGet();
		System.out.println("posFcs: " + posFcs);

		List<FacetCount> negFcs = fq.baseConcept(negConcept).focus().fwd().facetCounts().exec().toList().blockingGet();
		System.out.println("negFcs: " + negFcs);

		// Both pos and neg have the exact same sets of predicates, namely
		// rdf:type and eg:hasCard
		// so there is no existential restriction candidate
		
		/// Pruning
		
		// - Remove all facet values with a count of 1: These values are too discriminative
		//   (there is only a single resource in the example list having that value)\
		// - Remove all facet values that are common to all example resources
		//   (all resources have that value, so its not discriminative at all)
		
		
		// TODO In DL, we cannot use instances as fillers. The implications is, that
		// we don't want to query for the usual facet values -
		// Instead, we want the 'facetFillerCount' - i.e. instead of counting values, we want to count
		// the *type* of the values
		// But the processing afterwards is the same:
		
		// Check if there are any facet-filler pairs, that discriminate the positive from the negative examples
		
		List<FacetValueCount> posFvcs = fq.baseConcept(posConcept).focus().fwd().facetValueCounts().exec().toList().blockingGet()
			.stream()
			.filter(fv -> fv.getFocusCount().getCount() != 1)
			.filter(fv -> fv.getFocusCount().getCount() != pos.size())			
			.sorted((a, b) -> (int)(a.getFocusCount().getCount() - b.getFocusCount().getCount()))
			.collect(Collectors.toList());
			
		System.out.println("posFVCS: " + posFvcs);
		
		
		fq.baseConcept(negConcept);
		List<FacetValueCount> negFvcs = fq.baseConcept(negConcept).focus().fwd().facetValueCounts().exec().toList().blockingGet()
				.stream()
				.filter(fv -> fv.getFocusCount().getCount() != 1)
				.filter(fv -> fv.getFocusCount().getCount() != neg.size())			
				.sorted((a, b) -> (int)(a.getFocusCount().getCount() - b.getFocusCount().getCount()))
				.collect(Collectors.toList());
				
		System.out.println("negFVCS: " + negFvcs);

		
		// If both sets are empty, there are no discriminative features
		// and we need to look at longer paths resources
		// This is the case for this example
		
		
		// ISSUES We need the following features
		// - facet counts that refer to the number of focus resources having that facet
		//   (current facet counts only refer to the number of available distinct values for that facet)
		
		
		/// Candidate cluster creation
		
		
		// In general, at this stage we need to deal with non-trivial discrimination:
		// This means, that there is no single feature that is common to all positive (negative) examples.
		// Hence, a thumb-times-pi approach might be to generate candidate and validate candidate
		// sets of features:
		// Starting with the minimum set of discriminative features that cover
		// the maximum number of positive resources, start adding features until we reach solutions
		// that don't cover (too m)any negative examples (this sounds like knapsack problem is part of the solution)

		List<FacetCount> posff = fq.baseConcept(posConcept).focus().fwd("http://localhost/foo#hasCard").one().fwd().facetCounts().exec().toList().blockingGet();
		posff.forEach(x -> System.out.println("posFC: " + x));
		
		
		
		List<FacetCount> negff = fq.baseConcept(negConcept).focus().fwd("http://localhost/foo#hasCard").one().fwd().facetCounts().exec().toList().blockingGet();
		negff.forEach(x -> System.out.println("negFC: " + x));

		Set<Resource> posP = posff.stream().collect(Collectors.toSet());
		Set<Resource> negP = negff.stream().collect(Collectors.toSet());
		Set<Resource> diff = Sets.difference(posP, negP);
		System.out.println("Diff: " + diff);
		
		
		// In the example, at this stage the diff is:
		// Diff: [FacetCountImpl [http://localhost/foo#sameRank: CountInfo [count=21, hasMoreItems=false, itemLimit=null]]]
        // So EXISTS hasCard EXIST sameRank is discriminates the positive and negative examples	
		
		
//		List<FacetValueCount> posffs = fq.baseConcept(posConcept).focus().fwd("http://localhost/foo#hasCard").one().fwd().facetValueCounts().exec().toList().blockingGet();
//		posffs.forEach(x -> System.out.println("posFVC: " + x));
//		
//		List<FacetValueCount> negffs = fq.baseConcept(negConcept).focus().fwd("http://localhost/foo#hasCard").one().fwd().facetValueCounts().exec().toList().blockingGet();
//		posffs.forEach(x -> System.out.println("negFVC: " + x));
		
		//Collector
		
		
		//RDFDataMgr.write(System.out, ResourceUtils.reachableClosure(pos.iterator().next()), RDFFormat.TURTLE_PRETTY);
		
	}
	
}
