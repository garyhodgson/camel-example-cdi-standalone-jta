package org.apache.camel.example.cdi.util;

import javax.annotation.Resource;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.inject.Inject;
import org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CDI Extension to replace JEE @Resource with @Inject for CDI SE.
 *
 * see also https://blog.novatec-gmbh.de/unit-testing-jee-applications-cdi/
 */
public class ResourceInjectReplacementExtension implements Extension {
    private static final Logger LOG = LoggerFactory.getLogger(ResourceInjectReplacementExtension.class);

    public ResourceInjectReplacementExtension() {
    }

    <X> void addInjectAnnotation(final AnnotatedType<X> annotatedType, AnnotatedTypeBuilder<X> builder) {
        annotatedType.getFields().stream() //
                .filter((field) -> (!field.isAnnotationPresent(Inject.class) && field.isAnnotationPresent(Resource.class))) //
                .forEach((field) -> {
                    LOG.info("@Resource override for field: {}", field);
                    builder.addToField(field, AnnotationInstanceProvider.of(Inject.class));
                });
    }

    <X> void processBean(@Observes @WithAnnotations({Resource.class}) ProcessAnnotatedType<X> processAnnotatedType, BeanManager beanManager) {
        AnnotatedType annotatedType = processAnnotatedType.getAnnotatedType();
        AnnotatedTypeBuilder<X> annotatedTypeBuilder = new AnnotatedTypeBuilder<>().readFromType(annotatedType);
        addInjectAnnotation(annotatedType, annotatedTypeBuilder);
        processAnnotatedType.setAnnotatedType(annotatedTypeBuilder.create());
    }
}
