package org.xht.xdb.spring.config;

import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;

@Slf4j
public class JakartaXdbFilter implements Filter {

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
