package org.aksw.vaadin.datashape.form;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.facete3.app.vaadin.plugin.ManagedComponent;

import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasOrderedComponents;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.shared.Registration;

/**
 * A class that keeps a list of items and a list of components created from these items in sync
 * upon calling .refresh().
 * 
 * @author raven
 *
 * @param <T>
 */
public class ListBindingSupport<T> {
	protected List<T> currentItems;
	protected Map<Object, ManagedComponent> keyToView = new LinkedHashMap<>();
	
	protected DataProvider<T, ?> dataProvider;
	
	protected Function<? super T, ?> itemToKey;
	protected Function<? super T, ? extends ManagedComponent> itemToComponent;
	
	
	protected HasComponents targetLayout;
	
	protected Registration dataProviderRegistration;
	
	public ListBindingSupport(
			DataProvider<T, ?> dataProvider,
			Function<? super T, ?> itemToKey,
			Function<? super T, ? extends ManagedComponent> itemToComponent,
			HasComponents targetLayout) {
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

	public static <T> ListBindingSupport<T> create(
			DataProvider<T, ?> dataProvider,
			Function<? super T, ? extends ManagedComponent> itemToComponent,
			Function<? super T, ?> itemToKey,
			HasOrderedComponents<?> targetLayout) {

		return new ListBindingSupport<T>(dataProvider, itemToKey, itemToComponent, targetLayout);
	}

	/** Constructor where items serve directly as keys */
	public static <T> ListBindingSupport<T> create(
			DataProvider<T, ?> dataProvider,
			Function<? super T, ? extends ManagedComponent> itemToComponent,
			HasOrderedComponents<?> targetLayout) {

		return create(dataProvider, itemToComponent, item -> item, targetLayout);
	}
	

	public synchronized void refresh() {
//	    Query<T, ?> query = new Query<>();
		Query query = new Query();
		List<T> newItems = (List<T>)dataProvider.fetch(query).collect(Collectors.toList());
		
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
}
