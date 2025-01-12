package com.testmanagementsystem.entity;

import lombok.Data;
import javax.persistence.*;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column
    private boolean enabled;

    public User() {
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }
}