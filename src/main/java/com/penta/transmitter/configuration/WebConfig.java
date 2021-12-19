package com.penta.transmitter.configuration;


/*

// 사용안함
// 차량별 인증서 매핑이 필요하므로 차량별 template 사용으로 변경
@Configuration
public class WebConfig {

    @Value("${server.ssl.key-store}")
    private String keyStorePath;

    @Value("${server.ssl.key-store-password}")
    private String keyStorePassword;


    @Value("${server.ssl.trust-store}")
    private String trustKeyStorePath;

    @Value("${server.ssl.trust-store-password}")
    private String trustKeyStorePassword;

    // Request에 인증서를 매핑
    @Bean
    public RestTemplate restTemplate() throws Exception {
            SSLContext sslContext = new SSLContextBuilder()
                    .loadKeyMaterial(Paths.get(keyStorePath).toUri().toURL(), keyStorePassword.toCharArray(), keyStorePassword.toCharArray())
                    .loadTrustMaterial(Paths.get(trustKeyStorePath).toUri().toURL(), trustKeyStorePassword.toCharArray())
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

 */
