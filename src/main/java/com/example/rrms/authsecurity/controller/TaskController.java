package com.example.rrms.authsecurity.controller;

import com.example.rrms.authsecurity.domain.modal.Task;
import com.example.rrms.authsecurity.service.TaskService;
import com.example.rrms.authsecurity.dto.request.CreateTaskRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PreAuthorize("hasAuthority('TASK_CREATE')")
    @PostMapping("/api/management/tasks")
    public Task create(@Valid @RequestBody CreateTaskRequest createTaskRequest) {
        return taskService.createTask(createTaskRequest);
    }

    @PreAuthorize("hasAuthority('TASK_VIEW')")
    @GetMapping("/api/management/tasks")
    public List<Task> getTasks() {
        return taskService.getTenantTasks();
    }

    @PreAuthorize("hasAuthority('TASK_ASSIGN')")
    @PutMapping("/api/management/tasks/{id}/assign/{staffId}")
    public Task assign(@PathVariable UUID id, @PathVariable UUID staffId) {
        return taskService.assign(id, staffId);
    }

    @PreAuthorize("hasAuthority('TASK_VERIFY')")
    @PutMapping("/api/management/tasks/{id}/verify")
    public Task verify(@PathVariable UUID id) {
        return taskService.verify(id);
    }

    @PreAuthorize("hasAuthority('TASK_REJECT')")
    @PutMapping("/api/management/tasks/{id}/reject")
    public Task reject(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return taskService.reject(id, body.get("reason"));
    }

    @PreAuthorize("hasAuthority('TASK_VIEW_ASSIGNED')")
    @GetMapping("/api/staff/tasks")
    public List<Task> myTasks() {
        return taskService.myTasks();
    }

    @PreAuthorize("hasAuthority('TASK_START')")
    @PutMapping("/api/staff/tasks/{id}/start")
    public Task start(@PathVariable UUID id) {
        return taskService.start(id);
    }

    @PreAuthorize("hasAuthority('TASK_COMPLETE')")
    @PutMapping("/api/staff/tasks/{id}/complete")
    public Task complete(@PathVariable UUID id) {
        return taskService.complete(id);
    }
}
