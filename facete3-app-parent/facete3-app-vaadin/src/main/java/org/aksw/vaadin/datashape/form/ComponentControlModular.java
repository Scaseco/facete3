package org.aksw.vaadin.datashape.form;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import org.aksw.vaadin.datashape.form.ComponentControls.ComponentControlSimple;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.shared.Registration;

public class ComponentControlModular<C>
	implements ComponentControl<C>
{
	protected CompoundRegistration registration = new CompoundRegistrationImpl();
	protected CompoundConsumer<C> attach = new CompoundConsumerImpl<C>();
	

	/** The list of concrete components that will be attached to the component passed as argument to attach */
	//protected ObservableCollection<Function<C, Component>> attachers = new ObservableCollectionBase<>(new ArrayList<>());
	
	
	//protected Set<Component> components = new LinkedHashSet<>();
	protected Map<ComponentControl<C>, BiConsumer<C, ?>> componentToAttacher = new LinkedHashMap<>();
	
	
	public void add(Component component) {
		// componentToAttacher.put(ComponentControls.wrap(component), null);
		add(ComponentControls.wrap(component));
	}

	public void add(ComponentControl<C> component) {
		componentToAttacher.put(component, null);
	}

	
	public <T extends Component> void add(T component, BiConsumer<C, T> attacher) {
		componentToAttacher.put(new ComponentControlSimple<C>(component), attacher);
	}

//	public <T> void add(ComponentControl<C> component, BiConsumer<C, T> attacher) {
//		componentToAttacher.put(component, attacher);
//	}

	public void add(Registration registration) {
		this.getRegistration().add(getRegistration());
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

	public Map<ComponentControl<C>, BiConsumer<C, ?>> getComponentToAttacher() {
		return componentToAttacher;
	}
	
	@Override
	public void detach() {
//		HasComponents parent;
//		for (Component c : components) {
//			parent.remove(c);
//		}
	}

	@Override
	public void attach(C target) {
		// FIXME Call the attacher
		for (Entry<ComponentControl<C>, BiConsumer<C, ?>> e : componentToAttacher.entrySet()) {
			ComponentControl<C> cc = e.getKey();
			
			BiConsumer bc = e.getValue();
			
			cc.attach(target);
			
			if (bc != null) {
				Component c = ((ComponentControlSimple)cc).getComponent();
				bc.accept(target, c);
			}
		}
	}

	@Override
	public void refresh() {
		
	}

	@Override
	public void close() {
		registration.remove();
	}
}
