package org.aksw.vaadin.datashape.form;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.vaadin.common.component.managed.ManagedComponent;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasOrderedComponents;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;

interface ListComponentControl<T, C>
    extends ComponentControl<Collection<T>, C>
{

}

class ComponentControlTransform<I, O, C>
    implements ComponentControl<I, C>
{
    protected ComponentControl<O, C> delegate;
    protected Function<? super I, ? extends O> convertState;

    public ComponentControlTransform(ComponentControl<O, C> delegate, Function<? super I, ? extends O> convertState) {
        super();
        this.delegate = delegate;
        this.convertState = convertState;
    }

    @Override
    public Map<Object, ComponentControl<?, ?>> getChildren() {
        return delegate.getChildren();
    }

    @Override
    public void detach() {
        delegate.detach();
    }

    @Override
    public void attach(C target) {
        delegate.attach(target);
    }

    @Override
    public void refresh(I state) {
        O actualState = convertState.apply(state);
        delegate.refresh(actualState);
    }

    @Override
    public void close() {
        delegate.close();
    }
}


/**
 * A class that keeps a list of items and a list of components created from these items in sync
 * upon calling .refresh().
 *
 * This variant would track changes to a target layout's HTML element using glibc proxy mechanism.
 * This means that one item can be linked to multiple components.
 *
 * @author raven
 *
 * @param <T>
 */
