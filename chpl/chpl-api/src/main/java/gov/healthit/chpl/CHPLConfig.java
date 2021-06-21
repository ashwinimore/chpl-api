package gov.healthit.chpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import gov.healthit.chpl.api.ApiKeyManager;
import gov.healthit.chpl.filter.APIKeyAuthenticationFilter;
import gov.healthit.chpl.registration.RateLimitingInterceptor;
import gov.healthit.chpl.web.controller.annotation.CacheControlHandlerInterceptor;
import lombok.extern.log4j.Log4j2;

@Configuration
@EnableWebMvc
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableAsync
@EnableAspectJAutoProxy
@EnableScheduling
@PropertySources({
    @PropertySource("classpath:/environment.properties"),
    @PropertySource(value = "classpath:/environment-override.properties", ignoreResourceNotFound = true),
    @PropertySource("classpath:/lookup.properties"),
    @PropertySource(value = "classpath:/lookup-override.properties", ignoreResourceNotFound = true),
    @PropertySource("classpath:/email.properties"),
    @PropertySource(value = "classpath:/email-override.properties", ignoreResourceNotFound = true),
})
@ComponentScan(basePackages = {
        "gov.healthit.chpl.**"
})
@Log4j2
public class CHPLConfig implements WebMvcConfigurer {
    private static final long MAX_UPLOAD_FILE_SIZE = 5242880;
    private static final int MAX_COOKIE_AGE_SECONDS = 3600;

    @Autowired
    private ObjectFactory<ApiKeyManager> apiKeyManagerObjectFactory;

    @Bean
    public MappingJackson2HttpMessageConverter jsonConverter() {
        MappingJackson2HttpMessageConverter bean = new MappingJackson2HttpMessageConverter();
        bean.setPrefixJson(false);
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        bean.setSupportedMediaTypes(mediaTypes);
        return bean;
    }

    @Bean(name = "multipartResolver")
    public CommonsMultipartResolver getResolver() throws IOException {
        LOGGER.info("get CommonsMultipartResolver");
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();

        // Set the maximum allowed size (in bytes) for each individual file: 5MB
        resolver.setMaxUploadSize(MAX_UPLOAD_FILE_SIZE);
        return resolver;
    }

    @Bean
    public APIKeyAuthenticationFilter apiKeyAuthenticationFilter() {
        LOGGER.info("get APIKeyAuthenticationFilter");
        ApiKeyManager apiKeyManager = this.apiKeyManagerObjectFactory.getObject();
        return new APIKeyAuthenticationFilter(apiKeyManager);
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("errors-override");

        ResourceBundleMessageSource parentMessageSource = new ResourceBundleMessageSource();
        parentMessageSource.setBasename("errors");

        messageSource.setParentMessageSource(parentMessageSource);
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public CookieLocaleResolver localeResolver() {
        CookieLocaleResolver localeResolver = new CookieLocaleResolver();
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        localeResolver.setCookieName("my-locale-cookie");
        localeResolver.setCookieMaxAge(MAX_COOKIE_AGE_SECONDS);
        return localeResolver;
    }

    @Bean
    public LocaleChangeInterceptor localeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Bean
    public RateLimitingInterceptor rateLimitingInterceptor() {
        RateLimitingInterceptor interceptor = new RateLimitingInterceptor();
        return interceptor;
    }

    @Bean
    public CacheControlHandlerInterceptor cacheControlHandlerInterceptor() {
        CacheControlHandlerInterceptor interceptor = new CacheControlHandlerInterceptor();
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeInterceptor());
        registry.addInterceptor(rateLimitingInterceptor())
        .addPathPatterns("/**")
        .excludePathPatterns(APIKeyAuthenticationFilter.ALLOWED_REQUEST_PATHS);
        registry.addInterceptor(cacheControlHandlerInterceptor());
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        LOGGER.info("Get BCryptPasswordEncoder");
        return new BCryptPasswordEncoder();
    }
}
