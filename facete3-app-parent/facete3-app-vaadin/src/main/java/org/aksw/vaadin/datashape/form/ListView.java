package org.aksw.vaadin.datashape.form;

import java.util.function.Function;

import org.aksw.facete3.app.vaadin.plugin.ManagedComponent;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;

public class ListView<T>
	extends VerticalLayout
{
	protected ListBindingSupport<T> listBindingSupport;
	
	public ListView(DataProvider<T, String> dataProvider,
			Function<? super T, ? extends ManagedComponent> itemToComponent,
			Function<? super T, ?> itemToKey) {
		super();
		listBindingSupport = ListBindingSupport.create(dataProvider, itemToComponent, this);
	}


	public static <T> ListView<T> create(
			DataProvider<T, String> dataProvider,
			Function<T, ManagedComponent> itemToComponent) {
		return new ListView<T>(dataProvider, itemToComponent, item -> item);
	}

/*
	protected Map<T, ManagedComponent> itemToView = new LinkedHashMap<>();
	protected DataProvider<T, String> dataProvider;
	protected Function<T, ManagedComponent> itemToComponent;
	

	public void refresh() {
		List<T> items = dataProvider.fetch(new Query<>()).collect(Collectors.toList());
		Set<T> set = new HashSet<>(items);
		
		
		// Remove components not among the new items
		for (Entry<T, ManagedComponent> e : itemToView.entrySet()) {
			T item = e.getKey();
			ManagedComponent component = e.getValue();
			if (!set.contains(item)) {
				this.remove(component.getComponent());
				component.close();
				itemToView.remove(item);
			}
		}
		
		int i = 0;
		for (T item : items) {
			ManagedComponent component = itemToView.get(item);
			if (component == null) {
				component = itemToComponent.apply(item);
				itemToView.put(item, component);
			}

			addComponentAtIndex(i, component.getComponent());
			
			++i;
		}
	}
*/
}
