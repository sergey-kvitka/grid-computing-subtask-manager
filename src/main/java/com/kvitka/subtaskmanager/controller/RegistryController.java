package com.kvitka.subtaskmanager.controller;

import com.kvitka.subtaskmanager.dto.NodeInfo;
import com.kvitka.subtaskmanager.dto.NodeStatus;
import com.kvitka.subtaskmanager.dto.RegistrationDto;
import com.kvitka.subtaskmanager.service.RegistryService;
import com.kvitka.subtaskmanager.service.SubtaskDistributionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RegistryController {

    private final RegistryService registryService;
    private final SubtaskDistributionService subtaskDistributionService;

    @PutMapping("/register")
    public void register(@RequestBody RegistrationDto registrationDto) {
        String url = registrationDto.getUrl();
        registryService.setInfo(new NodeInfo(url, NodeStatus.FREE));
        log.info("New node registered (url={})", url);
        subtaskDistributionService.startDistribution();
    }
}