public class ListBindingSupport2<T, F, C>
    implements ListComponentControl<T, C>
{

//	public static class ItemState<C> {
//		protected CompoundConsumer<C> appender;
//		protected CompoundRegistration registration;
//		protected CompoundRunnable refreshActions;
//
//		public ItemState(CompoundConsumer<C> appender, CompoundRegistration registration) {
//			super();
//			this.appender = appender;
//			this.registration = registration;
//		}
//
//		public CompoundConsumer<C> getAppender() {
//			return appender;
//		}
//
//		public CompoundRegistration getRegistration() {
//			return registration;
//		}
//	}

//	protected S source;
//	protected Function<? super S, ? extends Collection<? extends T>> itemSupplier;

    protected Collection<T> currentItems;
    protected Map<Object, ComponentControl<T, C>> keyToState = new LinkedHashMap<>();

    protected DataProvider<T, F> dataProvider;

    protected Function<? super T, ?> itemToKey;
    protected Function<? super T, ? extends ComponentControl<T, C>> itemToAction;

    protected C targetLayout;

    protected Registration dataProviderRegistration;


//	public ConfigurableFilterDataProvider<T, F> getDataProvider() {
//		return (ConfigurableFilterDataProvider<T, F>)dataProvider;
//	}

//	public static <T, C> ListBindingSupport2<T, SerializablePredicate<T>, C> create(
//			C target,
//			Supplier<Collection<T>> items, BiConsumer<T, ComponentControlModular<T, C>> componentBuilder) {
//
//		return create(target, items, item -> item, componentBuilder);
//	}

//	public static <S, T, C> ListBindingSupport2<T, SerializablePredicate<T>, C> create(
//			C target,
//			S source,
//			Function<S, Collection<T>> items, BiConsumer<T, ComponentControlModular<T, C>> componentBuilder) {
//
//		return create(target, items, item -> item, componentBuilder);
//	}

    public static <T, F, C> ListBindingSupport2<T, F, C> create(
            C target,
            DataProvider<T, F> dataProvider,
            BiConsumer<T, ComponentControlModular<T, C>> componentBuilder) {

        return new ListBindingSupport2<T, F, C>(target, dataProvider, item -> item, item -> {
            ComponentControlModular<T, C> tmp = new ComponentControlModular<>();
            componentBuilder.accept(item, tmp);
            return tmp;
            // ComponentControls.create(item, componentBuilder));
        });
    }


    public static <T, C> ListBindingSupport2<T, SerializablePredicate<T>, C> create(
            C target,
            Collection<T> items, BiConsumer<T, ComponentControlModular<T, C>> componentBuilder) {

        return create(target, items, item -> item, componentBuilder);
    }


    public static <T, C> ListBindingSupport2<T, SerializablePredicate<T>, C> create(
            C target,
            Collection<T> items,
            Function<? super T, ?> itemToKey,
            BiConsumer<T, ComponentControlModular<T, C>> componentBuilder) {

        ListBindingSupport2<T, SerializablePredicate<T>, C> result = new ListBindingSupport2<>(
                target,
                new ListDataProvider<T>(items),
                itemToKey, item -> {
                    ComponentControlModular<T, C> tmp = new ComponentControlModular<>();
                    componentBuilder.accept(item, tmp);
                    return tmp;
                    // ComponentControls.create(item, componentBuilder));
                });

        return result;
    }

    public ListBindingSupport2(
            C targetLayout,
            DataProvider<T, F> dataProvider,
            Function<? super T, ?> itemToKey,
            Function<? super T, ? extends ComponentControl<T, C>> itemToAction) {
        super();
        this.targetLayout = targetLayout;
        this.dataProvider = dataProvider;
        this.itemToKey = itemToKey;
        this.itemToAction = itemToAction;

        refresh();
        enableSync(true);
    }

    @Override
    public Map<Object, ComponentControl<?, ?>> getChildren() {
        return (Map)keyToState;
    }


    public synchronized void enableSync(boolean onOrOff) {
        if (onOrOff && dataProviderRegistration == null) {
            dataProviderRegistration = dataProvider.addDataProviderListener(event -> {
                refresh();
            });
        } else if (dataProviderRegistration != null){
            dataProviderRegistration.remove();
            dataProviderRegistration = null;
        }
    }

    public static <T> ListBindingSupport<T> create(
            DataProvider<T, String> dataProvider,
            Function<? super T, ? extends ManagedComponent> itemToComponent,
            Function<? super T, ?> itemToKey,
            HasOrderedComponents targetLayout) {

        return new ListBindingSupport<T>(dataProvider, itemToKey, itemToComponent, targetLayout);
    }

    /** Constructor where items serve directly as keys */
    public static <T> ListBindingSupport<T> create(
            DataProvider<T, String> dataProvider,
            Function<? super T, ? extends ManagedComponent> itemToComponent,
            HasOrderedComponents targetLayout) {

        return create(dataProvider, itemToComponent, item -> item, targetLayout);
    }



    public void refresh(Collection<T> newItems) {
        Set<Object> newKeys = newItems.stream().map(itemToKey).collect(Collectors.toSet());

        // Remove components not among the new items
        Iterator<Entry<Object, ComponentControl<T, C>>> it = keyToState.entrySet().iterator();
        //for (Entry<Object, ComponentControl<T, C>> e : keyToState.entrySet()) {
        while (it.hasNext()) {
            Entry<Object, ComponentControl<T, C>> e = it.next();
            Object key = e.getKey();
            ComponentControl<T, C> componentControl = e.getValue();
            if (!newKeys.contains(key)) {
                componentControl.close();
                // keyToState.remove(key);
                it.remove();
            }
        }

        // Detach components that are out of order
        // FIXME Right now we simply detach all
        for (ComponentControl<T, C> componentControl : keyToState.values()) {
            componentControl.detach();
        }


        // Add the new items at the appropriate indices ; create new items as needed
//		int i = 0;
        for (T item : newItems) {
            Object key = itemToKey.apply(item);
            ComponentControl<T, C> componentControl = keyToState.get(key);
            if (componentControl == null) {
                componentControl = itemToAction.apply(item);
                keyToState.put(key, componentControl);
            }

            componentControl.refresh(item);
            componentControl.attach(targetLayout);
//			++i;
        }

        currentItems = newItems;
    }

    /**
     * Perform dirty checking and update as needed.
     * Concretely, obtains a new list of items and removes components no longer backed by an item
     * and creates new components for newly encountered items.
     *
     * */
    public synchronized void refresh() {
        Query<T, F> query = new Query<>();
        List<T> newItems = dataProvider.fetch(query).collect(Collectors.toList());

        refresh(newItems);
    }


    @Override
    public void detach() {
        //keyToState.values().forEach(item -> targetLayout.re);
    }


    @Override
    public void attach(C target) {

        Component ctgt = (Component)target;
        HasComponents tgt = (HasComponents)target;

        List<Component> children = ctgt.getChildren().collect(Collectors.toList());
        int currentIdx = 0;

        for (ComponentControl<T, C> cc : keyToState.values()) {
            // TODO For each key we need the current list of components and the new one


            cc.attach(target);
        }

    }


    @Override
    public void close() {
        for (ComponentControl<T, C> cc : keyToState.values()) {
            cc.close();
        }
    }


//	public static class TrackingInvocationHandler
//		implements MethodInterceptor
//	{
//		protected Set<Element> elements = new LinkedHashSet<>();
//	}
//	public static void createProxyForElement(Element elt) {
//		// elt.appendChild(null)
//		// elt.removeChild(null)
//	}
//
//	public static void createProxyForHasComponents(Class<? extends HasComponents> clazz) {
//	    Enhancer enhancer = new Enhancer();
//	    if(clazz.isInterface()) {
//	    } else {
//	        enhancer.setSuperclass(clazz);
//	    }
//
//
//	    Method addMethod = clazz.getMethod("add", Component[].class);
//
//	    enhancer.setCallback(new MethodInterceptor() {
//	        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
//	        	if (method.equals(addMethod)) {
//
//	        	} else {
//	        		proxy.invokeSuper(obj, args);
//	        	}
//	        }
//	    });
//
//	    // enhancer.crea
//	    // o = enhancer.create(argTypes, argValues);
//	}
}

