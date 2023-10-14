package org.aksw.facete.v3.component.data_tree;

import java.util.function.Function;

import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.sparql.fragment.api.Fragment2;

public interface Slice {
    void setFilter(Fragment filter);
    void setFilter(Function<? super Fragment, ? extends Fragment> filterFn);

    public Column addPredicate(String predicate, Fragment2 binaryRelation);
}
