package com.kvitka.subtaskmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NodeService {

    @Value("${node-properties.jar-file-name}")
    private String jarFileName;
    @Value("${node-properties.directory}")
    private String directoryName;

    private final RestTemplate restTemplate;

    public List<String> sendSubtaskToNode(String url, List<String> args) throws IOException, RestClientException {

        MultiValueMap<String, Object> requestMap = new LinkedMultiValueMap<>();
        requestMap.add("jar", bytesToResource(
                Files.readAllBytes(Paths.get(directoryName + "/" + jarFileName)), jarFileName,
                "application/java-archive"));
        requestMap.add("args", bytesToResource(
                String.join("\n", args).getBytes(), "args.txt",
                "text/plain"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ResponseEntity<String[]> responseEntity = restTemplate.postForEntity(
                url, new HttpEntity<>(requestMap, headers), String[].class);

        if (!responseEntity.getStatusCode().is2xxSuccessful())
            throw new RuntimeException("Failed to send subtask and get result (%s)".formatted(url));

        return new ArrayList<>(List.of(Objects.requireNonNull(responseEntity.getBody())));
    }

    public void saveJarFile(MultipartFile jarFile) throws IOException {
        Files.createDirectories(Paths.get(directoryName));
        BufferedOutputStream stream = new BufferedOutputStream(
                new FileOutputStream(directoryName + "/" + jarFileName));
        stream.write(jarFile.getBytes());
        stream.close();
    }

    private Resource bytesToResource(byte[] content, String fileName, String contentType) {
        return new MockMultipartFile(fileName, fileName, contentType, content).getResource();
    }
}
