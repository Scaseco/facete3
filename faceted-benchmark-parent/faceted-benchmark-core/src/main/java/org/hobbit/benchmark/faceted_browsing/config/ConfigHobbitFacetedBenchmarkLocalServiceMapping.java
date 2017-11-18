package org.hobbit.benchmark.faceted_browsing.config;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.hobbit.core.component.DataGeneratorFacetedBrowsing;
import org.hobbit.core.component.EvaluationModuleComponent;
import org.hobbit.core.component.TaskGeneratorFacetedBenchmark;
import org.hobbit.core.components.test.InMemoryEvaluationStore;
import org.hobbit.core.service.docker.DockerService;
import org.hobbit.core.service.docker.DockerServiceFactory;
import org.hobbit.core.service.docker.DockerServiceManagerServerComponent;
import org.hobbit.core.service.docker.DockerServiceSimple;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import com.google.common.util.concurrent.Service;


class DockerServiceFactoryFoo<X, A, B>
	implements DockerServiceFactory<DockerService>
{
	protected Map<String, X> imageNameToConfig;	
	protected Map<String, AtomicInteger> imageToNextId = new HashMap<>();
	
	protected BiFunction<Map<String, String>, X, A> start;
	protected Function<A, B> run;
	protected BiConsumer<A, B> stop;
	

	public DockerServiceFactoryFoo(Map<String, X> imageNameToConfig,
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
				() -> new SimpleEntry<>(idStrSupplier.get(), start.apply(env, imageConfig)),
				Entry::getKey,
				ea -> run.apply(ea.getValue()),
				(ea, b) -> stop.accept(ea.getValue(), b),
				imageName);
	}

}

public class ConfigHobbitFacetedBenchmarkLocalServiceMapping {

	public DockerServiceFactory<?> createDockerServiceFactoryForBootstrap(Map<String, Class<?>> imageNameToClass, Supplier<SpringApplicationBuilder> baseAppBuilder) {

		DockerServiceFactory<DockerService> result = new DockerServiceFactoryFoo<
					Class<?>,
					Entry<Map<String, String>, SpringApplicationBuilder>,
					ConfigurableApplicationContext>
			(					
				imageNameToClass,
				(env, clazz) -> new SimpleEntry<>(env, baseAppBuilder.get().sources(clazz)),
				e -> {
					Map<String, Object> env = e.getKey().entrySet().stream().collect(Collectors.toMap(Entry::getKey, x -> (Object)x));
					
					ConfigurableEnvironment cenv = new StandardEnvironment();
					cenv.getPropertySources().addFirst(new MapPropertySource("myPropertySource", env));

					ConfigurableApplicationContext r = e.getValue().environment(cenv).build().run();
					return r;
				},
				(appBuilder, ctx) -> ctx.close());
		
		return result;
	}
	
    @Bean
    public DockerServiceFactory<?> dockerServiceManagerComponent() throws TimeoutException {
        Map<String, Class<?>> imageNameToClass = new HashMap<>();
        imageNameToClass.put("git.project-hobbit.eu:4567/gkatsibras/faceteddatagenerator/image", DataGeneratorFacetedBrowsing.class);
        imageNameToClass.put("git.project-hobbit.eu:4567/gkatsibras/facetedtaskgenerator/image", TaskGeneratorFacetedBenchmark.class);
        imageNameToClass.put("git.project-hobbit.eu:4567/defaulthobbituser/defaultevaluationstorage:1.0.0", InMemoryEvaluationStore .class);
        imageNameToClass.put("git.project-hobbit.eu:4567/gkatsibras/facetedevaluationmodule/image", EvaluationModuleComponent.class);

        
        DockerServiceFactory<?> result = createDockerServiceFactoryForBootstrap(imageNameToClass,
        		() -> new SpringApplicationBuilder()
        );
        
//        Docker
//
//        BiFunction<String, Map<String, String>, DockerService> serviceFactory = (imageName, env) -> {        	
//        	if(!imageNameToClass.containsKey(imageName)) {
//        		throw new RuntimeException("No image " + imageName + " in the registry");
//        	}
//
//        	Class<?> ctx = imageNameToClass.get(imageName);
//        	Objects.requireNonNull(ctx);
//        	
//        	
//        	new DockerServiceSimpleDelegation(
//        			imageName,
//        			startServiceDelegate,
//        			stopServiceDelegate);
//        	
//        };
        
        // FIXME Get this mapping working: We may need a preconfigured env + launcher in addition to the class
        //DockerServiceFactorySimpleDelegation


        //DockerServiceManagerServerComponent result = new DockerServiceManagerServerComponent(null, null, null, null, null);
        return result;
    }

    @Bean
    //@Autowired
    public String dummy(@Qualifier("dockerServiceManagerComponent") Service service) throws TimeoutException {
        //Thread.dumpStack();

        service.startAsync();
        service.awaitRunning(60, TimeUnit.SECONDS);
        return "dummy";
    }
}
