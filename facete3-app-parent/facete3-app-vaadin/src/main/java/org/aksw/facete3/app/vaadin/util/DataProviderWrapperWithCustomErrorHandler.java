package org.aksw.facete3.app.vaadin.util;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataProviderWrapper;
import com.vaadin.flow.data.provider.Query;


public class DataProviderWrapperWithCustomErrorHandler<T, F>
    extends DataProviderWrapper<T, F, F>
{
    private static final long serialVersionUID = 1L;

    protected Consumer<? super Throwable> customErrorHandler;

    public DataProviderWrapperWithCustomErrorHandler(
            DataProvider<T, F> dataProvider,
            Consumer<? super Throwable> customErrorHandler) {
        super(dataProvider);
        this.customErrorHandler = Objects.requireNonNull(customErrorHandler, "Error handler must not be null");
    }

    @Override
    public int size(Query<T, F> t) {
        int result;
        try {
            result = super.size(t);
        } catch (Exception e) {
            customErrorHandler.accept(e);
            result = 0;
        }
        return result;
    }

    @Override
    public Stream<T> fetch(Query<T, F> t) {
        Stream<T> result;
        try {
            result = super.fetch(t);
        } catch (Exception e) {
            customErrorHandler.accept(e);
            result = Stream.empty();
        }
        return result;
    }

    @Override
    protected F getFilter(Query<T, F> query) {
        return query.getFilter().orElse(null);
    }
}
