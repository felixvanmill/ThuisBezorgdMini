package security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/api/orders/**").hasAnyRole("CUSTOMER", "RESTAURANT_EMPLOYEE", "DELIVERY_PERSON")
                .antMatchers("/api/menu-items/**").hasRole("RESTAURANT_EMPLOYEE")
                .antMatchers("/api/users/**").hasRole("ADMIN")
                .anyRequest().authenticated()
                .and()
                .httpBasic();
    }
}
