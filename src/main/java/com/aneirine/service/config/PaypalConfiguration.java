package com.aneirine.service.config;

import com.paypal.base.rest.OAuthTokenCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class PaypalConfiguration {

    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Value("${paypal.mode}")
    private String mode;

    @Bean
    public Map<String, String> paypalSdkConfiguration() {
        return new HashMap<String, String>() {{
            put("mode", mode);
        }};
    }

    @Bean
    public OAuthTokenCredential tokenCredential(){
        return new OAuthTokenCredential(clientId, clientSecret);
    }
}
