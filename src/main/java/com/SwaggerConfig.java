package com;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(title = "[PROJECT] barogagi API", version = "v1", description = "프로젝트 바로가기 API 명세서")
)

@Configuration
public class SwaggerConfig {
}
