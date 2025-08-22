package com.example.demo.survey.entity;

import com.example.demo.survey.dto.request.AnswerRequestDto;
import com.example.demo.survey.dto.request.AnswerSubmitRequestDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "answer")
@Getter @Setter
@NoArgsConstructor
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "response_id", foreignKey = @ForeignKey(name = "fk_answer_response"))
    private Response response;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", foreignKey = @ForeignKey(name = "fk_answer_question"))
    private Question question;

    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id", foreignKey = @ForeignKey(name = "fk_answer_selected_option"))
    private QuestionOption selectedOption;

    @ManyToMany
    @JoinTable(
            name = "answer_option",
            joinColumns = @JoinColumn(name = "answer_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "option_id", referencedColumnName = "id")
    )
    private Set<QuestionOption> selectedOptions = new HashSet<>(); //체크박스일때 selectedOptions에 저장

    public Answer(Response response, Question question){
        this.response = response;
        this.question = question;
    }

    public void addSelectedOption(QuestionOption opt) {
        this.selectedOptions.add(opt);
    }

    public void removeSelectedOption(QuestionOption opt) {
        this.selectedOptions.remove(opt);
    }
}
