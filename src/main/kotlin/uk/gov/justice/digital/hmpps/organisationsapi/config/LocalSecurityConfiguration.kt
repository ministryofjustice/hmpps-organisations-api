package uk.gov.justice.digital.hmpps.organisationsapi.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter
import org.springframework.web.filter.OncePerRequestFilter

@Configuration
@Profile("local")
class LocalSecurityConfiguration {

  @Bean
  fun localSecurityFilterChain(http: HttpSecurity): SecurityFilterChain = http
    .csrf(AbstractHttpConfigurer<*, *>::disable)
    .authorizeHttpRequests { it.anyRequest().permitAll() }
    .addFilterBefore(LocalDeveloperAuthenticationFilter(), AnonymousAuthenticationFilter::class.java)
    .build()
}

private class LocalDeveloperAuthenticationFilter : OncePerRequestFilter() {
  override fun doFilterInternal(
    request: HttpServletRequest,
    response: HttpServletResponse,
    filterChain: FilterChain,
  ) {
    val authorities = listOf(
      "ROLE_ORGANISATIONS__R",
      "ROLE_ORGANISATIONS__RW",
      "ROLE_ORGANISATIONS_MIGRATION",
    ).map(::SimpleGrantedAuthority)

    SecurityContextHolder.getContext().authentication =
      UsernamePasswordAuthenticationToken("local-developer", "N/A", authorities)

    filterChain.doFilter(request, response)
  }
}
