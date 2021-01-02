package org.aksw.vaadin.datashape.form;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.facete3.app.vaadin.plugin.ManagedComponent;
import org.aksw.jena_sparql_api.mapper.proxy.ResourceProxyBase;
import org.apache.jena.rdf.model.Resource;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasOrderedComponents;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.shared.Registration;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

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
/*
public class ListBindingSupport2<T, L extends HasComponents> {
	protected List<T> currentItems;
	
	protected ListMultimap<Object, ManagedComponent> keyToView = ArrayListMultimap.create();
	
	protected DataProvider<T, String> dataProvider;
	
	protected Function<? super T, ?> itemToKey;
	//protected Function<? super T, ? extends ManagedComponent> itemToComponent;
	
	protected BiConsumer<? super T, ? super L> itemRenderer;
	
	
	// protected HasOrderedComponents<?> targetLayout;
	protected L targetLayout;
	
	protected Registration dataProviderRegistration;
	
	public ListBindingSupport2(
			DataProvider<T, String> dataProvider,
			Function<? super T, ?> itemToKey,
			Function<? super T, ? extends ManagedComponent> itemToComponent,
			HasOrderedComponents<?> targetLayout) {
		super();
		this.dataProvider = dataProvider;
		this.itemToKey = itemToKey;
		this.itemToComponent = itemToComponent;
		this.targetLayout = targetLayout;
		
		refresh();
		enableSync(true);
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

	public static <T> ListBindingSupport2<T> create(
			DataProvider<T, String> dataProvider,
			Function<? super T, ? extends ManagedComponent> itemToComponent,
			Function<? super T, ?> itemToKey,
			HasOrderedComponents<?> targetLayout) {

		return new ListBindingSupport2<T>(dataProvider, itemToKey, itemToComponent, targetLayout);
	}

	/** Constructor where items serve directly as keys * /
	public static <T> ListBindingSupport2<T> create(
			DataProvider<T, String> dataProvider,
			Function<? super T, ? extends ManagedComponent> itemToComponent,
			HasOrderedComponents<?> targetLayout) {

		return create(dataProvider, itemToComponent, item -> item, targetLayout);
	}
	

	public synchronized void refresh() {
		Query<T, String> query = new Query<>();
		List<T> newItems = dataProvider.fetch(query).collect(Collectors.toList());
		
		Set<Object> keys = newItems.stream().map(itemToKey).collect(Collectors.toSet());
		
		// Remove components not among the new items
		for (Entry<Object, ManagedComponent> e : keyToView.entrySet()) {
			Object key = e.getKey();
			ManagedComponent component = e.getValue();
			if (!keys.contains(key)) {
				targetLayout.remove(component.getComponent());
				component.close();
				keyToView.remove(key);
			}
		}
		
		// Add the new items at the appropriate indices
		int i = 0;
		for (T item : newItems) {
			Object key = itemToKey.apply(item);
			ManagedComponent component = keyToView.get(key);
			if (component == null) {
				component = itemToComponent.apply(item);
				keyToView.put(key, component);
			}

			targetLayout.addComponentAtIndex(i, component.getComponent());
			
			component.refresh();
			++i;
		}
		
		currentItems = newItems;
	}
	
	
	public static void createProxy(Class<? extends HasComponents> clazz) {
        Enhancer enhancer = new Enhancer();
        if(clazz.isInterface()) {
        } else {
            enhancer.setSuperclass(clazz);
        }
        
        HasComponents.class.getMethod("add", null);
        
        enhancer.setCallback(new MethodInterceptor() {
            public Object intercept(Object obj, java.lang.reflect.Method method, Object[] args,
                    MethodProxy proxy) throws Throwable {
            
            	
            	if (method.equals(args))
            }
        });

        o = enhancer.create(argTypes, argValues);
	}
}
*/
