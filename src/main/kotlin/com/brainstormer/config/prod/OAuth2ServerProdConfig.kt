package com.brainstormer.config.prod

import com.brainstormer.service.CustomUserDetailsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore

import javax.sql.DataSource

@Configuration
@Profile("prod")
class OAuth2ServerProdConfig {

    @Profile("prod")
    @Configuration
    @EnableResourceServer
    internal class ResourceServerConfiguration : ResourceServerConfigurerAdapter() {

        private val RESOURCE_ID = "brainstormer_backend"

        @Autowired
        private val tokenStore: TokenStore? = null

        override fun configure(resources: ResourceServerSecurityConfigurer?) {
            // @formatter:off
            resources!!
                    .tokenStore(tokenStore)
                    .resourceId(RESOURCE_ID)
            // @formatter:on
        }

        @Throws(Exception::class)
        override fun configure(http: HttpSecurity) {
            // @formatter:off
            http
                    .authorizeRequests()
                    .antMatchers("/**").fullyAuthenticated()
            // @formatter:on
        }
    }

    @Profile("prod")
    @Configuration
    @EnableAuthorizationServer
    internal class AuthorizationServerConfiguration : AuthorizationServerConfigurerAdapter() {

        private val RESOURCE_ID = "brainstormer_backend"

        @Autowired
        @Qualifier("authenticationManagerBean")
        private val authenticationManager: AuthenticationManager? = null

        @Autowired
        private val userDetailsService: CustomUserDetailsService? = null

        @Autowired
        private val dataSource: DataSource? = null

        @Bean
        fun passwordEncoder(): PasswordEncoder {
            return BCryptPasswordEncoder()
        }

        @Bean
        fun tokenStore(): JdbcTokenStore {
            return JdbcTokenStore(dataSource!!)
        }

        @Bean
        protected fun authorizationCodeServices(): AuthorizationCodeServices {
            return JdbcAuthorizationCodeServices(dataSource!!)
        }

        @Throws(Exception::class)
        override fun configure(security: AuthorizationServerSecurityConfigurer?) {
            security!!.passwordEncoder(passwordEncoder())
        }

        @Throws(Exception::class)
        override fun configure(endpoints: AuthorizationServerEndpointsConfigurer?) {
            // @formatter:off
            endpoints!!
                    .authorizationCodeServices(authorizationCodeServices())
                    .tokenStore(tokenStore())
                    .authenticationManager(this.authenticationManager)
                    .userDetailsService(userDetailsService)
            // @formatter:on
        }

        @Throws(Exception::class)
        override fun configure(clients: ClientDetailsServiceConfigurer?) {
            // @formatter:off
            clients!!
                    .jdbc(dataSource)
                    .passwordEncoder(passwordEncoder())
                    .withClient("brainstormer_mobile")
                    .authorizedGrantTypes("password", "refresh_token")
                    .authorities("USER")
                    .scopes("read", "write")
                    .resourceIds(RESOURCE_ID)
                    .secret("bsm_secret")
                    .and()
                    .withClient("brainstormer_web_frontend")
                    .authorizedGrantTypes("password", "refresh_token")
                    .authorities("USER")
                    .scopes("read", "write")
                    .resourceIds(RESOURCE_ID)
                    .secret("bswf_secret")
            // @formatter:on
        }
    }
}
