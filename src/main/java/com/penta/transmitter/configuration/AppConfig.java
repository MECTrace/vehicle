package com.penta.transmitter.configuration;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationPropertiesScan("com.penta.transmitter.configuration")
@Configuration
public class AppConfig {
}
