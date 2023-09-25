package org.aksw.jena_sparql_api.utils.views.map;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.aksw.jena_sparql_api.rdf.collections.ConverterFromNodeMapper;
import org.aksw.jena_sparql_api.rdf.collections.ConverterFromNodeMapperAndModel;
import org.aksw.jena_sparql_api.rdf.collections.NodeMappers;
import com.google.common.collect.Iterables;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

import com.google.common.base.Stopwatch;

public class TestMapFromProperty {
    //@Test
    public void testMapFromProperty() {
        Model model = ModelFactory.createDefaultModel();
        Resource s = model.createResource();


        // For 1mio items, the hash map is 6x times faster than model on i7-7700HQ CPU @ 2.80GHz

        Map<RDFNode, Resource> rawMap = new MapFromResourceUnmanaged(s, MapVocab.entry, RDFS.label);

        //Map<RDFNode, Resource> rawMap = new HashMap<>();

        Map<String, Resource> tmpMap = new MapFromKeyConverter<>(
                rawMap,
                new ConverterFromNodeMapperAndModel<>(model, RDFNode.class, new ConverterFromNodeMapper<>(NodeMappers.uriString)));

        Map<String, String> map = new MapFromValueConverter<>(
                tmpMap, new ConverterFromNodeMapperAndModel<>(model, Resource.class, new ConverterFromNodeMapper<>(NodeMappers.uriString)));


        // TODO We need to extract the value attribute of the entry resource


        map.put("http://www.example.org/key1", "http://www.example.org/entry1");

        for(int i = 0; i < 10; ++i) {
            benchmark(map, x -> "http://www.example.org/key" + x, x -> "http://www.example.org/entry" + x);
        }


        Iterable<?> it = Iterables.limit(rawMap.entrySet(), 10);
        for(Object e : it) {
            System.out.println(e);
        }
        //RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
    }


    public static <K, V> void benchmark(Map<K, V> map, Function<Long, K> keygen, Function<Long, V> valgen) {
        Stopwatch sw = Stopwatch.createStarted();
        for(long i = 0; i < 1000000; ++i) {
            K key = keygen.apply(i);
            V val = valgen.apply(i);

            map.put(key, val);
        }
        System.out.println("Elapsed (s): " + sw.stop().elapsed(TimeUnit.NANOSECONDS) / 1000000000.0);
    }
}
