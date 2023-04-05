package com.kvitka.subtaskmanager.service;

import com.kvitka.subtaskmanager.dto.NodeInfo;
import com.kvitka.subtaskmanager.dto.NodeStatus;
import com.kvitka.subtaskmanager.dto.SubtaskDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubtaskDistributionService {

    @Value("${node-properties.builder-url}")
    private String builderUrl;
    @Value("${node-properties.send-result-method}")
    private String sendResultMethod;
    @Value("${node-properties.execute-method}")
    private String executeMethod;

    private final AtomicBoolean startAgain = new AtomicBoolean();
    private final AtomicBoolean isStarted = new AtomicBoolean();

    private final AtomicInteger distributionCounter = new AtomicInteger();

    private final RestTemplate restTemplate;
    private final RegistryService registryService;
    private final SubtaskService subtaskService;
    private final NodeService nodeService;

    public synchronized void startDistribution() {
        if (isStarted.get()) {
            startAgain.set(true);
            return;
        }
        new Thread(() -> {
            try {
                distribute();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void distribute() throws InterruptedException {

        isStarted.set(true);
        startAgain.set(false);

        int distributionNumber = distributionCounter.incrementAndGet();

        log.info("Distribution #{} started", distributionNumber);

        for (int i = 0; ; i++) {
            NodeInfo nodeInfo = registryService.getFirstFree();
            if (nodeInfo == null) {
                log.warn("Distribution #{}: no free nodes ({} successful iteration(s) before)",
                        distributionNumber, i);
                break;
            }
            SubtaskDto subtaskDto = subtaskService.getFirstSubtask();
            if (subtaskDto == null) {
                nodeInfo.setStatus(NodeStatus.FREE);
                log.warn("Distribution #{}: no unfinished subtasks ({} successful iteration(s) before)",
                        distributionNumber, i);
                break;
            }
            executeSubtaskAndSendResult(nodeInfo, subtaskDto);
        }

        log.info("Distribution #{} finished", distributionNumber);

        isStarted.set(false);

        if (startAgain.get()) new Thread(() -> {
            try {
                distribute();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void executeSubtaskAndSendResult(NodeInfo nodeInfo, SubtaskDto subtaskDto) {
        new Thread(() -> {
            try {
                nodeInfo.setStatus(NodeStatus.BUSY);
                List<String> result = nodeService.sendSubtaskToNode(
                        nodeInfo.getUrl() + "/" + executeMethod,
                        subtaskDto.getArgs());

                log.info("Subtask execution complete. The result is ready to be sent (result: {}, subtask: {})",
                        result, subtaskDto);

                nodeInfo.setStatus(NodeStatus.FREE);
                startDistribution();
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    restTemplate.put(builderUrl + sendResultMethod, new HttpEntity<>(result, headers));
                } catch (Exception ignored) {
                }
            } catch (ResourceAccessException e) {
                log.error("Subtask execution failed ({}: {})", e.getClass().getSimpleName(), e.getMessage());
                subtaskService.addSubtask(subtaskDto);
                nodeInfo.setStatus(NodeStatus.UNAVAILABLE);
            } catch (RestClientException | IOException e) {
                log.error("Subtask execution failed ({}: {})", e.getClass().getSimpleName(), e.getMessage());
                subtaskService.addSubtask(subtaskDto);
                nodeInfo.setStatus(NodeStatus.FREE);
            }
        }).start();
    }
}
