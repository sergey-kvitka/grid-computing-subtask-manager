package com.kvitka.subtaskmanager.service;

import com.kvitka.subtaskmanager.dto.SubtaskDto;
import org.springframework.stereotype.Service;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class SubtaskService {

    private final Deque<SubtaskDto> subtaskStorage = new ConcurrentLinkedDeque<>();

    public void addSubtasks(List<SubtaskDto> newSubtasks) {
        subtaskStorage.addAll(newSubtasks);
    }

    public SubtaskDto getFirstSubtask() {
        return subtaskStorage.pollFirst();
    }

    public void addSubtask(SubtaskDto subtask) {
        subtaskStorage.offerLast(subtask);
    }

    public void info() {
        System.out.println(subtaskStorage);
    }
}
