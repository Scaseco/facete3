package org.hobbit.benchmark.faceted_browsing.v2;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.sparql_path.core.algorithm.ConceptPathFinder;
import org.aksw.jenax.dboe.dataset.impl.DatasetGraphQuadsImpl;
import org.aksw.jenax.sparql.path.SimplePath;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.aksw.jenax.sparql.rx.op.FlowOfQuadsOps;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Quad;
import org.junit.Test;

import com.google.common.collect.Streams;

import io.reactivex.rxjava3.core.Flowable;

public class TestConceptPathFinder {

    public static String substituteSpaceWithTInTimestamps(String str) {
        Pattern p = Pattern.compile("(\\d+-\\d{1,2}-\\d{1,2}) (\\d{1,2}:\\d{1,2}:\\d{1,2})");
        String result = p.matcher(str).replaceAll("$1T$2");
        return result;
    }

    // @Test
    public void testTrigDateFormat() {
        String data = "<http://www.example.org/> { <http://www.agtinternational.com/resources/livedData#House39274.device6_SmartMeter_Power.Observation_0> <http://www.w3.org/ns/ssn/#observationResultTime> \"2018-10-30 09:41:53\"^^<http://www.w3.org/2001/XMLSchema#dateTime> . }\n";
        data += data;
        data = substituteSpaceWithTInTimestamps(data);
        System.out.println("Data:\n" + data);
//		{
//			Model m = ModelFactory.createDefaultModel();
//			RDFDataMgr.read(m, new ByteArrayInputStream("<http://www.agtinternational.com/resources/livedData#House39274.device6_SmartMeter_Power.Observation_0> <http://www.w3.org/ns/ssn/#observationResultTime> \"2018-10-30T09:41:53\"^^<http://www.w3.org/2001/XMLSchema#dateTime> .".getBytes()), Lang.NTRIPLES);
//		}
        Dataset d = DatasetFactory.create();
        RDFDataMgr.read(d, new ByteArrayInputStream(data.getBytes()), Lang.TRIG);
        System.out.println("Data in model:");
        RDFDataMgr.write(System.out, d, RDFFormat.TRIG);
    }

    @Test
    public void testTrigStream() {
        Iterable<Quad> i = () -> RDFDataMgr.createIteratorQuads(
                TestConceptPathFinder.class.getClassLoader().getResourceAsStream("rdf-stream.trig"), Lang.TRIG,
                "http://www.example.org/");

        Flowable<Dataset> eventStream = Flowable.fromIterable(i)
                .compose(FlowOfQuadsOps.groupToList())
                .map(Entry::getValue)
                .map(DatasetGraphQuadsImpl::create).map(DatasetFactory::wrap);

        Dataset dg = DatasetFactory.wrap(new DatasetGraphQuadsImpl());// DatasetFactory.create();

        eventStream.forEach(d -> {
            System.out.println("Got event");
            RDFDataMgr.write(System.out, d, RDFFormat.TRIG);

            Streams.stream(d.asDatasetGraph().find()).forEach(dg.asDatasetGraph()::add);
        });

//		while(it.hasNext()) {
//			Quad quad = it.next();
//			dg.asDatasetGraph().add(quad);
//			//dg.getNamedModel(quad.getGraph().getURI()).add
//			System.out.println(quad);
//		}

        System.out.println("Roundtrip result");
        RDFDataMgr.write(System.out, dg, RDFFormat.TRIG);
        // DatasetFactory.cr
    }

    // @Test
    public void testReverseProperties() {
        Model model = RDFDataMgr.loadModel("simple-reverse-path.ttl");

        // The source concept denotes the set of resources matching the facet
        // constraints
        UnaryRelation src = Concept.create("VALUES (?s) { (eg:a) }", "s", PrefixMapping.Extended);
        UnaryRelation target = Concept.create("?s eg:p3 ?o", "s", PrefixMapping.Extended);

        // The target concept denotes the set of resources carrying numeric properties

        // TODO We need to wire up pathPattern with the path finder
        List<SimplePath> paths = ConceptPathFinder.findPaths(FluentQueryExecutionFactory.from(model).create(), src,
                target, 100, 100);

        System.out.println(paths);
        // return paths;
    }
}
