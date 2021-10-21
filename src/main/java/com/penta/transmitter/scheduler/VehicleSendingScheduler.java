package com.penta.transmitter.scheduler;

import com.penta.transmitter.configuration.FileInfoProperties;
import com.penta.transmitter.constant.VehicleCertMap;
import com.penta.transmitter.domain.VehicleCert;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.*;
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

    @Value("${target.edge-node.url}")
    private String edgeNodeURL;

    private final Path targetLocation;
    private final Path doneLocation;

    public VehicleSendingScheduler(FileInfoProperties properties, VehicleCertMap vehicleCertMap) {
        this.targetLocation = Paths.get(properties.getTarget()).toAbsolutePath().normalize();
        this.doneLocation = Paths.get(properties.getDone()).toAbsolutePath().normalize();
        this.vehicleCertMap = vehicleCertMap;
    }

    @PostConstruct
    @SneakyThrows
    public void init() {
        Files.createDirectories(targetLocation);
        Files.createDirectories(doneLocation);
    }


    @Scheduled(fixedDelay = 300000)
    @SneakyThrows
    public void sendToEdge() {

        log.info("------- ------- sendToEdge 실행 ------- -------");

        if (checkFilePresent(this.targetLocation)) {

            File dir = new File(this.targetLocation.toString());
            File[] fileList = dir.listFiles();

            /*
            * 파일명에 차량번호가 존재하고 & VehicleCertMap에 등록된 차량번호인 경우에만 전송.
            * 아닌 경우 별도의 로직 없이 전송 X
            * */
            Map<String, List<File>> fileMap = Arrays.stream(fileList)
                    .filter(file -> verifyFile(file.getName().replaceAll(" ","")))
                    .collect(Collectors.groupingBy(file -> getCarNo(file.getName().replaceAll(" ",""))));

            for(Map.Entry<String, List<File>> entry : fileMap.entrySet()) {
                ResponseEntity<String> response = sendRequest(entry.getKey(), entry.getValue());
                if(isSuccess(response)) {
                    for(File file: entry.getValue()) {
                        if (!file.isDirectory() && !file.getName().contains(".DS_Store")) {
                            // TODO 전송이 끝난 파일 done 폴더로 이동(임시) >> 테스트 끝나면 삭제로 변경할것
                            Path orgLocation = this.targetLocation.resolve(file.getName());
                            Path movedTarget = this.doneLocation.resolve(file.getName());
                            Files.move(orgLocation, movedTarget, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            }

        }

    }


    @SneakyThrows
    private boolean verifyFile(String fileName) {
        String carNo = getCarNo(fileName);
        return StringUtils.hasText(carNo) ? vehicleCertMap.hasVehicleNo(carNo) : false;
    }

    private String getCarNo(String fileName) {
        Pattern pattern = Pattern.compile("\\d{2,3}[가-힣]{1}\\d{4}");
        Matcher matcher = pattern.matcher(fileName);
        return matcher.find() ? matcher.group() : "";
    }

    @SneakyThrows
    private ResponseEntity<String> sendRequest(String carNo, List<File> files) {

        VehicleCert vehicleCert = vehicleCertMap.getVehicleCertInfo(carNo);

        log.info("vehicleCert's certPath ... :: {}",vehicleCert.getCertPath());

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        for(File file : files) {
            body.add("file", new FileSystemResource(file));
            body.add("signature", getSignatureResource(file, vehicleCert));
        }

        return sendRequest(body, vehicleCert);

    }

    @SneakyThrows
    private boolean isSuccess(ResponseEntity<String> response) {
        // 5xx 응답 : edge server error
        if (String.valueOf(response.getStatusCodeValue()).startsWith("5")) {
            log.error("[5xx FAIL] Response :: " + response);
            return false;
        } else if (String.valueOf(response.getStatusCodeValue()).startsWith("4")) {
            // 4xx 응답 : 요청 에러
            log.error("[4xx FAIL] Response :: " + response);
            return false;
        } else {
            log.info("[SUCCESS] Response :: " + response);
            return true;
        }
    }

    @SneakyThrows
    private ByteArrayResource getSignatureResource(File file, VehicleCert vehicleCert) {
        // 서명
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(getPrivateKey(vehicleCert));

        byte[] fileBytes = Files.readAllBytes(file.toPath());
        signature.update(fileBytes);
        byte[] digitalSignature = signature.sign();

        // body에 디지털 서명값 추가
        ByteArrayResource resource = new ByteArrayResource(digitalSignature) {
            @Override
            public String getFilename() { return "signature"; }
        };

        return resource;
    }

    private ResponseEntity<String> sendRequest(MultiValueMap<String, Object> body, VehicleCert vehicleCert) {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, header);

        return getRestTemplate(vehicleCert).postForEntity(edgeNodeURL, requestEntity, String.class);
    }

    @SneakyThrows
    private RestTemplate getRestTemplate(VehicleCert cert) {

        log.info("------------------------VehicleCert object------------------------");
        log.info("CERTPATH :: {} ",cert.getCertPath());
        log.info("CERT-ALIAS :: {} ",cert.getCertAlias());
        log.info("TRUST_STORE_PATH :: {} ",cert.getTrustStorePath());

        log.info("AS URI >>>>> {}", Paths.get(cert.getCertPath()).toUri().toURL());

        SSLContext sslContext = new SSLContextBuilder()
                .loadKeyMaterial(Paths.get(cert.getCertPath()).toUri().toURL(), cert.getCertPassword().toCharArray(), cert.getCertPassword().toCharArray())
                .loadTrustMaterial(Paths.get(cert.getTrustStorePath()).toUri().toURL(), cert.getTrustStorePassword().toCharArray())
                .build();
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
        HttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(socketFactory)
                .build();
        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        return new RestTemplate(factory);
    }

    @SneakyThrows
    private PrivateKey getPrivateKey(VehicleCert cert) {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(cert.getCertPath()), cert.getCertPassword().toCharArray());
        // keyStore.load(new FileInputStream("/Users/penta/IdeaProjects/cloudEdge/transmitter/src/main/resources/client-key.jks"), this.keyPassword.toCharArray());
        return (PrivateKey) keyStore.getKey(cert.getCertAlias(), cert.getCertPassword().toCharArray());
    }

    @SneakyThrows
    private boolean checkFilePresent(Path path) {
        try (Stream<Path> entries = Files.list(path)) {
            return entries.filter(f -> !(f.toString().contains(".DS_Store")) && !(f.toFile().isDirectory())).findFirst().isPresent();
        }
    }


}

