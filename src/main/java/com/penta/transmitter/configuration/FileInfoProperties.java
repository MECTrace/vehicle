package com.penta.transmitter.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "file.location")
@Getter
@ConstructorBinding
@RequiredArgsConstructor
public class FileInfoProperties {

    private final String target;
    private final String done;

}