//public class ListBindingSupport2<T, L extends HasComponents> {
//	protected List<T> currentItems;
//
//	protected ListMultimap<Object, ManagedComponent> keyToView = ArrayListMultimap.create();
//
//	protected DataProvider<T, String> dataProvider;
//
//	protected Function<? super T, ?> itemToKey;
//	//protected Function<? super T, ? extends ManagedComponent> itemToComponent;
//
//	protected BiConsumer<? super T, ? super L> itemRenderer;
//
//
//	// protected HasOrderedComponents<?> targetLayout;
//	protected L targetLayout;
//
//	protected Registration dataProviderRegistration;
//
//	public ListBindingSupport2(
//			DataProvider<T, String> dataProvider,
//			Function<? super T, ?> itemToKey,
//			Function<? super T, ? extends ManagedComponent> itemToComponent,
//			HasOrderedComponents<?> targetLayout) {
//		super();
//		this.dataProvider = dataProvider;
//		this.itemToKey = itemToKey;
//		this.itemToComponent = itemToComponent;
//		this.targetLayout = targetLayout;
//
//		refresh();
//		enableSync(true);
//	}
//
//	public synchronized void enableSync(boolean onOrOff) {
//		if (onOrOff && dataProviderRegistration == null) {
//			dataProviderRegistration = dataProvider.addDataProviderListener(event -> {
//				refresh();
//			});
//		} else if (dataProviderRegistration != null){
//			dataProviderRegistration.remove();
//			dataProviderRegistration = null;
//		}
//	}
//
//	public static <T> ListBindingSupport2<T> create(
//			DataProvider<T, String> dataProvider,
//			Function<? super T, ? extends ManagedComponent> itemToComponent,
//			Function<? super T, ?> itemToKey,
//			HasOrderedComponents<?> targetLayout) {
//
//		return new ListBindingSupport2<T>(dataProvider, itemToKey, itemToComponent, targetLayout);
//	}
//
//	/** Constructor where items serve directly as keys */
//	public static <T> ListBindingSupport2<T> create(
//			DataProvider<T, String> dataProvider,
//			Function<? super T, ? extends ManagedComponent> itemToComponent,
//			HasOrderedComponents<?> targetLayout) {
//
//		return create(dataProvider, itemToComponent, item -> item, targetLayout);
//	}
//
//
//	public synchronized void refresh() {
//		Query<T, String> query = new Query<>();
//		List<T> newItems = dataProvider.fetch(query).collect(Collectors.toList());
//
//		Set<Object> keys = newItems.stream().map(itemToKey).collect(Collectors.toSet());
//
//		// Remove components not among the new items
//		for (Entry<Object, ManagedComponent> e : keyToView.entrySet()) {
//			Object key = e.getKey();
//			ManagedComponent component = e.getValue();
//			if (!keys.contains(key)) {
//				targetLayout.remove(component.getComponent());
//				component.close();
//				keyToView.remove(key);
//			}
//		}
//
//		// Add the new items at the appropriate indices
//		int i = 0;
//		for (T item : newItems) {
//			Object key = itemToKey.apply(item);
//			ManagedComponent component = keyToView.get(key);
//			if (component == null) {
//				component = itemToComponent.apply(item);
//				keyToView.put(key, component);
//			}
//
//			targetLayout.addComponentAtIndex(i, component.getComponent());
//
//			component.refresh();
//			++i;
//		}
//
//		currentItems = newItems;
//	}
//
//

//}

