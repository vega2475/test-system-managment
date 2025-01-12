package com.testmanagementsystem.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "answer")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "answer_text", nullable = false)
    private String answerText;

    @Column(name = "correct")
    private Boolean correct;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    @Override
    public String toString() {
        return "Answer{id=" + id + ", text='" + answerText + "', correct=" + correct + "}";
    }
}
