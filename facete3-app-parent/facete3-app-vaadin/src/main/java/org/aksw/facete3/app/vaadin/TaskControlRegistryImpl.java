package org.aksw.facete3.app.vaadin;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import org.aksw.vaadin.common.provider.util.TaskControl;
import org.aksw.vaadin.common.provider.util.TaskControlRegistry;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.shared.Registration;

public class TaskControlRegistryImpl
	implements TaskControlRegistry
{
	public static long DFT_AUTO_CLEANUP_DELAY = 10_000;
	
	// protected TreeData<TaskControl<?>> taskTree = new TreeData<>();
	protected TreeDataProvider<TaskControl<?>> treeDataProvider =  new TreeDataProvider<>(new TreeData<>());
	protected List<Consumer<TaskControl<?>>> errorListeners = new ArrayList<>(); 
	
	protected UI ui;
	
	protected long autoCleanUpDelay = DFT_AUTO_CLEANUP_DELAY;
	protected Timer timer = new Timer(true);
	
	/** Return the tree data of the task tree */
//	public TreeData<TaskControl<?>> getTreeData() {
//		return taskTree;
//	}
	public TreeDataProvider<TaskControl<?>> getTreeDataProvider() {
		return treeDataProvider;
	}
	
	public void setUi(UI ui) {
		this.ui = ui;
	}
	
	/** Delay after which to auto-remove completed tasks */
	public void setAutoCleanUpDelay(long autoCleanUpDelay) {
		this.autoCleanUpDelay = autoCleanUpDelay;
	}
	
	@Override
	public void register(TaskControl<?> taskControl) {
		if (ui != null) {
			ui.access(() -> {
				treeDataProvider.getTreeData().addItem(null, taskControl);
				// NotificationUtils.success("Added a task: " + taskControl.getName());
				treeDataProvider.refreshAll();
				taskControl.whenComplete(throwable -> {
					// Refresh status
					ui.access(() -> {
						treeDataProvider.refreshAll();
					});
					
					// Schedule removal
					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							ui.access(() -> {
								treeDataProvider.getTreeData().removeItem(taskControl);
								treeDataProvider.refreshAll();
							});
						}
					}, autoCleanUpDelay);
					//if (throwable != null) {
						// XXX Schedule removal after e.g. 5 seconds
					// }
				});
			});
		}
	}
	
	/** Add a listener that gets notified whenever a task fails */
	public Registration addErrorListener(Consumer<TaskControl<?>> listener) {
		errorListeners.add(listener);
		return () -> errorListeners.remove(listener);
	}
}