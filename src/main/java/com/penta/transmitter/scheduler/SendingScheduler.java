package com.penta.transmitter.scheduler;

import com.penta.transmitter.configuration.FileInfoProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.*;
import java.util.stream.Stream;

@Component
@Slf4j
public class SendingScheduler {

    @Value("${server.ssl.key-store}")
    private String certFilePath;

    @Value("${server.ssl.key-store-password}")
    private String keyPassword;

    @Value("${target.edge-node.url}")
    private String edgeNodeURL;



    private final Path targetLocation;
    private final Path doneLocation;

    private final RestTemplate restTemplate;

    public SendingScheduler(FileInfoProperties properties, RestTemplate restTemplate) {
        this.targetLocation = Paths.get(properties.getTarget()).toAbsolutePath().normalize();
        this.doneLocation = Paths.get(properties.getDone()).toAbsolutePath().normalize();
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    @SneakyThrows
    public void init() {
        Files.createDirectories(targetLocation);
        Files.createDirectories(doneLocation);
    }


    @Scheduled(fixedDelay = 500000)
    @SneakyThrows
    public void sendToEdge() {

        if (checkFilePresent(this.targetLocation)) {
            File dir = new File(this.targetLocation.toString());
            File[] fileList = dir.listFiles();

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            for (File file : fileList) {
                if (!file.isDirectory() && !file.getName().contains(".DS_Store")) {
                    body.add("file", new FileSystemResource(file));

                    // 서명
                    Signature signature = Signature.getInstance("SHA256withRSA");
                    signature.initSign(getPrivateKey(this.certFilePath));

                    byte[] fileBytes = Files.readAllBytes(file.toPath());
                    signature.update(fileBytes);
                    byte[] digitalSignature = signature.sign();

                    // body에 디지털 서명값 추가
                    ByteArrayResource resource = new ByteArrayResource(digitalSignature) {
                        @Override
                        public String getFilename() { return "signature"; }
                    };

                    body.add("signature", resource);

                }
            }

            HttpHeaders header = new HttpHeaders();
            header.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, header);

            ResponseEntity<String> response = restTemplate.postForEntity(edgeNodeURL, requestEntity, String.class);


            // 5xx 응답 : edge server error
            if (String.valueOf(response.getStatusCodeValue()).startsWith("5")) {
                log.error("[5xx FAIL] Response :: " + response);
            } else if (String.valueOf(response.getStatusCodeValue()).startsWith("4")) {
                // 4xx 응답 : 요청 에러
                log.error("[4xx FAIL] Response :: " + response);
            } else {
                log.info("[SUCCESS] Response :: " + response);
                for (File file : fileList) {
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

    @SneakyThrows
    private PrivateKey getPrivateKey(String certFilePath) {

        // todo cerFilePath 왜 no such file or directory 로 나오는지? 하드코딩 지우고 확인해볼것
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(certFilePath), this.keyPassword.toCharArray());
        // keyStore.load(new FileInputStream("/Users/penta/IdeaProjects/cloudEdge/transmitter/src/main/resources/client-key.jks"), this.keyPassword.toCharArray());
        PrivateKey key = (PrivateKey) keyStore.getKey("client-key", this.keyPassword.toCharArray());
        return (PrivateKey) keyStore.getKey("client-key", this.keyPassword.toCharArray());
    }

    @SneakyThrows
    private boolean checkFilePresent(Path path) {
        try (Stream<Path> entries = Files.list(path)) {
            return entries.filter(f -> !(f.toString().contains(".DS_Store")) && !(f.toFile().isDirectory())).findFirst().isPresent();
        }
    }
}
