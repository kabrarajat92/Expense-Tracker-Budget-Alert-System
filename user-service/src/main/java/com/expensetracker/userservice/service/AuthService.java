package com.expensetracker.userservice.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.expensetracker.userservice.dto.AuthResponse;
import com.expensetracker.userservice.dto.LoginRequest;
import com.expensetracker.userservice.dto.RegisterRequest;
import com.expensetracker.userservice.entity.User;
import com.expensetracker.userservice.repository.UserRepository;
import com.expensetracker.userservice.security.JwtUtil;


@Service
public class AuthService {
	private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
		super();
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtUtil = jwtUtil;
	}

	public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        return userRepository.save(user);
    }
	
	public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByUsername(request.getUsername())
	            .orElseThrow(() -> new RuntimeException("Invalid username or password"));
		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
	        throw new RuntimeException("Invalid username or password");
	    }
		String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name(), user.getEmail());
	    return new AuthResponse(token, user.getUsername(), user.getRole().name());
	}
    
    
}
