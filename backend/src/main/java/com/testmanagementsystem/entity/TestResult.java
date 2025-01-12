package com.testmanagementsystem.entity;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Test test;
    @ManyToOne
    private InviteToken inviteToken;
    private String name;
    private String surname;
    @Email
    private String email;
    private Double testResult;
    private Integer age;
    @OneToMany(mappedBy = "testResultId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartialTestResult> partialTestResults = new ArrayList<>();
}
