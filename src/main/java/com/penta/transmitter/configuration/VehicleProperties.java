package com.penta.transmitter.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "vehicle.cert")
@ConstructorBinding
@RequiredArgsConstructor
@Getter
public class VehicleProperties {

    private final String path;
    private final String password;
    private final String trustStorePath;
    private final String trustStorePassword;

}
