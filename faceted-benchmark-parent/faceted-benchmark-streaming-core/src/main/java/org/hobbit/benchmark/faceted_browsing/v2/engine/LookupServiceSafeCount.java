package org.hobbit.benchmark.faceted_browsing.v2.engine;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.lookup.CountInfo;
import org.aksw.jena_sparql_api.lookup.LookupService;

import com.google.common.collect.Streams;

public class LookupServiceSafeCount<K>
	implements LookupService<K, CountInfo>
{
	protected LookupService<K, CountInfo> preCountService;
	protected LookupService<K, CountInfo> exactCountService;

	@Override
	public CompletableFuture<Map<K, CountInfo>> apply(Iterable<K> items) {
		return preCountService.apply(items).thenCompose(preMap -> {
			List<K> winners = preMap.entrySet().stream()
					.filter(e -> !e.getValue().isHasMoreItems())
					.map(Entry::getKey)
					.collect(Collectors.toList());
			
			return exactCountService.apply(winners).thenApply(exactMap -> {
				Map<K, CountInfo> r = Streams.stream(items)
					.collect(Collectors.toMap(item -> item, item -> exactMap.getOrDefault(
							item, preMap.getOrDefault(item, new CountInfo(0, false, null)))));
				return r;
			});
		});
	}
	
}
