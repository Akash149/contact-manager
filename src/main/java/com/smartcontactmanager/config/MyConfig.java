package com.smartcontactmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;

@Configuration
@EnableWebSecurity
@Component
public class MyConfig {
    
    @Bean
    public UserDetailsService getUserDetailService() {
        return new UserDetailsServiceImpl();
    }

    //To Encrypt password
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //To javaMailSenter
    // @Bean
    // public JavaMailSender getJavaMailSender() {
    //     JavaMailSenderImpl mailsender = new JavaMailSenderImpl();
    //     mailsender.setHost("smtp.gmail.com");
    //     mailsender.setPort(587);

    //     mailsender.setUsername("mr.iotdeveloper@gmail.com");
    //     mailsender.setPassword("vdjqqokyccjubgzh");

    //     Properties props = mailsender.getJavaMailProperties();
    //     props.put("mail.transport.protocol", "smtp");
    //     props.put("mail.smtp.auth","true");
    //     props.put("mial.smtp.starttls.enable","true");
    //     props.put("mail.debug","true");

    //     return mailsender;
    // }

    @Bean
    public DaoAuthenticationProvider DaoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(this.getUserDetailService());
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    // @Bean
    // public WebSecurityCustomizer webSecurityCustomizer() {
    //     return (web) -> web.ignoring().requestMatchers("/");
    // }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests().requestMatchers("/admin/**")
        .hasRole("ADMIN")
        .requestMatchers("/user/**")
        .hasRole("USER")
        .requestMatchers("/**")
        .permitAll()
        .and().formLogin()
        .loginPage("/signin")
        .loginProcessingUrl("/signin")
        .defaultSuccessUrl("/user/index")
        // .failureUrl("/error")
        .and().csrf().disable();
        http.authenticationProvider(DaoAuthenticationProvider());
        DefaultSecurityFilterChain builds = http.build();
        return builds;

    }

    //Authentication manager
    @Bean
    public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
    
}
