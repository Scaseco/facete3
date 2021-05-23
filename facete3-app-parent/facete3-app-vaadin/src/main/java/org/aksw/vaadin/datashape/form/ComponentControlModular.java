package org.aksw.vaadin.datashape.form;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.aksw.vaadin.datashape.form.ComponentControls.ComponentControlSimple;
import org.apache.jena.shacl.sys.C;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.shared.Registration;

public class ComponentControlModular<T, C>
	implements ComponentControl<T, C>
{
	protected CompoundRegistration registration = new CompoundRegistrationImpl();
	protected CompoundConsumer<C> attach = new CompoundConsumerImpl<C>();
	
	// protected CompoundRunnable updaters = 

	/** The list of concrete components that will be attached to the component passed as argument to attach */
	//protected ObservableCollection<Function<C, Component>> attachers = new ObservableCollectionBase<>(new ArrayList<>());
	
	
	//protected Set<Component> components = new LinkedHashSet<>();
	protected Map<ComponentControl<T, C>, BiConsumer<C, ?>> componentToAttacher = new LinkedHashMap<>();
	
	
	public void add(Component component) {
		// componentToAttacher.put(ComponentControls.wrap(component), null);
		add(ComponentControls.wrap(component));
	}

	public void add(ComponentControl<T, C> component) {
		componentToAttacher.put(component, null);
	}

	
	public <X extends Component> void add(X component, BiConsumer<C, X> attacher) {
		componentToAttacher.put(new ComponentControlSimple<T, C>(component, null), attacher);
	}

	public <X extends Component> void add(X component, Consumer<X> updater) {
		componentToAttacher.put(new ComponentControlSimple<T, C>(component, () -> updater.accept(component)), null);
	}

	public <X extends Component> void add(X component, Consumer<X> updater, BiConsumer<C, X> attacher) {
		componentToAttacher.put(new ComponentControlSimple<T, C>(component, () -> updater.accept(component)), attacher);
	}

//	public <T> void add(ComponentControl<C> component, BiConsumer<C, T> attacher) {
//		componentToAttacher.put(component, attacher);
//	}

	public void add(Registration registration) {
		this.getRegistration().add(registration);
	}

	public void add(Runnable registration) {
		this.getRegistration().add(registration);
	}

	
	public CompoundRegistration getRegistration() {
		return registration;
	}

	public CompoundConsumer<C> getAttach() {
		return attach;
	}

//	public ObservableCollection<Function<C, Component>> getAttachers() {
//		return attachers;
//	}

//	public Set<ComponentControl<C>> getComponents() {
//		return componentToAttacher.keySet();
//	}

	public Map<ComponentControl<T, C>, BiConsumer<C, ?>> getComponentToAttacher() {
		return componentToAttacher;
	}
	
	@Override
	public void detach() {
		for (Entry<ComponentControl<T, C>, BiConsumer<C, ?>> e : componentToAttacher.entrySet()) {
			ComponentControl<T, C> cc = e.getKey();

			cc.detach();
		}		

//		HasComponents parent;
//		for (Component c : components) {
//			parent.remove(c);
//		}
	}

	@Override
	public void attach(C target) {
		// FIXME Call the attacher
		for (Entry<ComponentControl<T, C>, BiConsumer<C, ?>> e : componentToAttacher.entrySet()) {
			ComponentControl<T, C> cc = e.getKey();
			
			BiConsumer bc = e.getValue();
			
			cc.attach(target);
			
			if (bc != null) {
				Component c = ((ComponentControlSimple)cc).getComponent();
				bc.accept(target, c);
			}
		}
	}

//	@Override
//	public void refresh(T state) {
//		
//	}

	@Override
	public void close() {
		registration.remove();

		for (Entry<ComponentControl<T, C>, BiConsumer<C, ?>> e : componentToAttacher.entrySet()) {
			ComponentControl<T, C> cc = e.getKey();
			
			cc.close();
		}		
	}

	@Override
	public Map<Object, ComponentControl<?, ?>> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void refresh(T state) {
		for (Entry<ComponentControl<T, C>, BiConsumer<C, ?>> e : componentToAttacher.entrySet()) {
			ComponentControl<T, C> cc = e.getKey();

			cc.refresh(state);
		}		
	}
}
