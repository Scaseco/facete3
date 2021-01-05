package org.aksw.vaadin.datashape.form;

import java.util.function.Consumer;

import org.aksw.facete3.app.vaadin.plugin.ManagedComponent;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;

public class ComponentControls {

	
	public static <T, C> ComponentControl<C> create(Class<C> parentContainerClass, T item, Consumer<ComponentControlModular<C>> builder) {
		
		ComponentControlModular<C> result = new ComponentControlModular<C>();
		
		builder.accept(result);
		
		return result;
	}
	
	public static class ComponentControlSimple<C>
		implements ComponentControl<C>
	{

		protected Component component;
		
		public ComponentControlSimple(Component component) {
			super();
			this.component = component;
		}

		public Component getComponent() {
			return component;
		}
		
		@Override
		public void detach() {
		}

		@Override
		public void attach(C target) {
			((HasComponents)target).add(component);
		}

		@Override
		public void refresh() {
		}

		@Override
		public void close() {
			// ((HasComponents)target).add(component);
			// component.de
		}			
	};
	
	public static <C> ComponentControl<C> wrap(Component component) {
		return new ComponentControlSimple<C>(component);
	}

	
	public static <C> ComponentControl<C> wrap(Class<C> parentContainer, ManagedComponent managedComponent) {
		// managedComponent.getComponent().getParent().get().
		// HasComponents x;
		//x.remove(managedComponent.getComponent());
		return null;
	}
	
//	public <C extends Component> ComponentControl<C, ?> wrap(C component) {
//	}

	
	public static class ComponentControlWrapper<C, T>
		implements ComponentControl<T>
	{
		protected Component wrappedComponent;
		
		@Override
		public void detach() {
		}

		@Override
		public void attach(T target) {
		}

		@Override
		public void refresh() {
		}

		@Override
		public void close() {
		}		
	}
	
}
