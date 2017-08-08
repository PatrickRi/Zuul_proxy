package at.ac.wu.web.crawlers.thesis;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Created by Patrick on 05.08.2017.
 */
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/monitor/**")
                .hasRole("ACTRADMIN")
                .and()
                .httpBasic() //otherwise formLogin
                .and()
                .authorizeRequests()
                .antMatchers("*/**", "*", "/*", "/**")
                .permitAll()
                .and()
                .csrf()
                .disable()
                .cors().disable()
        ;
    }
}
