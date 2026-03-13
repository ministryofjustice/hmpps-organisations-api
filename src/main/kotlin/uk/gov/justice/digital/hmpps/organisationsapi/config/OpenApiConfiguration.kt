package uk.gov.justice.digital.hmpps.organisationsapi.config

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.core.util.PrimitiveType
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.tags.Tag
import jakarta.annotation.PostConstruct
import org.hibernate.internal.util.collections.CollectionHelper.listOf
import org.openapitools.jackson.nullable.JsonNullableModule
import org.springframework.boot.info.BuildProperties
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.DeserializationFeature

@Configuration
class OpenApiConfiguration(buildProperties: BuildProperties) {
  private val version: String? = buildProperties.version

  @Bean
  fun customOpenAPI(buildProperties: BuildProperties): OpenAPI = OpenAPI()
    .components(
      Components().addSecuritySchemes(
        "bearer-jwt",
        SecurityScheme()
          .type(SecurityScheme.Type.HTTP)
          .scheme("bearer")
          .bearerFormat("JWT")
          .`in`(SecurityScheme.In.HEADER)
          .name("Authorization"),
      ),
    )
    .info(
      Info()
        .title("Organisations API")
        .version(version)
        .description("API for the management of organisations and their contact details.")
        .contact(
          Contact()
            .name("HMPPS Digital Studio")
            .email("feedback@digital.justice.gov.uk"),
        ),
    )
    .tags(
      listOf(
        Tag().name("Migration and synchronisation").description("Migration and synchronisation endpoints"),
        Tag().name("Reference data").description("Reference data endpoints"),
        Tag().name("Organisations").description("Organisation endpoints"),
      ),
    )
    .addSecurityItem(SecurityRequirement().addList("bearer-jwt", listOf("read", "write")))
    .servers(
      listOf(
        Server().url("/").description("Default - this environment"),
        Server().url("https://organisations-api-dev.hmpps.service.justice.gov.uk").description("Development"),
        Server().url("https://organisations-api-preprod.hmpps.service.justice.gov.uk").description("Pre-production"),
        Server().url("https://organisations-api.hmpps.service.justice.gov.uk").description("Production"),
      ),
    )

  @PostConstruct
  fun enableLocalTimePrimitiveType() {
    PrimitiveType.enablePartialTime()
  }

  @Bean
  fun jsonNullableModule() = JsonNullableModule()

  @Bean
  fun jsonCustomizer(): JsonMapperBuilderCustomizer = JsonMapperBuilderCustomizer { builder ->
    builder.changeDefaultPropertyInclusion {
      JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL)
    }
    builder.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
  }
}
