package org.hobbit.benchmark.faceted_browsing.v2.engine;

import java.util.Map.Entry;

import org.aksw.jena_sparql_api.lookup.LookupService;

import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;

public class LookupServiceSafeCount<K>
    implements LookupService<K, Range<Long>>
{
    protected LookupService<K, Range<Long>> preCountService;
    protected LookupService<K, Range<Long>> exactCountService;
    @Override
    public Flowable<Entry<K, Range<Long>>> apply(Iterable<K> arg0) {
        // TODO Auto-generated method stub
        return null;
    }

//	@Override
//	public Flowable<Entry<K, Range<Long>>> apply(Iterable<K> items) {
//		// TODO buffer by condition if there are too many items without exact count
//		return preCountService.apply(items).toMap(Entry::getKey, Entry::getValue).flatMap(preMap -> {
//			List<K> winners = preMap.entrySet().stream()
//					.filter(e -> !CountUtils.toCountInfo(e.getValue()).isHasMoreItems())
//					.map(Entry::getKey)
//					.collect(Collectors.toList());
//
//			return exactCountService.apply(winners).toMap(Entry::getKey, Entry::getValue)
//					.flatMap(exactMap -> {
//					Map<K, Range<Long>> r = Streams.stream(items)
//						.collect(Collectors.toMap(item -> item, item -> exactMap.getOrDefault(
//								item, preMap.getOrDefault(item, Range.singleton(0l)))));
//					return Flowable.fromIterable(exactMap.entrySet());
//			});
//		}).toFlowable();
//	}


}
