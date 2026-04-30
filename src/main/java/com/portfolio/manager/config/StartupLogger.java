package com.portfolio.manager.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StartupLogger implements ApplicationListener<ApplicationReadyEvent> {

		private final Environment env;

		public StartupLogger(Environment env) {
				this.env = env;
		}

		@Override
		public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
				String port = env.getProperty("server.port", "8080");
				String contextPath = env.getProperty("server.servlet.context-path", "");
				String base = "http://localhost:" + port + contextPath;

				log.info("");
				log.info("┌─────────────────────────────────────────────────────────┐");
				log.info("│           Portfolio Manager - Aplicação iniciada         │");
				log.info("├─────────────────────────────────────────────────────────┤");
				log.info("│  Swagger UI  →  {}/swagger-ui.html", base);
				log.info("│  API Docs    →  {}/api-docs", base);
				log.info("├─────────────────────────────────────────────────────────┤");
				log.info("│  Usuários:  viewer / manager / admin                     │");
				log.info("└─────────────────────────────────────────────────────────┘");
		}
}