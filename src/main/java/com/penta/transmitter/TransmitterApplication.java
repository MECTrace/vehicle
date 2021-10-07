package com.penta.transmitter;

import com.penta.transmitter.configuration.FileInfoProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableConfigurationProperties(
        {FileInfoProperties.class}
)
@SpringBootApplication
@EnableScheduling
public class TransmitterApplication {

    public static void main(String[] args) {

        SpringApplication.run(TransmitterApplication.class, args);


    }



}
