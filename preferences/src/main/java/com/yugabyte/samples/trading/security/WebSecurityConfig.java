package com.yugabyte.samples.trading.security;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
  jsr250Enabled = true,
  securedEnabled = true,
  prePostEnabled = true
)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {


  private final DataSource dataSource;

  private final JwtCodec jwtCodec;

  @Autowired
  public WebSecurityConfig(DataSource dataSource, JwtCodec jwtCodec) {
    this.dataSource = dataSource;
    this.jwtCodec = jwtCodec;
  }


  @Override
  protected void configure(HttpSecurity http) throws Exception {

    http
      .cors()
      .and()
      .csrf()
      .disable()
      .exceptionHandling()
//      .authenticationEntryPoint(new Http403ForbiddenEntryPoint())
      .and()
      .sessionManagement()
      .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
      .exceptionHandling()
      .and()
      .authorizeRequests()
      // Static assets
      .antMatchers("/",
        "/static/**/*",
        "/asset-manifest.json",
        "/favicon.ico",
        "/index.html",
        "/logo192.png",
        "/logo512.png",
        "/manifest.json",
        "/robots.txt"
      )
      .permitAll()
      // API Docs
      .antMatchers(        "/api-docs",
        "/api-docs.json",
        "/api-docs.html",
        "/api-docs.yaml",
        "/api-docs.yml",
        "/swagger-ui/**"
      )
      .permitAll()
      // Registration and Authentication endpoints
      .antMatchers(
        "/api/v1/users/sign-up",
        "/api/v1/users/sign-in",
        "/api/v1/users/sign-out",
        "/api/v1/users/check-availability"
      )
      .permitAll()
      .anyRequest()
      .authenticated();
//      .and()
//      .formLogin()
//      .loginProcessingUrl("/api/v1/users/sign-in")
//      .failureForwardUrl("/#/sign-in?error=login-failed")
//      .loginPage("/#/sign-in")
//      .permitAll()
//      .and()
//      .logout()
//      .logoutUrl("/api/v1/users/sign-out")
//      .logoutSuccessUrl("/#/home")
//      .permitAll();

    http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

  }


  @Bean
  public CorsFilter corsFilter() {
    UrlBasedCorsConfigurationSource source =
      new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOrigin("*");
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    source.registerCorsConfiguration("/**", config);

    return new CorsFilter(source);
  }


  @Bean
  @Override
  protected AuthenticationManager authenticationManager() throws Exception {
    return super.authenticationManager();
  }

  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter() {
    return new JwtAuthenticationFilter(jwtCodec, users());
  }

  @Bean
  public UserDetailsManager users() {
    return new JdbcUserDetailsManager(dataSource);
  }

  @Override
  public void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth
      .jdbcAuthentication()
      .dataSource(dataSource)
      .passwordEncoder(passwordEncoder());
//      .usersByUsernameQuery("select username, password, enabled from users where username=?")
//      .authoritiesByUsernameQuery("select username, role from users where username=?")
    ;

  }


  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}