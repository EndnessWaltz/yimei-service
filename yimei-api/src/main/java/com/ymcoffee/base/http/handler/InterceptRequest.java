package com.ymcoffee.base.http.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.BasicErrorController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
@ComponentScan("com.ymcoffee")
public class InterceptRequest extends WebMvcConfigurerAdapter {
    @Autowired
    private ProcessRequest processRequest;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestInterceptor());
    }

    // 跨域
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**");
    }


    @Bean
    public HandlerInterceptorAdapter requestInterceptor() {
        return new HandlerInterceptorAdapter() {

            public boolean isSupport(Object handler) {
                if (((HandlerMethod) handler).getBean() instanceof BasicErrorController) {
                    return false;
                }
                return true;
            }

            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServletException {
                if (this.isSupport(handler)) {
                    processRequest.clearRequestContext();
                    processRequest.setRequestContext(request, (HandlerMethod) handler);
                }

                if (!((HandlerMethod) handler).getMethod().isAnnotationPresent(OpenInterface.class)
                        && !((HandlerMethod) handler).getClass().isAnnotationPresent(OpenInterface.class)) {
                    // todo 校验token
                    return true;
                }

                return true;
            }

            @Override
            public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
                if (this.isSupport(handler)) {
                    processRequest.printLog(request, response);
                }
            }
        };
    }

}
