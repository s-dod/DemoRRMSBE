package com.example.rrms.authsecurity.service;

import com.example.rrms.authsecurity.domain.enums.Role;
import com.example.rrms.authsecurity.domain.modal.Task;
import com.example.rrms.authsecurity.domain.enums.TaskStatus;
import com.example.rrms.authsecurity.domain.modal.User;
import com.example.rrms.authsecurity.domain.enums.UserStatus;
import com.example.rrms.authsecurity.repository.TaskRepository;
import com.example.rrms.authsecurity.repository.UserRepository;
import com.example.rrms.authsecurity.security.user.CurrentUser;
import com.example.rrms.authsecurity.security.user.UserPrincipal;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import org.modelmapper.ModelMapper;
import java.util.UUID;

import com.example.rrms.authsecurity.dto.request.CreateTaskRequest;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final ModelMapper modelMapper;

    @Transactional
    public Task createTask(CreateTaskRequest createTaskRequest) {
        UserPrincipal userPrincipal = CurrentUser.get();
        UUID tenantId = userPrincipal.getTenantId();

        Task task = new Task();
        modelMapper.map(createTaskRequest, task);
        task.setTenantId(tenantId);
        task.setCreatedBy(userPrincipal.getId());

        if (createTaskRequest.assignedStaffId() != null) {
            User staff = userRepository.findByIdAndTenantId(createTaskRequest.assignedStaffId(), tenantId)
                    .orElseThrow(() -> new EntityNotFoundException("Staff not found"));

            if (staff.getRole() != Role.STAFF || staff.getStatus() != UserStatus.ACTIVE) {
                throw new IllegalStateException("Staff is not active");
            }
            if (staff.getStaffType() != createTaskRequest.department()) {
                throw new IllegalStateException("Staff type does not match task department");
            }
            task.setAssignedStaffId(createTaskRequest.assignedStaffId());
            task.setStatus(TaskStatus.ASSIGNED);
        }

        task = taskRepository.save(task);
        auditService.log(userPrincipal, "TASK_CREATE", "TASK", task.getId().toString(), "SUCCESS", null);
        return task;
    }

    @Transactional(readOnly = true)
    public List<Task> getTenantTasks() {
        UserPrincipal userPrincipal = CurrentUser.get();
        return taskRepository.findByTenantId(userPrincipal.getTenantId());
    }

    @Transactional
    public Task assign(UUID taskId, UUID staffId) {
        UserPrincipal userPrincipal = CurrentUser.get();
        Task task = loadTask(taskId, userPrincipal);

        User staff = userRepository.findByIdAndTenantId(staffId, userPrincipal.getTenantId())
                .orElseThrow(() -> new EntityNotFoundException("Staff not found"));

        if (staff.getRole() != Role.STAFF || staff.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("Staff is not active");
        }
        if (staff.getStaffType() != task.getDepartment()) {
            throw new IllegalStateException("Staff type does not match task department");
        }

        task.setAssignedStaffId(staffId);
        task.setStatus(TaskStatus.ASSIGNED);
        auditService.log(userPrincipal, "TASK_ASSIGN", "TASK", taskId.toString(), "SUCCESS", "staff=" + staffId);
        return task;
    }

    @Transactional
    public Task verify(UUID taskId) {
        UserPrincipal userPrincipal = CurrentUser.get();
        Task task = loadTask(taskId, userPrincipal);
        requireStatus(task, TaskStatus.COMPLETED_BY_STAFF, TaskStatus.UNDER_INSPECTION);
        task.setStatus(TaskStatus.CLOSED);
        auditService.log(userPrincipal, "TASK_VERIFY", "TASK", taskId.toString(), "SUCCESS", null);
        return task;
    }

    @Transactional
    public Task reject(UUID taskId, String reason) {
        UserPrincipal userPrincipal = CurrentUser.get();
        Task task = loadTask(taskId, userPrincipal);
        requireStatus(task, TaskStatus.COMPLETED_BY_STAFF, TaskStatus.UNDER_INSPECTION);
        task.setStatus(TaskStatus.REWORK_REQUIRED);
        task.setRejectionReason(reason);
        auditService.log(userPrincipal, "TASK_REJECT", "TASK", taskId.toString(), "SUCCESS", reason);
        return task;
    }

    @Transactional(readOnly = true)
    public List<Task> myTasks() {
        UserPrincipal userPrincipal = CurrentUser.get();
        return taskRepository.findByTenantIdAndAssignedStaffId(userPrincipal.getTenantId(), userPrincipal.getId());
    }

    @Transactional
    public Task start(UUID taskId) {
        UserPrincipal userPrincipal = CurrentUser.get();
        Task task = loadAssignedToMe(taskId, userPrincipal);
        requireStatus(task, TaskStatus.ASSIGNED, TaskStatus.REWORK_REQUIRED);
        task.setStatus(TaskStatus.IN_PROGRESS);
        auditService.log(userPrincipal, "TASK_START", "TASK", taskId.toString(), "SUCCESS", null);
        return task;
    }

    @Transactional
    public Task complete(UUID taskId) {
        UserPrincipal userPrincipal = CurrentUser.get();
        Task task = loadAssignedToMe(taskId, userPrincipal);
        requireStatus(task, TaskStatus.IN_PROGRESS);
        task.setStatus(TaskStatus.COMPLETED_BY_STAFF);
        auditService.log(userPrincipal, "TASK_COMPLETE", "TASK", taskId.toString(), "SUCCESS", null);
        return task;
    }

    private Task loadTask(UUID id, UserPrincipal userPrincipal) {
        return taskRepository.findByIdAndTenantId(id, userPrincipal.getTenantId())
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
    }

    private Task loadAssignedToMe(UUID id, UserPrincipal userPrincipal) {
        Task task = loadTask(id, userPrincipal);
        if (!userPrincipal.getId().equals(task.getAssignedStaffId())) {
            auditService.log(userPrincipal, "TASK_ACCESS", "TASK", id.toString(), "DENIED", "not assigned");
            throw new AccessDeniedException("Task is not assigned to you");
        }
        return task;
    }

    private void requireStatus(Task task, TaskStatus... allowed) {
        if (!Arrays.asList(allowed).contains(task.getStatus())) {
            throw new IllegalStateException("Invalid transition from " + task.getStatus());
        }
    }
}
