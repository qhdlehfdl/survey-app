package com.example.demo.survey.entity;


import com.example.demo.auth.entity.User;
import com.example.demo.survey.dto.request.SurveyRegisterRequestDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "survey")
@Getter @Setter
@NoArgsConstructor
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "title")
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) // 필수 작성자이면 false
    @JoinColumn(name = "writer_id", foreignKey = @ForeignKey(name = "fk_survey_user"))
    private User writer;

    @Column(name = "participants_num")
    private Integer participantsNum;

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("questionOrder ASC")
    private List<Question> questions = new ArrayList<>();


    public Survey(SurveyRegisterRequestDto dto, User user){
        this.title = dto.getTitle();
        this.writer = user;
        this.participantsNum = 0;
    }

    public void addQuestion(Question q){
//        q.setSurvey(this);
        this.questions.add(q);
    }

    public void removeQuestion(Question q) {
//        q.setSurvey(null);
        this.questions.remove(q);
    }
}
