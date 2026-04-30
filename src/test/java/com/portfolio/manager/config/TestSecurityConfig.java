package com.portfolio.manager.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
public class TestSecurityConfig {

		@Bean
		public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
				http.csrf(AbstractHttpConfigurer::disable)
						.authorizeHttpRequests(auth -> auth
								.requestMatchers("/api/v1/**").authenticated()
								.anyRequest().permitAll()
						)
						.httpBasic(org.springframework.security.config.Customizer.withDefaults());
				return http.build();
		}
}
