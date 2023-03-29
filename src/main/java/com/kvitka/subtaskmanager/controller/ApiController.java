package com.kvitka.subtaskmanager.controller;

import com.kvitka.subtaskmanager.dto.SubtaskDto;
import com.kvitka.subtaskmanager.service.NodeService;
import com.kvitka.subtaskmanager.service.RegistryService;
import com.kvitka.subtaskmanager.service.SubtaskDistributionService;
import com.kvitka.subtaskmanager.service.SubtaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final NodeService nodeService;
    private final SubtaskService subtaskService;
    private final RegistryService registryService;
    private final SubtaskDistributionService subtaskDistributionService;

    @PutMapping("/saveJar")
    public void saveJar(@RequestParam("jar") MultipartFile jarFile) throws IOException {
        nodeService.saveJarFile(jarFile);
    }

    @PutMapping("/sendSubtasks")
    public void receiveSubtasks(@RequestBody List<SubtaskDto> subtasks) {
        subtaskService.addSubtasks(subtasks);
        subtaskDistributionService.startDistribution();
    }

    @PutMapping("/info")
    public void info() {
        registryService.info();
        subtaskService.info();
    }

}
