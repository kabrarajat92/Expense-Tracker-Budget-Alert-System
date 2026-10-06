package com.expensetracker.notificationservice.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;



import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter{
	private final JwtUtil jwtUtil;

	public JwtAuthFilter(JwtUtil jwtUtil) {
		super();
		this.jwtUtil = jwtUtil;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			System.out.println("Reaching end of filter, calling doFilter next");
			filterChain.doFilter(request, response);
			return;
		}
		String token = authHeader.substring(7);

		if (jwtUtil.isTokenValid(token)) {
			String username = jwtUtil.extractUsername(token);
			System.out.println("Extracted username: [" + username + "]");
			String role = jwtUtil.extractRole(token);
			if (SecurityContextHolder.getContext().getAuthentication() == null) {
				UsernamePasswordAuthenticationToken authToken =
		                new UsernamePasswordAuthenticationToken(
		                        username,
		                        null,
		                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
		                );
		        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		        SecurityContextHolder.getContext().setAuthentication(authToken);
			}
			filterChain.doFilter(request, response);
		}

	}
}
