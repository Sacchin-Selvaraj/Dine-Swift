package com.dineswift.userservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;

@OpenAPIDefinition(
        info = @Info(
                title = "User Service API",
                version = "1.0",
                description = "API documentation for the User Service"
        ),
        tags = @Tag(
                name = "User Service",
                description = "Operations related to user management"
        ),
        servers = @Server(
                url = "http://localhost:8080",
                description = "Local server"
        )
)
public class OpenApiConfig {
}
