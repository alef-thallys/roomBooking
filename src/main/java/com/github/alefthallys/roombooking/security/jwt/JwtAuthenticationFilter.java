package com.github.alefthallys.roombooking.security.jwt;

import com.github.alefthallys.roombooking.exceptions.Auth.InvalidJwtException;
import com.github.alefthallys.roombooking.repositories.UserRepository;
import com.github.alefthallys.roombooking.security.CustomUserDetailsService;
import com.github.alefthallys.roombooking.security.SecurityConstants;
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
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	
	private final JwtTokenProvider jwtTokenProvider;
	private final CustomUserDetailsService customUserDetailsService;
	private final AntPathMatcher pathMatcher = new AntPathMatcher();
	private final UserRepository userRepository;
	
	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, CustomUserDetailsService customUserDetailsService, UserRepository userRepository) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.customUserDetailsService = customUserDetailsService;
		this.userRepository = userRepository;
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request,
	                                HttpServletResponse response,
	                                FilterChain filterChain) throws ServletException, IOException {
		
		String requestURI = request.getRequestURI();
		
		boolean isPublicEndpoint = SecurityConstants.PUBLIC_ENDPOINTS.stream()
				.anyMatch(pattern -> pathMatcher.match(pattern, requestURI));
		
		if (isPublicEndpoint) {
			filterChain.doFilter(request, response);
			return;
		}
		
		try {
			String token = extractToken(request);
			jwtTokenProvider.validateToken(token);
			
			String username = jwtTokenProvider.getUsernameFromToken(token);
			
			if (!userRepository.existsByEmail(username)) {
				throw new InvalidJwtException("Invalid JWT: User does not exist");
			}
			
			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
				
				var auth = new UsernamePasswordAuthenticationToken(
						userDetails,
						null,
						userDetails.getAuthorities()
				);
				
				auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(auth);
			}
			
		} catch (InvalidJwtException ex) {
			respondUnauthorized(response, request, ex);
			return;
		}
		
		filterChain.doFilter(request, response);
	}
	
	private String extractToken(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		
		if (header == null || !header.startsWith("Bearer ")) {
			throw new InvalidJwtException("Missing or invalid Authorization header");
		}
		
		return header.substring(7);
	}
	
	private void respondUnauthorized(HttpServletResponse response,
	                                 HttpServletRequest request,
	                                 InvalidJwtException ex) throws IOException {
		
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType("application/json");
		
		String body = """
				{
				  "timestamp": "%s",
				  "status": 401,
				  "message": "%s",
				  "path": "%s"
				}
				""".formatted(Instant.now(), ex.getMessage(), request.getRequestURI());
		
		response.getWriter().write(body);
	}
}