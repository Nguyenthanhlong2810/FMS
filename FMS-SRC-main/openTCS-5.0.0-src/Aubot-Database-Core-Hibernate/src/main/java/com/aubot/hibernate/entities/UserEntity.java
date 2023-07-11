package com.aubot.hibernate.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "user", schema = "public")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "username")
    private String username;

    @Column(name = "password_hash")
    private String password;

    @Column(name = "create_at")
    private Timestamp createAt;

    @Column(name = "update_at")
    private Timestamp updateAt;

    @Column(name = "delete_at")
    private Timestamp deleteAt;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "last_login")
    private Timestamp lastLogin;

    @Column(name = "is_admin")
    private boolean isAdmin;

    @OneToMany(mappedBy="user", fetch = FetchType.EAGER)
    private Set<UserPermissionEntity> permissions;

}
