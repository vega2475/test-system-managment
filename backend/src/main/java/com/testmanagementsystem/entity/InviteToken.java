package com.testmanagementsystem.entity;


import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
public class InviteToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String token;
    @ManyToOne
    private Test test;
    private LocalDateTime expirationDate = LocalDateTime.now().plusDays(30);
}
