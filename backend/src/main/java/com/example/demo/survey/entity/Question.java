package com.example.demo.survey.entity;

import com.example.demo.survey.dto.request.QuestionRequestDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "question")
@Getter @Setter
@NoArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "question_id")
    private UUID questionId;

    @Column(name = "text")
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private QuestionType type;

    @Column(name = "required")
    private boolean required;

    @Column(name = "question_order")
    private Integer questionOrder; //optional 순서

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("optionOrder ASC")
    private List<QuestionOption> options = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    private Survey survey;
    public enum QuestionType{
        ANSWER, MULTIPLE, CHECKBOX, DROPDOWN;
    }

    public Question(QuestionRequestDto dto, int order, Survey survey){
        this.questionId = dto.getId();
        this.text = dto.getText();
        this.type = QuestionType.valueOf(dto.getType().toUpperCase());
        this.required = dto.isRequired();
        this.questionOrder = order;
        this.survey = survey;
    }

    public void updateQuestion(QuestionRequestDto dto, int order) {
        this.text = dto.getText();
        this.type = QuestionType.valueOf(dto.getType().toUpperCase());
        this.required = dto.isRequired();
        this.questionOrder = order;
    }

    public void addOption(QuestionOption opt){
//        opt.setQuestion(this);
        this.options.add(opt);
    }

    public void removeOption(QuestionOption option) {
//        option.setQuestion(null);
        this.options.remove(option);
    }
}


