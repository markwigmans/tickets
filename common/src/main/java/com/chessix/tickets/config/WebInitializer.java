package com.chessix.tickets.config;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import com.yammer.metrics.reporting.MetricsServlet;

/**
 * Web initializer without the need for a web.xml file.
 * 
 * @author Mark Wigmans
 * @see <a
 *      href="http://static.springsource.org/spring-framework/docs/3.2.3.RELEASE/spring-framework-reference/html/mvc.html">Spring
 *      MVC</a>
 */
public class WebInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    public void onStartup(final ServletContext servletContext) throws ServletException {
        super.onStartup(servletContext);

        registerMetricsServlet(servletContext);
    }

    /**
     * Register the metrics servlet.
     */
    void registerMetricsServlet(final ServletContext servletContext) {
        final ServletRegistration.Dynamic metricsServlet = servletContext.addServlet("metricsServlet", new MetricsServlet());
        metricsServlet.setLoadOnStartup(1);
        metricsServlet.addMapping("/metrics/*");
    }
    
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return null;
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[] { WebConfig.class };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }

    @Override
    protected Filter[] getServletFilters() {

        final CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");
        return new Filter[] { characterEncodingFilter };
    }
}
