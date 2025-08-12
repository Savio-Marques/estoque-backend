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
        // (1) Define as informações gerais da sua API
        info = @Info(
                title = "API de Controle de Estoque",
                version = "v1",
                description = "API para gerenciamento de produtos, categorias e devedores para pequenos comércios."
        ),
        // (2) Define a ordem em que os grupos (Tags) aparecerão
        tags = {
                @Tag(name = "Autenticação", description = "Endpoints para registro e login de usuários"),
                @Tag(name = "Categorias", description = "Operações para gerenciar categorias de produtos"),
                @Tag(name = "Produtos", description = "Operações para gerenciar produtos e estoque"),
                @Tag(name = "Devedores", description = "Operações para gerenciar devedores"),
                @Tag(name = "Usuários", description = "Operações para gerenciar usuários")
                // Adicione outras tags aqui na ordem que desejar
        },
        // (3) Aplica a segurança JWT em todos os endpoints (exceto os liberados no SecurityConfig)
        security = @SecurityRequirement(name = "bearerAuth")
)
// (4) Define como a segurança JWT funciona para o Swagger UI
@SecurityScheme(
        name = "bearerAuth", // Nome de referência para a segurança
        type = SecuritySchemeType.HTTP, // O tipo de segurança
        scheme = "bearer", // O esquema a ser usado
        bearerFormat = "JWT" // O formato do token
)
public class OpenApiConfig {

}