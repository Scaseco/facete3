package org.hobbit.benchmark.faceted_browsing.v2;

import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.bgp.api.XFacetedQuery;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Range;

public class TestFacetedQueryVariableNamingBug {

    protected FacetedQuery fq;

    @Before
    public void beforeTest() {
        Model model = RDFDataMgr.loadModel("path-data.ttl");
        RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.create(model));

        Model dataModel = ModelFactory.createDefaultModel();
        XFacetedQuery facetedQuery = dataModel.createResource().as(XFacetedQuery.class);
        FacetedQueryImpl.initResource(facetedQuery);

        fq = FacetedQueryImpl.create(facetedQuery, conn);

        //FacetedQueryResource fq = FacetedQueryImpl.create(model, conn);
    }

    /**
     * Test to ensure that constraints on the same path are combined using OR (rather than AND)
     *
     */
    @Test
    public void testToStringConstraintDisjunction() {
        fq.root().fwd("http://www.agtinternational.com/ontologies/IoTCore#valueLiteral").one()
            .constraints()
                .range(Range.closed(38.47, 69.34)).activate();
        //fq.root().fwd(RDF.type).one().constraints().eq(RDFS.Class);

        String queryStr = "" + fq.focus().fwd().facets()
                .filter(Concept.parse("?p { FILTER(?p IN (<http://www.agtinternational.com/ontologies/IoTCore#valueLiteral>)) }"))
                .toConstructQuery();
        System.out.println(queryStr);
    }



    /**
     * Test case where it happens that a facet value relation has the same variable
     * in subject and object position
     *
     */
    @Test
    public void testPotentialRewriteBug() {
//
//		focus: <http://www.w3.org/ns/ssn/#hasValue>
//			facetPath: <>
//			reverse: false
//			negated: false
//			includeAbsent: false
//			pFilter: null
//			oFilter: null
//
//
//			TernaryRelation [s=?v_2, p=?p, o=?v_2, element={ ?v_1  <http://www.w3.org/ns/ssn/#hasValue>  ?v_2 .
//			  ?v_2  <http://www.agtinternational.com/ontologies/IoTCore#valueLiteral>  ?v_3
//			  FILTER ( ( ?v_3 >= "29.79"^^<http://www.w3.org/2001/XMLSchema#float> ) && ( ?v_3 <= "30.43"^^<http://www.w3.org/2001/XMLSchema#float> ) )
//			  BIND(<http://www.w3.org/ns/ssn/#hasValue> AS ?p)
//			}]

        fq.focus()
            .fwd("http://www.w3.org/ns/ssn/#hasValue").one().chFocus()
            .fwd("http://www.agtinternational.com/ontologies/IoTCore#valueLiteral").one()
//			.constraints()
//				.range(Range.closed(29.79f, 30.43f)).activate()
//			.end()
            ;

        String queryStr = "" + fq.focus().fwd()
                .facetValueCounts()
                .toConstructQuery();

        System.out.println(queryStr);
    }


//	DEBUG POINT FOCUS:
//		DEBUG POINT CONSTRAINT: ( ( "[<http://www.agtinternational.com/ontologies/IoTCore#valueLiteral>]" >= "38.47"^^xsd:double ) && ( "[<http://www.agtinternational.com/ontologies/IoTCore#valueLiteral>]" <= "69.34"^^xsd:double ) )
//		2019-01-13 01:23:56,782 [main] DEBUG org.aksw.facete.v3.impl.DataQueryImpl: After rewrite: SELECT DISTINCT  ?p
//		WHERE
//		  {   { ?v_1  <http://www.agtinternational.com/ontologies/IoTCore#valueLiteral>  ?v_2 ;
//		              ?p                    ?o
//		        FILTER ( ?v_2 >= "38.47"^^<http://www.w3.org/2001/XMLSchema#double> )
//		        FILTER ( ?p IN (<http://www.agtinternational.com/ontologies/IoTCore#valueLiteral>) )
//		        FILTER ( ?v_2 <= "69.34"^^<http://www.w3.org/2001/XMLSchema#double> )
//		        FILTER ( ?p NOT IN (<http://www.agtinternational.com/ontologies/IoTCore#valueLiteral>) )
//		      }
//		    UNION
//		      { { ?v_1  <http://www.agtinternational.com/ontologies/IoTCore#valueLiteral>  ?v_2
//		          BIND(<http://www.agtinternational.com/ontologies/IoTCore#valueLiteral> AS ?p)
//		        }
//		        FILTER ( ?p IN (<http://www.agtinternational.com/ontologies/IoTCore#valueLiteral>) )
//		      }
//		  }

}
