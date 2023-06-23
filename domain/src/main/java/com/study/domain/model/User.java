package com.study.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
@Table(name = "USERS")
@EntityListeners(value = {AuditingEntityListener.class})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name")
    private String userName;
    @Column(name = "user_email")
    private String userEmail;
    @Column(name = "phone")
    private String phone;
    @Column(name = "password")
    private String password;

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<JoinGroup> groups = new ArrayList<>();

    @Column(name = "is_active")
    private Boolean isActive;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    public void add(JoinGroup joinGroup) {
        this.groups.add(joinGroup);
        joinGroup.setUser(this);
    }

}
