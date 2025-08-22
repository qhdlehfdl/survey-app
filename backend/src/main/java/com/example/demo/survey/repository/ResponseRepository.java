package com.example.demo.survey.repository;

import com.example.demo.survey.entity.Response;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResponseRepository extends JpaRepository<Response, Integer> {

    boolean existsBySurveyIdAndUserId(Integer surveyId, Integer userId);

    List<Response> findAllBySurveyId(Integer surveyId);
}
