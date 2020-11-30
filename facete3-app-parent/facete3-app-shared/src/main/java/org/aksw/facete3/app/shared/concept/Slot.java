package org.aksw.facete3.app.shared.concept;

import java.util.function.Supplier;

public interface Slot<P>
    extends AutoCloseable
{
    Slot<P> setSupplier(Supplier<P> partSupplier);
    Supplier<P> getSupplier();

    default Slot<P> set(P part) {
        return setSupplier(() -> part);
    }

    @Override
    void close();
}