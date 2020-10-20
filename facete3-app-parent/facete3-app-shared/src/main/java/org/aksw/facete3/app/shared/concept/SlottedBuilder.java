package org.aksw.facete3.app.shared.concept;

import java.util.function.Supplier;

/**
 * Interface where a <b>whole</b> is created from <b>parts</b>, whereas these parts
 * are requested from different contributors.
 *
 * Contributors request <b>slots</b> into which they can place a supplier for the part they wish to contribute.
 * Slots should be released via {@link Slot#close()}.
 *
 * The <b>whole</b> can be repeatedly assembled using {@link #build()} thereby reflecting the latest state of the parts.
 * During assembly null values on supplies and contributions are ignored.
 *
 * Thread safety depends on the implementation.
 *
 * @author raven
 *
 * @param <W> The type of the whole to be created
 * @param <P> The type of the parts that are contributed
 */
public interface SlottedBuilder<W, P> {
    interface Slot<P>
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


    Slot<P> newSlot();
    W build();
}
