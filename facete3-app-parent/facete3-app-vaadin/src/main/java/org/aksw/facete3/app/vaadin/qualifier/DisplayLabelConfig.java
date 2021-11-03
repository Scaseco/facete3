package org.aksw.facete3.app.vaadin.qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.aksw.jenax.arq.aggregation.BestLiteralConfig;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Marker to designate a {@link BestLiteralConfig} bean as the configuration
 * for display labels. This is contrasted by e.g. labels used for searching.
 *
 * @author raven
 *
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface DisplayLabelConfig {
    String value() default "";
}
