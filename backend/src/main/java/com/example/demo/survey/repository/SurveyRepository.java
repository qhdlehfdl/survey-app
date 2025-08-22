package com.example.demo.survey.repository;

import com.example.demo.survey.dto.response.SurveySimpleResponseDto;
import com.example.demo.survey.entity.Survey;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Integer> {

    //id, title, participantsNum 뽑아서 SurveySimpleResponseDto 만듬
    @Query("SELECT new com.example.demo.survey.dto.response.SurveySimpleResponseDto(s)" +
            "FROM Survey s WHERE s.writer.id = :writerId " +
            "AND (:lastId IS NULL OR s.id <= :lastId) " +
            "ORDER BY s.id DESC")
    List<SurveySimpleResponseDto> findSimpleDtoByWriterIdWithCursor(@Param("writerId") Integer writerId, @Param("lastId")Integer lastId, Pageable pageable);

}
