package org.aksw.facete3.app.vaadin.plugin;

import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.jena.rdf.model.Resource;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;

import com.vaadin.flow.component.Component;

public class ComponentPlugin {
    // protected ConfigurableApplicationContext parentCxt;
    protected Supplier<SpringApplicationBuilder> appBuilderSupplier;
    protected Function<? super SpringApplicationBuilder, ? extends SpringApplicationBuilder> appBuilderTransformer;

    public ComponentPlugin(
            Supplier<SpringApplicationBuilder> appBuilderSupplier,
            Function<? super SpringApplicationBuilder, ? extends SpringApplicationBuilder> appBuilderTransformer
            ) {
        super();
        this.appBuilderSupplier = appBuilderSupplier;
        this.appBuilderTransformer = appBuilderTransformer;
    }

    public ConfigurableComponent<Resource> newComponent() {

        SpringApplicationBuilder baseAppBuilder = appBuilderSupplier.get();
        SpringApplicationBuilder effectiveAppBuilder = appBuilderTransformer.apply(baseAppBuilder);

        ConfigurableApplicationContext result = effectiveAppBuilder.run();

        Resource config = result.getBean(Resource.class);
        Component component = result.getBean(Component.class);

        return new ConfigurableComponent<Resource>() {
            @Override public void refresh() { result.getBean(RefreshScope.class).refreshAll(); }
            @Override public Resource getConfig() { return config; }
            @Override public Component getComponent() { return component; }
            @Override public void close() { result.close(); }
        };
    }

    public static Supplier<SpringApplicationBuilder> baseAppBuilderSupplier() {
        return () -> new SpringApplicationBuilder()
//                .properties(ImmutableMap.<String, Object>builder()
//                        .build())
                .headless(true)
                .web(WebApplicationType.NONE)
                .bannerMode(Mode.OFF);
    }

    public static ComponentPlugin createWithDefaultBase(Function<? super SpringApplicationBuilder, ? extends SpringApplicationBuilder> appBuilderTransformer) {
        return new ComponentPlugin(baseAppBuilderSupplier(), appBuilderTransformer);
    }
}
