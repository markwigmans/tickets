package com.chessix.tickets.config;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import akka.actor.ActorSystem;

import com.chessix.tickets.Main;
import com.chessix.tickets.spring.ActorBuilder;

@Configuration
@EnableWebMvc
@ComponentScan(basePackageClasses = Main.class)
public class WebConfig extends WebMvcConfigurerAdapter {

    @SuppressWarnings("unused")
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    
    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        final LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");
        registry.addInterceptor(localeChangeInterceptor);
    }

    @Bean
    public LocaleResolver localeResolver() {
        final CookieLocaleResolver cookieLocaleResolver = new CookieLocaleResolver();
        cookieLocaleResolver.setDefaultLocale(StringUtils.parseLocaleString("en"));
        return cookieLocaleResolver;
    }

    @Bean
    public ViewResolver viewResolver() {
        final InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setViewClass(JstlView.class);
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".jsp");
        return viewResolver;
    }

    @Bean
    public MessageSource messageSource() {
        final ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames("classpath:messages/messages", "classpath:messages/validation");
        // if true, the key of the message will be displayed if the key is not
        // found, instead of throwing a NoSuchMessageException
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setDefaultEncoding("UTF-8");
        // # -1 : never reload, 0 always reload
        messageSource.setCacheSeconds(0);
        return messageSource;
    }
    
    @Bean
    public PropertiesFactoryBean properties() {
        final PropertiesFactoryBean factoryBean = new PropertiesFactoryBean();
        final List<Resource> locations = new ArrayList<Resource>();
        locations.add(new ClassPathResource("/tickets-default.properties"));
        locations.add(new ClassPathResource("/tickets.properties"));
        locations.add(new FileSystemResource("${tickets.config.dir}/tickets.properties"));
        factoryBean.setLocations(locations.toArray(new Resource[0]));
        factoryBean.setIgnoreResourceNotFound(true);
        return factoryBean;
    }
    
    @Bean
    public ActorSystem actorSystem() {
        return ActorSystem.create("EventSystem");
    }
    
    @Bean
    public ActorBuilder eventSuperVisorBuilder() {
        return new ActorBuilder("eventSuperVisor");
    }
}
