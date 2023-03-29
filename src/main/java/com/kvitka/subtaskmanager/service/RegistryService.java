package com.kvitka.subtaskmanager.service;

import com.kvitka.subtaskmanager.dto.NodeInfo;
import com.kvitka.subtaskmanager.dto.NodeStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RegistryService {

    private final Map<String, NodeInfo> registry = new ConcurrentHashMap<>();

    public void setInfo(NodeInfo info) {
        registry.put(info.getUrl(), info);
    }

    public synchronized NodeInfo getFirstFree() {
        NodeInfo nodeInfo;
        for (String url: registry.keySet()) {
            nodeInfo = registry.get(url);
            if (nodeInfo.getStatus() == NodeStatus.FREE) {
                nodeInfo.setStatus(NodeStatus.CONSIDERATION);
                return nodeInfo;
            }
        }
        return null;
    }

    public void info() {
        System.out.println(registry);
    }
}
