package com.example.demo.survey.repository;

import com.example.demo.survey.entity.Question;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {

    @Query("select q from Question q " +
            "left join fetch q.options o " +
            "where q.survey.id = :surveyId " +
            "order by q.questionOrder, o.optionOrder")
    List<Question> findBySurveyIdWithOptions(@Param("surveyId") Integer surveyId);
}
