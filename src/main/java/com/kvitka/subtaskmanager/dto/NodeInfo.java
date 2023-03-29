package com.kvitka.subtaskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeInfo {
    private String url;
    private NodeStatus status;
}
