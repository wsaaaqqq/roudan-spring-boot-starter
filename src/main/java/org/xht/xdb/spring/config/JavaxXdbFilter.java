package org.xht.xdb.spring.config;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

@Slf4j
public class JavaxXdbFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
        log.info("================== {} ==================", "XdbFilter init");
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
        log.info("================== {} ==================", "XdbFilter destroy");
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        XdbFilterDelegate.doFilter();
        chain.doFilter(request, response);
    }
}
