package org.hobbit.benchmark.faceted_browsing.v2.task_generator;

import io.reactivex.rxjava3.core.Flowable;



public interface Hierarchy<T> {
    Flowable<T> roots();
    Flowable<T> descendents();
    Flowable<T> children(Iterable<T> nodes);
    Flowable<T> parents(Iterable<T> nodes);
}
