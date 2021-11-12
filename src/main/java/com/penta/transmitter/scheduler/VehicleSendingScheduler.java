package com.penta.transmitter.scheduler;

import com.penta.transmitter.configuration.FileInfoProperties;
import com.penta.transmitter.constant.VehicleCertMap;
import com.penta.transmitter.domain.VehicleCert;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Component
@Slf4j
public class VehicleSendingScheduler {

    private VehicleCertMap vehicleCertMap;

    private final Path targetLocation;
    private final Path doneLocation;
    private final Environment environment;

    public VehicleSendingScheduler(FileInfoProperties properties, VehicleCertMap vehicleCertMap, Environment environment) {
        this.targetLocation = Paths.get(properties.getTarget()).toAbsolutePath().normalize();
        this.doneLocation = Paths.get(properties.getDone()).toAbsolutePath().normalize();
        this.vehicleCertMap = vehicleCertMap;
        this.environment = environment;
    }

    @PostConstruct
    @SneakyThrows
    public void init() {
        Files.createDirectories(targetLocation);
        Files.createDirectories(doneLocation);
    }


    @Scheduled(fixedDelay = 60000)
    @SneakyThrows
    public void sendToEdge() {

        int port = Integer.parseInt(environment.getProperty("server.port"));

        log.info("------- ------- sendToEdge 실행 ------- -------");

        if (checkFilePresent(this.targetLocation)) {

            log.info("------- ------- {} ------- -------",this.targetLocation.toString());
            log.info("------- ------- file exists ------- -------");

            File dir = new File(this.targetLocation.toString());
            File[] fileList = dir.listFiles();

            /*
            * 파일명에 차량번호가 존재하고 & VehicleCertMap에 등록된 차량번호인 경우에만 전송.
            * 아닌 경우 별도의 로직 없이 전송 X
            * */
            Map<String, List<File>> fileMap = Arrays.stream(fileList)
                    .filter(file -> verifyFile(file.getName().replaceAll(" ","")))
                    .filter(file -> {
                        String carNo = getCarNo(file.getName());
                        int lastNumberOfCarNo = Character.getNumericValue(carNo.charAt(carNo.length()-1));
                        if(port == 8082) {
                            // 차량번호 뒷자리가 0 또는 짝수
                            return lastNumberOfCarNo == 0 || lastNumberOfCarNo%2 == 0;
                        } else {
                            // 차량번호 뒷자리가 홀수
                            return lastNumberOfCarNo%2 != 0;
                        }
                    })
                    .collect(Collectors.groupingBy(file -> getCarNo(file.getName())));

            Object[] keyArray = fileMap.keySet().toArray();
            int limited = 0;
            if(keyArray.length > 10) {
                limited = keyArray.length/2;  // 전체파일(홀or짝)의 절반만 쓰레드 생성
            } else {
                limited = keyArray.length;    // 차량갯수가 10개 이하일 경우 차량갯수만큼 쓰레드 생성
            }

            Thread[] t = new Thread[limited];

            for(int i = 0; i < limited; i++) {
                String key = keyArray[i].toString();
                List<File> values = fileMap.get(key);
                VehicleCert vehicleCert = vehicleCertMap.getVehicleCertInfo(key);
                Runnable r = new SendingThread(key, values.get(0), vehicleCert, this.targetLocation, this.doneLocation);
                t[i] = new Thread(r);
                t[i].start();
            }

        } else {
            log.info("------- {} 에 파일이 존재하지 않음 -------",this.targetLocation.toString());
        }

    }


    @SneakyThrows
    private boolean verifyFile(String fileName) {
        String carNo = getCarNo(fileName);
        log.info("------- ------- verifyFile() ------- -------");
        log.info("fileName :: {} ", fileName);
        log.info("carNo :: {} ", carNo);
        return StringUtils.hasText(carNo) ? vehicleCertMap.hasVehicleNo(carNo) : false;
    }

    private String getCarNo(String fileName) {
        String trimStr = fileName.trim().replaceAll(" ","");
        Pattern pattern = Pattern.compile("\\d{2,3}[가-힣]{1}\\d{4}");
        Matcher matcher = pattern.matcher(trimStr);
        return matcher.find() ? matcher.group() : "";
    }

    @SneakyThrows
    private boolean checkFilePresent(Path path) {
        try (Stream<Path> entries = Files.list(path)) {
            return entries.filter(f -> !(f.toString().contains(".DS_Store")) && !(f.toFile().isDirectory())).findFirst().isPresent();
        }
    }


}

