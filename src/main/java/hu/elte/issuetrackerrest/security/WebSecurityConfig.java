package hu.elte.issuetrackerrest.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

//    private static void handleException(HttpServletRequest req, HttpServletResponse rsp, AuthenticationException e)
//    throws IOException {
//        PrintWriter writer = rsp.getWriter();
//        writer.println(new ObjectMapper().writeValueAsString("Unauthorized"));
//        rsp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http    
//                .cors().and()
                .csrf().disable()
                .authorizeRequests()
                    .antMatchers(HttpMethod.POST, "/api/auth").permitAll()
                    .anyRequest().authenticated()
                    .and()
//                .exceptionHandling().authenticationEntryPoint(WebSecurityConfig::handleException)
//                    .and()
                .addFilter(new JWTAuthenticationFilter(authenticationManager()))
                .addFilter(new JWTAuthorizationFilter(authenticationManager()))
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//                .addFilter(new JwtUsernameAndPasswordAuthenticationFilter(jwtCookieStore, authenticationManager()))
//                .addFilterAfter(new JwtTokenAuthenticationFilter(jwtCookieStore), UsernamePasswordAuthenticationFilter.class)
                
    }

    @Autowired
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .inMemoryAuthentication()
                .withUser("user")
//                .password(passwordEncoder().encode("user"))
                .password("{noop}user")
                .authorities("ROLE_USER");
    }

//    @Bean
//    public BCryptPasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
    
//    @Bean
//    CorsConfigurationSource corsConfigurationSource() {
//      final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//      source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
//      return source;
//    }

}
