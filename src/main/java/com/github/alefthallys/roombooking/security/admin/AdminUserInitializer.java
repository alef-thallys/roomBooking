package com.github.alefthallys.roombooking.security.admin;

import com.github.alefthallys.roombooking.models.User;
import com.github.alefthallys.roombooking.repositories.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminUserInitializer {
	
	@Bean
	public ApplicationRunner adminCreator(UserRepository userRepository, PasswordEncoder encoder) {
		return args -> {
			String adminEmail = "admin@admin.com";
			
			if (userRepository.findByEmail(adminEmail).isEmpty()) {
				User admin = new User();
				admin.setName("Admin");
				admin.setEmail(adminEmail);
				admin.setPhone("12997665045");
				admin.setPassword(encoder.encode("admin123"));
				admin.setRole(User.Role.ADMIN);
				
				userRepository.save(admin);
			}
		};
	}
}
