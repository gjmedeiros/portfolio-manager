package com.portfolio.manager.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
		info = @Info(
				title = "Portfolio Manager API",
				version = "1.0.0",
				description = """
						Sistema de Gerenciamento de Portfólio de Projetos.
						
						**Usuários disponíveis:**
						- `viewer / viewer123` → somente leitura
						- `manager / manager123` → leitura e escrita (exceto DELETE)
						- `admin / admin123` → acesso total
						
						**Sequência de status permitida:**
						`em análise → análise realizada → análise aprovada → iniciado → planejado → em andamento → encerrado`
						
						O status `cancelado` pode ser aplicado a partir de qualquer etapa.
						""",
				contact = @Contact(name = "Portfolio Manager", email = "dev@portfolio.com")
		),
		servers = {
				@Server(url = "http://localhost:8080", description = "Local")
		}
)
@SecurityScheme(
		name = "basicAuth",
		type = SecuritySchemeType.HTTP,
		scheme = "basic"
)
public class OpenApiConfig {
}
