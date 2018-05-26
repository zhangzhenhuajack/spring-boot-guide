package com.alo7.framework.tracing.jms.config;

import io.opentracing.Tracer;
import io.opentracing.contrib.jms.spring.TracingJmsListenerConfigurer;
import io.opentracing.contrib.jms.spring.TracingJmsListenerEndpointRegistry;
import io.opentracing.contrib.jms.spring.TracingJmsTemplate;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.AbstractLazyCreationTargetSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration;
import org.springframework.cloud.sleuth.instrument.opentracing.OpentracingAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.destination.DestinationResolver;

import javax.jms.ConnectionFactory;
import javax.jms.Message;
import java.time.Duration;

@Configuration
@AutoConfigureAfter({TraceAutoConfiguration.class,OpentracingAutoConfiguration.class})
@ConditionalOnClass({Message.class, JmsTemplate.class})
public class TracingJmsAutoConfiguration {


    private final JmsProperties properties;

    private final ObjectProvider<DestinationResolver> destinationResolver;

    private final ObjectProvider<MessageConverter> messageConverter;

    public TracingJmsAutoConfiguration(JmsProperties properties,
                                       ObjectProvider<DestinationResolver> destinationResolver,
                                       ObjectProvider<MessageConverter> messageConverter) {
        this.properties = properties;
        this.destinationResolver = destinationResolver;
        this.messageConverter = messageConverter;
    }

    @Bean
    @ConditionalOnMissingBean
    public TracingJmsListenerEndpointRegistry createTracingJmsListenerEndpointRegistry(Tracer tracer) {
        return new TracingJmsListenerEndpointRegistry(tracer);
    }

    @Bean
    @ConditionalOnMissingBean
    public JmsListenerConfigurer createTracingJmsListenerConfigurer(
        TracingJmsListenerEndpointRegistry registry) {
        return new TracingJmsListenerConfigurer(registry);
    }

    /**
     * see org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration.JmsTemplateConfiguration
     */
    @Bean
    @ConditionalOnMissingBean
    public JmsTemplate jmsTemplate(BeanFactory beanFactory, Tracer tracer) {
        // we create lazy proxy, to avoid dependency and config order
        // if JMS is used, and ConnectionFactory bean is not present,
        // it will throw an error on first use, so imo, we should be all good
        ConnectionFactory connectionFactory = createProxy(beanFactory);

        //this over JmsAutoConfiguration.JmsTemplateConfiguration config
        PropertyMapper map = PropertyMapper.get();
        TracingJmsTemplate template = new TracingJmsTemplate(connectionFactory,tracer);
        template.setPubSubDomain(this.properties.isPubSubDomain());
        map.from(this.destinationResolver::getIfUnique).whenNonNull()
            .to(template::setDestinationResolver);
        map.from(this.messageConverter::getIfUnique).whenNonNull()
            .to(template::setMessageConverter);
        mapTemplateProperties(this.properties.getTemplate(), template);
        return template;
    }

    /**
     * see org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration.JmsTemplateConfiguration
     *
     * @param properties
     * @param template
     */
    private void mapTemplateProperties(JmsProperties.Template properties, JmsTemplate template) {
        PropertyMapper map = PropertyMapper.get();
        map.from(properties::getDefaultDestination).whenNonNull()
            .to(template::setDefaultDestinationName);
        map.from(properties::getDeliveryDelay).whenNonNull().as(Duration::toMillis)
            .to(template::setDeliveryDelay);
        map.from(properties::determineQosEnabled).to(template::setExplicitQosEnabled);
        map.from(properties::getDeliveryMode).whenNonNull().as(JmsProperties.DeliveryMode::getValue)
            .to(template::setDeliveryMode);
        map.from(properties::getPriority).whenNonNull().to(template::setPriority);
        map.from(properties::getTimeToLive).whenNonNull().as(Duration::toMillis)
            .to(template::setTimeToLive);
        map.from(properties::getReceiveTimeout).whenNonNull().as(Duration::toMillis)
            .to(template::setReceiveTimeout);
    }

    private ConnectionFactory createProxy(final BeanFactory beanFactory) {
        return (ConnectionFactory) ProxyFactory.getProxy(new AbstractLazyCreationTargetSource() {
            @Override
            public synchronized Class<?> getTargetClass() {
                return ConnectionFactory.class;
            }

            @Override
            protected Object createObject() throws Exception {
                return beanFactory.getBean(ConnectionFactory.class);
            }
        });
    }

}
