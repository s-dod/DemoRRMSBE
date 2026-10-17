package com.example.rrms.authsecurity.domain.modal;

import com.example.rrms.authsecurity.domain.enums.StaffType;
import com.example.rrms.authsecurity.domain.enums.TaskStatus;
import com.example.rrms.authsecurity.security.tenant.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "tasks", indexes = @Index(name = "idx_task_tenant_staff", columnList = "tenant_id,assigned_staff_id"))
@Getter
@Setter
@NoArgsConstructor
public class Task extends BaseTenantEntity {

    @Column(nullable = false) private String title;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private StaffType department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_staff_id", foreignKey = @ForeignKey(name = "fk_task_staff"))
    private User assignedStaff;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private TaskStatus status = TaskStatus.ASSIGNED;

    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", foreignKey = @ForeignKey(name = "fk_task_creator"))
    private User createdBy;

    public UUID getAssignedStaffId() {
        return assignedStaff != null ? assignedStaff.getId() : null;
    }

    public void setAssignedStaffId(UUID staffId) {
        if (staffId == null) {
            this.assignedStaff = null;
        } else {
            User u = new User();
            u.setId(staffId);
            this.assignedStaff = u;
        }
    }

    public UUID getCreatedById() {
        return createdBy != null ? createdBy.getId() : null;
    }

    public void setCreatedBy(UUID userId) {
        if (userId == null) {
            this.createdBy = null;
        } else {
            User u = new User();
            u.setId(userId);
            this.createdBy = u;
        }
    }
}
