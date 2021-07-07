/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tecnalia.datausage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

/**
 *
 * @author root
 */
@Configuration
public class JWTSecurityConfig extends WebSecurityConfigurerAdapter {

	@Value("${spring.profiles.active:Unknown}")
	private String activeProfile;

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.cors().and().csrf().disable().formLogin().disable().headers().httpStrictTransportSecurity().disable().and()
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.ALWAYS).and()
				.authorizeRequests(authz -> authz.antMatchers("/swagger-ui.html", "/swagger-ui/**","/v3/api-docs/**","/platoontec/**")
						.permitAll().anyRequest().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2.jwt());
	}}

