package org.hobbit.benchmark.faceted_browsing.v2;

import org.aksw.facete.v3.api.FacetConstraint;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetNodeResource;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.impl.FacetedQueryBuilder;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;

public class TestFacetedQueryChRoot {


    @Test
    public void testFacetedQueryChRootSimple2BgpNodes() {

        FacetedQuery fq =
            FacetedQueryBuilder.builder()
                .configDataConnection().defaultModel().end()
            .create();

        FacetNode distributionFn = fq.root().fwd(DCAT.distribution).one();

        fq.root().fwd(RDF.type).one().constraints().eq(DCAT.Dataset).activate();

//		System.out.println(distributionFn.availableValues().toConstructQuery());

        RDFDataMgr.write(System.out, fq.root().as(FacetNodeResource.class).state().getModel(), RDFFormat.TURTLE_PRETTY);

        System.out.println("--------------------------------------------");
        FacetNode tmp = fq.root().fwd(DCAT.distribution).one();
        tmp.chRoot();//.chFocus();
        RDFDataMgr.write(System.out, fq.root().as(FacetNodeResource.class).state().getModel(), RDFFormat.TURTLE_PRETTY);

        for(FacetConstraint c : fq.constraints()) {
            System.out.println(c);
        }

        System.out.println(distributionFn.availableValues().toConstructQuery());

        System.out.println(fq.focus().availableValues().toConstructQuery());
    }

    /**
     * A clash situation with chRoot:
     *
     * We start with
     * ?root knows ?s
     * ?s ^knows ?y
     *
     * and change the root to ?s which gives
     *
     * ?s ^knows ?root
     * ?s ^knows ?y
     *
     * This means that the BgpMultiNode from ?s and ?root have to be merged
     *
     * Update: Um the above example is somewhat wrong - it should be
     * step 1: substitution:
     * ?root knows ?newRoot
     * ?newRoot ^knows ?y
     *
     * step 2: rotate root to subject (so that all edges are outgoing):
     * ?newRoot ^knows ?oldRoot
     * ?newRoot ^knows ?y
     *
     * But still the problem remains that if we had a path without alias e.g. just [^knows] then its ambigious which transition to take.
     * Note that we are dealing with trees. Maybe we could introduce a special alias prefix "parent" to track that one node was originally a parent?
     *
     * So if we had a transition
     * x (<label> fwd "default" TARGET) y
     * and made y the root then we would get
     * y (<label> bwd "parent.default" TARGET) x
     *
     * Conversely, if we made x the parent of y again we can remove the "parent" prefix.
     *
     * --- Maybe we could track that ?newRoot ^knows ^y is the default one because it existing on ?oldRoot before?
     *
     *
     *
     *
     * BgpMultiNode is just a set view so there is no issue with merging.
     * Rather, the problem is that this issue cannot be resolved using local aliases - instead global ones need to be used to avoid clashes.
     *
     */
    @Test
    public void testFacetedQueryChRootClash2BgpNodes() {
        FacetedQuery fq =
                FacetedQueryBuilder.builder()
                    .configDataConnection().defaultModel().end()
                .create();

        FacetNode root = fq.root();
        FacetNode s = root.fwd(RDF.type).one();
        FacetNode y = s.bwd(RDF.type).one().constraints().exists().activate().end();

        s.chRoot();

        System.out.println(fq.root().availableValues().toConstructQuery());
        RDFDataMgr.write(System.out, fq.root().as(FacetNodeResource.class).state().getModel(), RDFFormat.TURTLE_PRETTY);


        System.out.println("--------------------------------------------");

        root.chRoot();

        System.out.println(fq.root().availableValues().toConstructQuery());
        RDFDataMgr.write(System.out, fq.root().as(FacetNodeResource.class).state().getModel(), RDFFormat.TURTLE_PRETTY);

        // Now try to change to root back to root;
    }
}
