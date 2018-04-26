package org.hobbit.benchmark.faceted_browsing.config;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.hobbit.core.service.api.DockerServiceDelegate;
import org.hobbit.core.service.docker.DockerService;
import org.hobbit.core.service.docker.DockerServiceFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import com.google.common.util.concurrent.Service;

public class DockerServiceFactorySpringApplicationBuilder
	implements DockerServiceFactory<DockerService>
{
	protected Map<String, ? extends Supplier<? extends SpringApplicationBuilder>> imageNameToConfigSupplier;	

	protected Map<String, AtomicInteger> imageToNextId = new HashMap<>();
	

	public DockerServiceFactorySpringApplicationBuilder(
			Map<String, ? extends Supplier<? extends SpringApplicationBuilder>> imageNameToConfig
		) {
		super();
		this.imageNameToConfigSupplier = imageNameToConfig;
	}	
	
	public static <T> T getRoot(T node, Function<? super T, ? extends T> getParent) {
		T parent = node;
		T current = node;
		
		if(parent != null) {
			do {
				current = parent;
				parent = getParent.apply(current);
			} while(parent != null);
		}

		return current;
	}
	
	public static SpringApplicationBuilder getParent(SpringApplicationBuilder appBuilder) {
		SpringApplicationBuilder result;
		try {
			Field field = appBuilder.getClass().getDeclaredField("parent");
			field.setAccessible(true);
			Object v = field.get(appBuilder);
			result = (SpringApplicationBuilder)v;
			field.setAccessible(false);
		} catch(Exception e) {
			 throw new RuntimeException(e);
		}
		return result;
	}

	@Override
	public DockerService create(String imageName, Map<String, String> env) {

		Supplier<? extends SpringApplicationBuilder> imageConfigSupplier = imageNameToConfigSupplier.get(imageName);
		if(imageConfigSupplier == null) {
			throw new UnsupportedOperationException("No image '" + imageName + "' registered with this docker service factory");
		}
		
		//Objects.requireNonNull(imageConfigSupplier);
		
		SpringApplicationBuilder appBuilder = imageConfigSupplier.get();
		SpringApplicationBuilder rootBuilder = getRoot(appBuilder, DockerServiceFactorySpringApplicationBuilder::getParent);
		
		Map<String, Object> env2 = env.entrySet().stream().collect(Collectors.toMap(Entry::getKey, x -> (Object)x.getValue()));

		ConfigurableEnvironment cenv = new StandardEnvironment();
		cenv.getPropertySources().addFirst(new MapPropertySource("myPropertySource", env2));
		
		// TODO We may need to merge with a prior environment
		rootBuilder.environment(cenv);

		Supplier<Integer> idSupplier = () -> imageToNextId.computeIfAbsent(imageName, (x) -> new AtomicInteger()).incrementAndGet();
		Supplier<String> idStrSupplier = () -> imageName + "-" + idSupplier.get();

		Service service = new ServiceSpringApplicationBuilder(imageName, appBuilder);

		DockerService result = new DockerServiceDelegate<>(service, imageName, idStrSupplier);
		return result;
	}

}



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

//public static class Builder<X, A, B> {
//protected Map<String, X> imageNameToConfig;	
//
//protected BiFunction<Map<String, String>, X, A> start;
//protected Function<A, B> run;
//protected BiConsumer<A, B> stop;
//
//public Builder<X, A, B> setConfigMap(Map<String, X> imageNameToConfig) {
//	this.imageNameToConfig = imageNameToConfig;
//	return this;
//}
//
//public Builder<X, A, B> setStart(BiFunction<Map<String, String>, X, A> start) {
//	this.start = start;
//	return this;
//}
//
//public Builder<X, A, B> setStop(BiConsumer<A, B> stop) {
//	this.stop = stop;
//	return this;
//}
//
//public Builder<X, A, B> setRun(Function<A, B> run) {
//	this.run = run;
//	return this;
//}
//
//public DockerServiceFactoryGeneric2<X, A, B> build() {
//	DockerServiceFactoryGeneric2<X, A, B> result = new DockerServiceFactoryGeneric2<>(
//			imageNameToConfig,
//			start, run, stop);
//	return result;
//}
//}