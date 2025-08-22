package com.example.demo.survey.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "question_option")
@Getter @Setter
@NoArgsConstructor
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "text")
    private String text;

    @Column(name = "option_order")
    private Integer optionOrder;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    public QuestionOption(String optTxt, int optOrder, Question q){
        this.text = optTxt;
        this.optionOrder = optOrder;
        this.question = q;
    }

}
