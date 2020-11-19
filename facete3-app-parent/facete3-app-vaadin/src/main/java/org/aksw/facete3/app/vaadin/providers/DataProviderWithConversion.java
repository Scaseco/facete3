package org.aksw.facete3.app.vaadin.providers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.jsonldjava.shaded.com.google.common.collect.Iterables;
import com.vaadin.flow.data.provider.DataChangeEvent;
import com.vaadin.flow.data.provider.DataChangeEvent.DataRefreshEvent;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataProviderListener;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.shared.Registration;

public class DataProviderWithConversion<O, F, I>
    implements DataProvider<O, F>
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected DataProvider<I, F> delegate;
    protected Function<? super List<I>, ? extends List<O>> bulkConvert;
    protected Function<? super O, ? extends I> convertBack;

    protected DataProviderWithConversion(
            DataProvider<I, F> delegate,
            Function<? super List<I>, ? extends List<O>> bulkConvert,
            Function<? super O, ? extends I> convertBack) {
        this.delegate = delegate;
        this.bulkConvert = bulkConvert;
        this.convertBack = convertBack;
    }


    public static <O, F, I> DataProvider<O, F> wrap(
            DataProvider<I, F> delegate,
            Function<? super List<I>, ? extends List<O>> bulkConvert,
            Function<? super O, ? extends I> convertBack
            ) {
        return new DataProviderWithConversion<>(delegate, bulkConvert, convertBack);
    }

    @Override
    public boolean isInMemory() {
        return delegate.isInMemory();
    }

    @Override
    public int size(Query<O, F> query) {
        Query<I, F> delegateQuery = convertQuery(query, null);
        int result = delegate.size(delegateQuery);
        return result;
    }

    @Override
    public Stream<O> fetch(Query<O, F> query) {
        Query<I, F> delegateQuery = convertQuery(query, null);

        List<I> rawItems = delegate.fetch(delegateQuery)
                .collect(Collectors.toList());

        List<O> items = bulkConvert.apply(rawItems);
        return items.stream();
    }


    public static <I, O, F> Query<O, F> convertQuery(Query<I, F> query, Comparator<O> inMemorySorting) {
        Query<O, F> result = new Query<>(
                query.getOffset(), query.getLimit(), query.getSortOrders(),
                inMemorySorting, query.getFilter().orElse(null));
        return result;
    }

    @Override
    public Registration addDataProviderListener(DataProviderListener<O> listener) {
        Registration result = delegate.addDataProviderListener(event -> {
            DataChangeEvent<O> newEvent = null;
            if (event instanceof DataRefreshEvent) {
                DataRefreshEvent<I> dre = (DataRefreshEvent<I>)event;
                List<O> convertedItems = bulkConvert.apply(Collections.singletonList(dre.getItem()));
                O newItem = Iterables.getOnlyElement(convertedItems);
                newEvent = new DataRefreshEvent<O>(this, newItem);
            } else {
                newEvent = new DataChangeEvent<O>(this);
            }

            listener.onDataChange(newEvent);
        });

        return result;
    }


    @Override
    public void refreshItem(O item) {
        I delegateItem = convertBack.apply(item);
        delegate.refreshItem(delegateItem);
    }


    @Override
    public void refreshAll() {
        delegate.refreshAll();
    }
}


