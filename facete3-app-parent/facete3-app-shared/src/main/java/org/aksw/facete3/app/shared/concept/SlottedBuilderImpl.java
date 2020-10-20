package org.aksw.facete3.app.shared.concept;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SlottedBuilderImpl<W, P>
    implements SlottedBuilder<W, P>
{
    /**
     * The assembler that assembles the 'whole' from the 'parts'
     */
    protected Function<Collection<P>, ? extends W> assembler;

    protected Set<Slot<P>> slots = new LinkedHashSet<>();

    public SlottedBuilderImpl(Function<Collection<P>, ? extends W> assembler) {
        super();
        this.assembler = assembler;
    }

    public class SlotImpl
        implements Slot<P>
    {
        protected Supplier<P> supplier;

        @Override
        public Supplier<P> getSupplier() {
            return supplier;
        }

        @Override
        public Slot<P> setSupplier(Supplier<P> partSupplier) {
            this.supplier = partSupplier;
            return this;
        }

        @Override
        public void close() {
            slots.remove(this);
        }
    }

    @Override
    public Slot<P> newSlot() {
        Slot<P> result = new SlotImpl();
        slots.add(result);
        return result;
    }

    @Override
    public W build() {
        Collection<P> parts = slots.stream()
            .filter(slot -> slot != null)
            .map(Slot::getSupplier)
            .filter(supplier -> supplier != null)
            .map(Supplier::get)
            .filter(part -> part != null)
            .collect(Collectors.toList());

        W result = assembler.apply(parts);
        return result;
    }


    public static <W, P> SlottedBuilder<W, P> create(Function<Collection<P>, ? extends W> assembler) {
        return new SlottedBuilderImpl<W, P>(assembler);
    }
}
