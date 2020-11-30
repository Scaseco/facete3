package org.aksw.facete3.app.shared.concept;

public interface SlotSource<P> {
    Slot<P> newSlot(/* R requester */);
}
