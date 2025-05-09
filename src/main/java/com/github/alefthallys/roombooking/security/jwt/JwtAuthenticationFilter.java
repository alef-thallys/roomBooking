package com.github.alefthallys.roombooking.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	
	private final JwtTokenProvider jwtTokenProvider;
	
	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request,
	                                HttpServletResponse response,
	                                FilterChain filterChain)
			throws ServletException, IOException {
		
		String token = getTokenFromRequest(request);
		
		if (token != null && jwtTokenProvider.validateToken(token)) {
			String username = jwtTokenProvider.getUsernameFromToken(token);
			var auth = new UsernamePasswordAuthenticationToken(username, null, null); // Adicione roles se tiver
			auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(auth);
		}
		
		filterChain.doFilter(request, response);
	}
	
	private String getTokenFromRequest(HttpServletRequest request) {
		String bearer = request.getHeader("Authorization");
		return (bearer != null && bearer.startsWith("Bearer ")) ? bearer.substring(7) : null;
	}
}
