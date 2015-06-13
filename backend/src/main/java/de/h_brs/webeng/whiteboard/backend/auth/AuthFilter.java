package de.h_brs.webeng.whiteboard.backend.auth;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.h_brs.webeng.whiteboard.backend.DrawingEndpoint;

@WebFilter(filterName = "authFilter", urlPatterns = {"/rest/whiteboards/*"})
public final class AuthFilter implements Filter{
	private static final Logger LOG = LoggerFactory.getLogger(DrawingEndpoint.class);
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// noop
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
			this.doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
		} else {
			chain.doFilter(request, response);
		}
	}
	
	private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) 
			throws IOException, ServletException 
	{
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		// noop
	}

}
