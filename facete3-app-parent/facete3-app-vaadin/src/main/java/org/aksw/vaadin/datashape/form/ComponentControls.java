package org.aksw.vaadin.datashape.form;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

import org.aksw.vaadin.common.component.managed.ManagedComponent;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;

public class ComponentControls {


    public static <T, C> ComponentControl<T, C> create(T item, Consumer<ComponentControlModular<T, C>> builder) {

        ComponentControlModular<T, C> result = new ComponentControlModular<T, C>();

        builder.accept(result);

        return result;
    }

    public static class ComponentControlSimple<T, C>
        implements ComponentControl<T, C>
    {

        protected Component component;
        protected Runnable updateAction;

        public ComponentControlSimple(Component component, Runnable updateAction) {
            super();
            this.component = component;
            this.updateAction = updateAction;
        }

        public Component getComponent() {
            return component;
        }

        @Override
        public void detach() {
            Component parent = component.getParent().orElse(null);

            if (parent != null && parent instanceof HasComponents) {
                ((HasComponents)parent).remove(component);
            }
        }

        @Override
        public void attach(C target) {
            ((HasComponents)target).add(component);
        }

        @Override
        public void refresh(T state) {
            // updateAction.accept(state);
            if (updateAction != null) {
                updateAction.run();
            }
        }

        @Override
        public void close() {
            detach();
            // ((HasComponents)target).add(component);
            // component.de
        }

        @Override
        public Map<Object, ComponentControl<?, ?>> getChildren() {
            /// return Collections.singletonMap(null, this);
            return Collections.emptyMap();
        }
    };

    public static <T, C> ComponentControl<T, C> wrap(Component component) {
        return new ComponentControlSimple<T, C>(component, null);
    }


    public static <T, C> ComponentControl<T, C> wrap(Class<C> parentContainer, ManagedComponent managedComponent) {
        // managedComponent.getComponent().getParent().get().
        // HasComponents x;
        //x.remove(managedComponent.getComponent());
        return null;
    }

//	public <C extends Component> ComponentControl<C, ?> wrap(C component) {
//	}


    public static class ComponentControlWrapper<T, C>
        implements ComponentControl<T, C>
    {
        protected Component wrappedComponent;

        @Override
        public void detach() {
        }

        @Override
        public void attach(C target) {
        }

        @Override
        public void refresh(T state) {
        }

        @Override
        public void close() {
        }

        @Override
        public Map<Object, ComponentControl<?, ?>> getChildren() {
            return null;
        }
    }

}
