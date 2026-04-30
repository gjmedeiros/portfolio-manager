package com.portfolio.manager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

		private static final String[] PUBLIC_PATHS = {
				"/swagger-ui/**",
				"/swagger-ui.html",
				"/api-docs/**",
				"/v3/api-docs/**",
				"/actuator/health"
		};

		@Bean
		public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
				http
						.csrf(AbstractHttpConfigurer::disable)
						.sessionManagement(session ->
								session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
						.authorizeHttpRequests(auth -> auth
								.requestMatchers(PUBLIC_PATHS).permitAll()
								.anyRequest().authenticated()
						)
						.httpBasic(Customizer.withDefaults());

				return http.build();
		}

		@Bean
		public UserDetailsService userDetailsService(PasswordEncoder encoder) {

				UserDetails admin = User.builder()
						.username("admin")
						.password(encoder.encode("admin123"))
						.build();

				return new InMemoryUserDetailsManager(admin);
		}

		@Bean
		public PasswordEncoder passwordEncoder() {
				return new BCryptPasswordEncoder();
		}
}
