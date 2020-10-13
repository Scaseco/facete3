package org.aksw.facete3.cli.main;

/**
 * Workaround to enable ActionListBox to include custom data
 * 
 * @author raven
 *
 * @param <T>
 */
public class RunnableWithLabelAndData<T>
	implements Runnable
{
	protected String label;
	protected Runnable runnable;
	protected T data;

	public RunnableWithLabelAndData(T data) {
		this("" + data, data, null);
	}

	public RunnableWithLabelAndData(String label, T data, Runnable runnable) {
		super();
		this.label = label;
		this.data = data;
		this.runnable = runnable;
	}

	@Override
	public void run() {
		if(runnable != null) {
			runnable.run();
		}
	}
	
	public T getData() {
		return data;
	}
	
	@Override
	public String toString() {
		return label;
	}
	
	public static <T> RunnableWithLabelAndData<T> from(String label, T data, Runnable runnable) {
		return new RunnableWithLabelAndData<T>(label, data, runnable);
	}
}