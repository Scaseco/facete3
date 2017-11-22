package org.hobbit.benchmark.faceted_browsing.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import org.hobbit.core.service.docker.DockerService;
import org.hobbit.core.service.docker.DockerServiceFactory;

import com.google.common.util.concurrent.Service;

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
public class DockerServiceFactoryGeneric2<C, S extends Service>
	implements DockerServiceFactory<DockerService>
{
	protected Map<String, ? extends Supplier<? extends C>> imageNameToConfigSupplier;	
	protected Function<? super C, ? extends S> configToService;

	protected Map<String, AtomicInteger> imageToNextId = new HashMap<>();
		
//	public static class Builder<X, A, B> {
//		protected Map<String, X> imageNameToConfig;	
//		
//		protected BiFunction<Map<String, String>, X, A> start;
//		protected Function<A, B> run;
//		protected BiConsumer<A, B> stop;
//	
//		public Builder<X, A, B> setConfigMap(Map<String, X> imageNameToConfig) {
//			this.imageNameToConfig = imageNameToConfig;
//			return this;
//		}
//		
//		public Builder<X, A, B> setStart(BiFunction<Map<String, String>, X, A> start) {
//			this.start = start;
//			return this;
//		}
//
//		public Builder<X, A, B> setStop(BiConsumer<A, B> stop) {
//			this.stop = stop;
//			return this;
//		}
//
//		public Builder<X, A, B> setRun(Function<A, B> run) {
//			this.run = run;
//			return this;
//		}
//
//		public DockerServiceFactoryGeneric2<X, A, B> build() {
//			DockerServiceFactoryGeneric2<X, A, B> result = new DockerServiceFactoryGeneric2<>(
//					imageNameToConfig,
//					start, run, stop);
//			return result;
//		}
//	}
	

	public DockerServiceFactoryGeneric2(
			Map<String, ? extends Supplier<? extends C>> imageNameToConfig,
			Function<? super C, ? extends S> configToService
		) {
		super();
		this.imageNameToConfigSupplier = imageNameToConfig;
		this.configToService = configToService;
	}




//	public Map<String, Function<Map<String, String>, X>> getImageNameToConfig() {
//		return imageNameToConfig;
//	}
	
	@Override
	public DockerService create(String imageName, Map<String, String> env) {

		Supplier<? extends C> imageConfigSupplier = imageNameToConfigSupplier.get(imageName);
		Objects.requireNonNull(imageConfigSupplier);
		
		C imageConfig = imageConfigSupplier.get();
		S service = configToService.apply(imageConfig);
		
		
		
		Supplier<Integer> idSupplier = () -> imageToNextId.computeIfAbsent(imageName, (x) -> new AtomicInteger()).incrementAndGet();
		Supplier<String> idStrSupplier = () -> imageName + "-" + idSupplier.get();

		DockerService result = new DockerServiceDelegate<>(service, imageName, idStrSupplier);
		return result;
	}

}