package org.hobbit.trash;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.hobbit.core.service.docker.DockerService;
import org.hobbit.core.service.docker.DockerServiceFactory;
import org.hobbit.trash.DockerServiceSimple;

/**
 * A generic service factory.
 * When creating a service of imageName and environment env, the following steps are taken:
 * 
 * <ol>
 *   <li>There is a registry (a map) which maps imageNames to configuration objects of type X.</li>
 *   <li>service.start() uses the supplied start-function to mapconfiguration to an init object a of type A</li>
 *   <li>service.run() takes the init-object and yields a run-object of type B</li>
 *   <li>service.stop() passes both a and b to the supplied stop biconsumer</li>
 * </ol>
 * 
 * @author raven Nov 18, 2017
 *
 * @param <X>
 * @param <A>
 * @param <B>
 */
@Deprecated
public class DockerServiceFactoryGeneric<X, A, B>
	implements DockerServiceFactory<DockerService>
{
	protected Map<String, X> imageNameToConfig;	
	protected Map<String, AtomicInteger> imageToNextId = new HashMap<>();
	
	protected BiFunction<Map<String, String>, X, A> start;
	protected Function<A, B> run;
	protected BiConsumer<A, B> stop;
	
	public static class Builder<X, A, B> {
		protected Map<String, X> imageNameToConfig;	
		
		protected BiFunction<Map<String, String>, X, A> start;
		protected Function<A, B> run;
		protected BiConsumer<A, B> stop;
	
		public Builder<X, A, B> setConfigMap(Map<String, X> imageNameToConfig) {
			this.imageNameToConfig = imageNameToConfig;
			return this;
		}
		
		public Builder<X, A, B> setStart(BiFunction<Map<String, String>, X, A> start) {
			this.start = start;
			return this;
		}

		public Builder<X, A, B> setStop(BiConsumer<A, B> stop) {
			this.stop = stop;
			return this;
		}

		public Builder<X, A, B> setRun(Function<A, B> run) {
			this.run = run;
			return this;
		}

		public DockerServiceFactoryGeneric<X, A, B> build() {
			DockerServiceFactoryGeneric<X, A, B> result = new DockerServiceFactoryGeneric<>(
					imageNameToConfig,
					start, run, stop);
			return result;
		}
	}
	

	public DockerServiceFactoryGeneric(Map<String, X> imageNameToConfig,
			BiFunction<Map<String, String>, X, A> start,
			Function<A, B> run,
			BiConsumer<A, B> stop) {
		super();
		this.imageNameToConfig = imageNameToConfig;
		this.start = start;
		this.run = run;
		this.stop = stop;
	}




//	public Map<String, Function<Map<String, String>, X>> getImageNameToConfig() {
//		return imageNameToConfig;
//	}
	
	@Override
	public DockerService create(String imageName, Map<String, String> env) {
//		Function<Map<String, String>, X> imageConfigFn = imageNameToConfig.get(imageName);
//		X imageConfig = imageConfigFn.apply(env);

		X imageConfig = imageNameToConfig.get(imageName);
		Objects.requireNonNull(imageConfig);
		
		Supplier<Integer> idSupplier = () -> imageToNextId.computeIfAbsent(imageName, (x) -> new AtomicInteger()).incrementAndGet();
		Supplier<String> idStrSupplier = () -> imageName + "-" + idSupplier.get();
		
		return new DockerServiceSimple<Entry<String, A>, B>(
				() -> {
					String containerId = idStrSupplier.get();
					A initObject = start.apply(env, imageConfig);
					return new SimpleEntry<>(containerId, initObject);
				},
				Entry::getKey,
				ea -> run.apply(ea.getValue()),
				(ea, b) -> stop.accept(ea.getValue(), b),
				imageName);
	}




	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		
	}

}