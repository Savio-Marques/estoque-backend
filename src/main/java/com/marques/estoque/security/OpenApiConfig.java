package com.marques.estoque.security;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API de Controle de Estoque",
                version = "v1",
                description = "API para gerenciamento de produtos, categorias e devedores para pequenos comércios."
        ),
        tags = {
                @Tag(name = "Autenticação", description = "Endpoints para registro e login de usuários"),
                @Tag(name = "Categorias", description = "Operações para gerenciar categorias de produtos"),
                @Tag(name = "Produtos", description = "Operações para gerenciar produtos e estoque"),
                @Tag(name = "Devedores", description = "Operações para gerenciar devedores"),
                @Tag(name = "Usuários", description = "Operações para gerenciar usuários")
        },
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

}