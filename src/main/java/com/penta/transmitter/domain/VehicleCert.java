package com.penta.transmitter.domain;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class VehicleCert {

    private String certPath;
    private String certPassword;

    private String trustStorePath;
    private String trustStorePassword;

    private String certAlias;

}
