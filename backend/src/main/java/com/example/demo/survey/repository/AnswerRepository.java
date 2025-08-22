package com.example.demo.survey.repository;

import com.example.demo.survey.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Integer> {

    boolean existsByQuestionId(Integer questionId);

    @Query("select a.question.id as qid, count(a) as cnt " +
            "from Answer a " +
            "where a.question.survey.id = :surveyId " +
            "group by a.question.id")
    List<Object[]> countAnswersWithQuestionIdBySurveyId(@Param("surveyId") Integer surveyId);
}
