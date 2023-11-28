package org.aksw.vaadin.datashape.form;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import org.aksw.commons.collection.observable.ObservableCollection;

import com.google.common.primitives.Ints;
import com.vaadin.flow.data.provider.AbstractDataProvider;
import com.vaadin.flow.data.provider.Query;

public class DataProviderFromObservableCollection<T, F>
    extends AbstractDataProvider<T, F>
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected ObservableCollection<T> observableCollection;
    protected BiPredicate<T, F> matches;

    public DataProviderFromObservableCollection(
            ObservableCollection<T> observableCollection,
            BiPredicate<T, F> matches) {
        super();
        this.observableCollection = observableCollection;
        this.matches = matches;

        observableCollection.addPropertyChangeListener(event -> {
            this.refreshAll();
        });
    }


    @Override
    public int size(Query<T, F> query) {
        Optional<F> optFilter = query.getFilter();
        F filter = optFilter.orElse(null);

        int result = optFilter.isPresent()
                ? Ints.saturatedCast(observableCollection.stream()
                        .filter(item -> matches.test(item, filter)).count())
                : observableCollection.size();

        return result;
    }

    @Override
    public Stream<T> fetch(Query<T, F> query) {
        Stream<T> result = observableCollection.stream();

        Optional<F> optFilter = query.getFilter();
        if (optFilter.isPresent()) {
            F filter = optFilter.get();

            result = result.filter(item -> matches.test(item, filter));
        }

        result
            .skip(query.getOffset())
            .limit(query.getLimit());

        return result;
    }

    @Override
    public boolean isInMemory() {
        return true;
    }
}
