package com.penta.transmitter.constant;

import com.penta.transmitter.configuration.VehicleProperties;
import com.penta.transmitter.domain.VehicleCert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class VehicleCertMap {

    private final Map<String, VehicleCert> map;

    public VehicleCertMap(VehicleProperties vehicleProperties) {

        Map<String, VehicleCert> vehicleMap = new HashMap<>();

        String certPath = vehicleProperties.getPath();
        String certPassword = vehicleProperties.getPassword();
        String trustStorePath = vehicleProperties.getTrustStorePath();
        String trustStorePassword = vehicleProperties.getTrustStorePassword();

        vehicleMap.put("02구2392",VehicleCert.builder().certAlias("car-1").certPath(certPath+"car-1.p12").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("02보4305",VehicleCert.builder().certAlias("car-2").certPath(certPath+"car-2.p12").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("07서3924",VehicleCert.builder().certAlias("car-3").certPath(certPath+"car-3.p12").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("08가8030",VehicleCert.builder().certAlias("car-4").certPath(certPath+"car-4.p12").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("17고2507",VehicleCert.builder().certAlias("car-5").certPath(certPath+"car-5.p12").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("20수6983",VehicleCert.builder().certAlias("car-6").certPath(certPath+"car-6.p12").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("21부2270",VehicleCert.builder().certAlias("car-7").certPath(certPath+"car-7.p12").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("25가7084",VehicleCert.builder().certAlias("car-8").certPath(certPath+"car-8.p12").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("26도4136",VehicleCert.builder().certAlias("car-9").certPath(certPath+"car-9.p12").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("27수2650",VehicleCert.builder().certAlias("car-10").certPath(certPath+"car-10.p12").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("29로9704",VehicleCert.builder().certAlias("car-11").certPath(certPath+"car-11.p12").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("29하4923",VehicleCert.builder().certAlias("car-12").certPath(certPath+"car-12.p12").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("29하4925",VehicleCert.builder().certAlias("car-13").certPath(certPath+"car-13.p12").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("31도7070",VehicleCert.builder().certAlias("car-14").certPath(certPath+"car-14.p12").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("32주6679",VehicleCert.builder().certAlias("car-15").certPath(certPath+"car-15.p12").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("33더7680",VehicleCert.builder().certAlias("car-16").certPath(certPath+"car-16.p12").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("33라8981",VehicleCert.builder().certAlias("car-17").certPath(certPath+"car-17.p12").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("34머0364",VehicleCert.builder().certAlias("car-18").certPath(certPath+"car-18.p12").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("47부6975",VehicleCert.builder().certAlias("car-19").certPath(certPath+"car-19.p12").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("49호5165",VehicleCert.builder().certAlias("car-20").certPath(certPath+"car-20.p12").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("50루5982",VehicleCert.builder().certAlias("car-21").certPath(certPath+"car-21.p12").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("56모1782",VehicleCert.builder().certAlias("car-22").certPath(certPath+"car-22.p12").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("58고4210",VehicleCert.builder().certAlias("car-23").certPath(certPath+"car-23.p12").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("62모1074",VehicleCert.builder().certAlias("car-24").certPath(certPath+"car-24.p12").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());


        /*
                vehicleMap.put("02구2392",VehicleCert.builder().certAlias("client-key").certPath(certPath+"client-key.jks").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("02보4305",VehicleCert.builder().certAlias("client-key-2").certPath(certPath+"client-key-2.jks").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());

        vehicleMap.put("07서3924",VehicleCert.builder().certAlias("client-key-3").certPath(certPath+"client-key-3.jks").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("08가8030",VehicleCert.builder().certAlias("client-key-4").certPath(certPath+"client-key-4.jks").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("17고2507",VehicleCert.builder().certAlias("client-key-5").certPath(certPath+"client-key-5.jks").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("20수6983",VehicleCert.builder().certAlias("client-key-6").certPath(certPath+"client-key-6.jks").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("21부2270",VehicleCert.builder().certAlias("client-key-7").certPath(certPath+"client-key-7.jks").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("25가7084",VehicleCert.builder().certAlias("client-key-8").certPath(certPath+"client-key-8.jks").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("26도4136",VehicleCert.builder().certAlias("client-key-9").certPath(certPath+"client-key-9.jks").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("27수2650",VehicleCert.builder().certAlias("client-key-10").certPath(certPath+"client-key-10.jks").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("29로9704",VehicleCert.builder().certAlias("client-key-11").certPath(certPath+"client-key-11.jks").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("29하4923",VehicleCert.builder().certAlias("client-key-12").certPath(certPath+"client-key-12.jks").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("29하4925",VehicleCert.builder().certAlias("client-key-13").certPath(certPath+"client-key-13.jks").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("31도7070",VehicleCert.builder().certAlias("client-key-14").certPath(certPath+"client-key-14.jks").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("32주6679",VehicleCert.builder().certAlias("client-key-15").certPath(certPath+"client-key-15.jks").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("33더7680",VehicleCert.builder().certAlias("client-key-16").certPath(certPath+"client-key-16.jks").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("33라8981",VehicleCert.builder().certAlias("client-key-17").certPath(certPath+"client-key-17.jks").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("34머0364",VehicleCert.builder().certAlias("client-key-18").certPath(certPath+"client-key-18.jks").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("47부6975",VehicleCert.builder().certAlias("client-key-19").certPath(certPath+"client-key-19.jks").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("49호5165",VehicleCert.builder().certAlias("client-key-20").certPath(certPath+"client-key-20.jks").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("50루5982",VehicleCert.builder().certAlias("client-key-21").certPath(certPath+"client-key-21.jks").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("56모1782",VehicleCert.builder().certAlias("client-key-22").certPath(certPath+"client-key-22.jks").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("58고4210",VehicleCert.builder().certAlias("client-key-23").certPath(certPath+"client-key-23.jks").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
        vehicleMap.put("62모1074",VehicleCert.builder().certAlias("client-key-24").certPath(certPath+"client-key-24.jks").certPassword(certPassword).trustStorePath(trustStorePath).trustStorePassword(trustStorePassword).build());
         */
        map = Collections.unmodifiableMap(vehicleMap);


    }


    public VehicleCert getVehicleCertInfo(String vehicleNo) {
        return map.get(vehicleNo);
    }

    public boolean hasVehicleNo(String vehicleNo){
        return map.containsKey(vehicleNo);
    }


}
