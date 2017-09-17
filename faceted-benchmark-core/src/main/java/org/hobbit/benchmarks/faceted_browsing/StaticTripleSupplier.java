package org.hobbit.benchmarks.faceted_browsing;

import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import com.google.common.collect.Streams;

public class StaticTripleSupplier
{
    protected String resourceName;

    public StaticTripleSupplier(String resourceName) {
        super();
        this.resourceName = resourceName;
    }

    public Stream<Triple> generate() {
        TypedInputStream in = RDFDataMgr.open(resourceName);
        Lang lang = RDFDataMgr.determineLang(resourceName, in.getContentType(), Lang.TURTLE);
        Iterator<Triple> it = RDFDataMgr.createIteratorTriples(in, lang, "http://example.org/");

        return Streams.stream(it);
    }
}
