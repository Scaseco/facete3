package org.aksw.facete3.app.vaadin.qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.aksw.facete3.app.vaadin.plugin.search.SearchPlugin;
import org.springframework.beans.factory.annotation.Qualifier;

import com.vaadin.flow.data.provider.DataProvider;

/**
 * Qualifier for {@link DataProvider}s holding {@link SearchPlugin}s.
 *
 * @author raven
 *
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface SearchPluginProvider {
    String value() default "";
}
