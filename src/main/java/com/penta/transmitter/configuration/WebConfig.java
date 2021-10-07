package com.penta.transmitter.configuration;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.net.URL;


@Configuration
public class WebConfig {

    @Value("${server.ssl.key-store}")
    private URL keyStorePath;

    @Value("${server.ssl.key-store-password}")
    private String keyStorePassword;


    @Value("${server.ssl.trust-store}")
    private URL trustKeyStorePath;

    @Value("${server.ssl.trust-store-password}")
    private String trustKeyStorePassword;

    // Request에 인증서를 매핑
    @Bean
    public RestTemplate restTemplate() throws Exception {
            SSLContext sslContext = new SSLContextBuilder()
                    .loadKeyMaterial(keyStorePath, keyStorePassword.toCharArray(), keyStorePassword.toCharArray())
                    .loadTrustMaterial(trustKeyStorePath, trustKeyStorePassword.toCharArray())
                    .build();
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
            HttpClient httpClient = HttpClients.custom()
                    //.setSSLHostnameVerifier((hostname, session)->true)
                    .setSSLSocketFactory(socketFactory)
                    .build();
            HttpComponentsClientHttpRequestFactory factory =
                    new HttpComponentsClientHttpRequestFactory(httpClient);
            return new RestTemplate(factory);

    }

}
