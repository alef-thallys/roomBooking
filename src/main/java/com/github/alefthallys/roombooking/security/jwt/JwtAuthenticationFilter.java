package com.github.alefthallys.roombooking.security.jwt;

import com.github.alefthallys.roombooking.exceptions.InvalidJwtException;
import com.github.alefthallys.roombooking.security.services.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	
	private final JwtTokenProvider jwtTokenProvider;
	private final CustomUserDetailsService customUserDetailsService;
	
	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, CustomUserDetailsService customUserDetailsService) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.customUserDetailsService = customUserDetailsService;
	}
	
	private static void handleException(HttpServletRequest request, HttpServletResponse response, InvalidJwtException ex) throws IOException {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType("application/json");
		response.getWriter().write("""
				{
				  "timestamp": "%s",
				  "status": 401,
				  "message": "%s",
				}
				""".formatted(java.time.Instant.now(), ex.getMessage(), request.getRequestURI()));
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		
		if (request.getRequestURI().equals("/api/v1/auth/login") || request.getRequestURI().equals("/api/v1/auth/register")) {
			filterChain.doFilter(request, response);
			return;
		}
		
		try {
			String token = getTokenFromRequest(request);
			jwtTokenProvider.validateToken(token);
			String username = jwtTokenProvider.getUsernameFromToken(token);
			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
				
				var auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				
				auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(auth);
			}
		} catch (InvalidJwtException ex) {
			handleException(request, response, ex);
			return;
		}
		
		filterChain.doFilter(request, response);
	}
	
	private String getTokenFromRequest(HttpServletRequest request) throws InvalidJwtException {
		String bearer = request.getHeader("Authorization");
		if (bearer == null) {
			throw new InvalidJwtException("Authorization header is missing");
		}
		return bearer.startsWith("Bearer ") ? bearer.substring(7) : null;
	}
}
