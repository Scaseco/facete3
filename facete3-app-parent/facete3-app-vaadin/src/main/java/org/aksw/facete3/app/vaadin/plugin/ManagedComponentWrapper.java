package org.aksw.facete3.app.vaadin.plugin;

import com.vaadin.flow.component.Component;

public class ManagedComponentWrapper
    implements ManagedComponent
{
    protected ManagedComponent delegate;

    public ManagedComponent getDelegate() {
        return delegate;
    }

    public ManagedComponentWrapper(ManagedComponent delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public Component getComponent() {
        return getDelegate().getComponent();
    }

    @Override
    public void refresh() {
        getDelegate().refresh();
    }

    @Override
    public void close() {
        getDelegate().close();
    }

}
