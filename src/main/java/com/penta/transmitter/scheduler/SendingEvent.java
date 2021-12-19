package com.penta.transmitter.scheduler;

import com.penta.transmitter.configuration.FileInfoProperties;
import com.penta.transmitter.constant.EdgeNode;
import com.penta.transmitter.constant.VehicleCertMap;
import com.penta.transmitter.domain.VehicleCert;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class SendingEvent implements ApplicationListener<ApplicationStartedEvent> {

    private VehicleCertMap vehicleCertMap;

    private final Path targetLocation;
    private final Path doneLocation;
    private final Environment environment;


    public SendingEvent(FileInfoProperties properties, VehicleCertMap vehicleCertMap, Environment environment) {
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

    @SneakyThrows
    @Override
    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {

        //log.info("------- ------- sendToEdge 실행 ------- -------");

        if (checkFilePresent(this.targetLocation)) {

            //log.info("------- ------- file exists ------- -------");

            File dir = new File(this.targetLocation.toString());
            File[] fileList = dir.listFiles();

            /*
             * 파일명에 차량번호가 존재하고 & VehicleCertMap에 등록된 차량번호인 경우에만 전송.
             * 아닌 경우 별도의 로직 없이 전송 X
             * */
            Map<String, List<File>> fileMap = Arrays.stream(fileList)
                    .filter(file -> verifyFile(file.getName().replaceAll(" ", "")))
                    .collect(Collectors.groupingBy(file -> getCarNo(file.getName())));

            Object[] keyArray = fileMap.keySet().toArray();

            int count = 1;
            for (int i = 0; i < keyArray.length; i++) {
                String key = keyArray[i].toString();
                List<File> values = fileMap.get(key);
                VehicleCert vehicleCert = vehicleCertMap.getVehicleCertInfo(key);
                for (File file : values) {

                    log.info("▣▣▣▣▣▣▣▣▣▣▣▣▣▣▣▣▣count :: {}",count++ +"▣▣▣▣▣▣▣▣▣▣▣▣▣▣▣▣");
                    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                    body.add("file", new FileSystemResource(file));
                    body.add("signature", getSignatureResource(file, vehicleCert));

                    ResponseEntity<String> response = sendRequest(body, vehicleCert);

                    if (isSuccess(response)) {
                        if (!file.isDirectory() && !file.getName().contains(".DS_Store")) {
                            // TODO 전송이 끝난 파일 done 폴더로 이동(임시) >> 테스트 끝나면 삭제로 변경할것
                            Path orgLocation = this.targetLocation.resolve(file.getName());
                            Path movedTarget = this.doneLocation.resolve(file.getName());
                            Files.move(orgLocation, movedTarget, StandardCopyOption.REPLACE_EXISTING);
                            log.info("▣▣▣▣▣▣▣▣▣▣▣▣▣▣▣파일이동 :: {}", file.getName());
                        }
                    }


                }
            }

        } else {
            log.info("------- {} 에 파일이 존재하지 않음 -------", this.targetLocation.toString());
        }

    }


    @SneakyThrows
    private boolean verifyFile(String fileName) {
        String carNo = getCarNo(fileName);
        boolean result = StringUtils.hasText(carNo) ? vehicleCertMap.hasVehicleNo(carNo) : false;
        return result;
    }

    private String getCarNo(String fileName) {
        String trimStr = fileName.trim().replaceAll(" ", "");
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

        // * Getter 필요
        ByteArrayResource resource = new ByteArrayResource(digitalSignature) {
            @Override
            public String getFilename() {
                return "signature";
            }
        };

        return resource;
    }

    private ResponseEntity<String> sendRequest(MultiValueMap<String, Object> body, VehicleCert vehicleCert) {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, header);

        // edge random 전송
        EdgeNode edge = EdgeNode.values()[new Random().nextInt(10)];

        // TODO :: 로컬테스트용 아래 주석 풀고 사용
         /*
        return getRestTemplate(vehicleCert)
                .postForEntity("https://127.0.0.1:8443/api/edge/upload/vehicle/", requestEntity, String.class);
        */

        return getRestTemplate(vehicleCert)
                .postForEntity("https://" + edge.getIP() + ":8443/api/edge/upload/vehicle/", requestEntity, String.class);

    }


    @SneakyThrows
    private RestTemplate getRestTemplate(VehicleCert cert) {

        log.info("------------------------VehicleCert object------------------------");
        log.info("CERTPATH :: {} ", cert.getCertPath());
        log.info("CERT-ALIAS :: {} ", cert.getCertAlias());
        log.info("TRUST_STORE_PATH :: {} ", cert.getTrustStorePath());

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
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(cert.getCertPath()), cert.getCertPassword().toCharArray());
        return (PrivateKey) keyStore.getKey(cert.getCertAlias(), cert.getCertPassword().toCharArray());
    }


}
