package org.aksw.facete.v3.component.data_tree;

import org.aksw.facete.v3.bgp.api.BinaryRelation;

public interface Column {

    void sortBefore(Column column);
    void sortAfter(Column column);
    void dispose();
    void disable();



    /**
     * Add a subcolumn based on the given predicate.
     * This makes this heading the parent of the sub column.
     *
     *
     * @param predicate
     * @param isReverse
     * @return
     */
    public Column addSubColumn(String predicate, boolean isReverse);

    public Column addSubColumn(String exposedPredicate, BinaryRelation relation, boolean isReverse);

}
