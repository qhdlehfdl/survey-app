package com.example.demo.survey.entity;

import com.example.demo.auth.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "response")
@Getter @Setter
@NoArgsConstructor
public class Response {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "survey_id", foreignKey = @ForeignKey(name = "fk_response_survey"))
    private Survey survey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_response_user"))
    private User user;

    @OneToMany(mappedBy = "response", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers = new ArrayList<>();

    public Response(Survey survey, User user){
        this.survey = survey;
        this.user = user;
    }


    public void addAnswer(Answer a) {
        //a.setResponse(this);
        this.answers.add(a);
    }

    public void removeAnswer(Answer a) {
        //a.setResponse(null);
        this.answers.remove(a);
    }
}
