package com.example.demo.survey.service;

import com.example.demo.auth.entity.User;
import com.example.demo.auth.repository.UserRepository;
import com.example.demo.survey.dto.request.AnswerRequestDto;
import com.example.demo.survey.dto.request.AnswerSubmitRequestDto;
import com.example.demo.survey.dto.request.QuestionRequestDto;
import com.example.demo.survey.dto.request.SurveyRegisterRequestDto;
import com.example.demo.survey.dto.response.*;
import com.example.demo.survey.entity.*;
import com.example.demo.survey.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SurveyServiceImpl implements SurveyService {

    private final UserRepository userRepository;
    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final ResponseRepository responseRepository;
    private final AnswerRepository answerRepository;

    @Override
    public ResponseEntity<? super SurveyRegisterResponseDto> surveyRegister(SurveyRegisterRequestDto dto, Integer id) {

        try{
            Optional<User> userOpt = userRepository.findById(id);
            if(userOpt.isEmpty()) return SurveyRegisterResponseDto.notExistedUser();

            User user = userOpt.get();

            Survey survey = new Survey(dto, user);
            List<QuestionRequestDto> qDtos = dto.getQuestions();

            if(!qDtos.isEmpty()){
                int questionOrder = 1;
                for(QuestionRequestDto qDto: qDtos){
                    Question q = new Question(qDto, questionOrder++, survey);
                    List<String> qOptions = qDto.getOptions();

                    if(!qOptions.isEmpty()){
                        int optOrder = 1;
                        for(String optTxt: qOptions){
                            QuestionOption qOption = new QuestionOption(optTxt, optOrder++, q);
                            q.addOption(qOption);
                        }
                    }
                    survey.addQuestion(q);
                }
            }

            surveyRepository.save(survey);
        }catch(Exception e){
            e.printStackTrace();;
            return SurveyRegisterResponseDto.databaseError();
        }

        return SurveyRegisterResponseDto.success();
    }

    @Override
    public ResponseEntity<? super SurveyDetailResponseDto> getSurveyDetail(Integer userId, Integer surveyId) {

        SurveyResponseDto dto = null;
        try{
            Optional<Survey> surveyOpt = surveyRepository.findById(surveyId);
            if(surveyOpt.isEmpty()) return SurveyDetailResponseDto.notExistedSurvey();
            Survey survey = surveyOpt.get();

            List<Question> questions = questionRepository.findBySurveyIdWithOptions(surveyId);

            List<Object[]> counts = answerRepository.countAnswersWithQuestionIdBySurveyId(surveyId);

            Map<Integer, Integer> answersCnt = counts.stream().collect(Collectors.toMap(arr -> ((Number) arr[0]).intValue(), arr-> ((Number)arr[1]).intValue()));

            List<QuestionResponseDto> questionDtos = questions.stream()
                    .map(q->{
                        List<QuestionOptionResponseDto> optDtos = q.getOptions().stream()
                                .map(o -> new QuestionOptionResponseDto(o))
                                .collect(Collectors.toList());
                        Integer participantsNum = answersCnt.getOrDefault(q.getId(),0);
                        return new QuestionResponseDto(q, optDtos, participantsNum);
                    }).collect(Collectors.toList());

            dto = new SurveyResponseDto(survey, questionDtos);

        }catch(Exception e){
            e.printStackTrace();
            return SurveyDetailResponseDto.databaseError();
        }

        return SurveyDetailResponseDto.success(dto);
    }

    @Override
    public ResponseEntity<? super SurveyListResponseDto> getSurveyList(Integer page, Integer size) {

        List<SurveySimpleResponseDto> surveys = null;
        int totalPages, totalElements;
        try{
            int safePage = (page != null && page > 0) ? page - 1 : 0;

            Pageable pageable = PageRequest.of(safePage, size, Sort.by(Sort.Direction.DESC, "id"));
            Page<Survey> surveyPage = surveyRepository.findAll(pageable);

            totalElements = (int)surveyPage.getTotalElements();
            totalPages = surveyPage.getTotalPages();

            surveys = surveyPage.stream()
                    .map(survey -> {
                        return new SurveySimpleResponseDto(survey);
                    }).collect(Collectors.toList());

        }catch (Exception e){
            e.printStackTrace();
            return SurveyListResponseDto.databaseError();
        }

        return SurveyListResponseDto.success(surveys, totalPages, totalElements);
    }

    @Override
    public ResponseEntity<? super AnswerSubmitResponseDto> submitAnswer(Integer userId, Integer surveyId, AnswerSubmitRequestDto dto) {
        try{
            Optional<Survey> surveyOpt = surveyRepository.findById(surveyId);
            if(surveyOpt.isEmpty()) return AnswerSubmitResponseDto.notExistedSurvey();
            Survey survey = surveyOpt.get();

            Optional<User> userOpt = userRepository.findById(userId);
            if(userOpt.isEmpty()) return AnswerSubmitResponseDto.notExistedUser();
            User user = userOpt.get();

            boolean alreadyAnswered = responseRepository.existsBySurveyIdAndUserId(surveyId,userId);
            if(alreadyAnswered) return AnswerSubmitResponseDto.alreadyAnswered();

            List<Question> questions = questionRepository.findBySurveyIdWithOptions(surveyId);

            Response response = new Response(survey, user);

            Map<UUID, Question> questionByUuid = questions.stream()
                    .collect(Collectors.toMap(q->q.getQuestionId(),q->q));

            Map<Integer, QuestionOption> optionById = questions.stream()
                    .flatMap(q->q.getOptions().stream())
                    .collect(Collectors.toMap(qo->qo.getId(),qo->qo));

            for (AnswerRequestDto answerDto : dto.getAnswers()) {
                UUID uuid = UUID.fromString(answerDto.getQuestionId());
                Question q = questionByUuid.get(uuid);
                JsonNode jsonAnswer = answerDto.getAnswer();

                System.out.println(uuid);
                System.out.println(q.getText());
                System.out.println(jsonAnswer);

                //required에 답했는지 확인
                if(q.isRequired() && jsonAnswer.isNull()) return AnswerSubmitResponseDto.emptyAnswer();

                Answer answer = null;
                boolean hasAnswer = false;
                Question.QuestionType questionType= q.getType();

                switch (questionType) {
                    case ANSWER:
                        if(jsonAnswer == null || jsonAnswer.isNull()) {
                            hasAnswer = false;
                            break;
                        }

                        System.out.println("qwerzxcv");
                        if (!jsonAnswer.isTextual()) return AnswerSubmitResponseDto.invalidAnswer();

                        answer = new Answer(response, q);
                        answer.setAnswerText(jsonAnswer.asText());
                        hasAnswer = true;
                        break;

                    case MULTIPLE:
                    case DROPDOWN:
                        if(jsonAnswer.isTextual()) return AnswerSubmitResponseDto.invalidAnswer();

                        if(jsonAnswer == null || jsonAnswer.isNull()) {
                            hasAnswer = false;
                            break;
                        }

                        Integer selectedOptId = jsonAnswer.isNumber() ? jsonAnswer.asInt() : null;

                        QuestionOption questionOption = optionById.get(selectedOptId);
                        if(!questionOption.getQuestion().getId().equals(q.getId())) return AnswerSubmitResponseDto.invalidAnswer();

                        answer = new Answer(response, q);
                        answer.setSelectedOption(questionOption);
                        hasAnswer = true;
                        break;

                    case CHECKBOX:
                        if(jsonAnswer == null || jsonAnswer.isNull()) {
                            hasAnswer = false;
                            break;
                        }

                        //체크박스인 경우 프론트에서 무조건 배열로 보냄
                        if (!jsonAnswer.isNull() && !jsonAnswer.isArray()) return AnswerSubmitResponseDto.invalidAnswer();
                        System.out.println("zcxv");

                        answer = new Answer(response, q);

                        for (JsonNode checkboxAnswer : jsonAnswer) {
                            Integer checkboxOptId = checkboxAnswer.isNumber() ? checkboxAnswer.asInt() : null;

                            QuestionOption chosenOption = optionById.get(checkboxOptId);
                            if(chosenOption == null) return AnswerSubmitResponseDto.invalidQuestionOption();

                            if(!chosenOption.getQuestion().getId().equals(q.getId())) return AnswerSubmitResponseDto.invalidAnswer();

                            answer.getSelectedOptions().add(chosenOption);
                        }

                        hasAnswer = true;
                        break;
                    default:
                        return AnswerSubmitResponseDto.invalidAnswer();
                }
                if(hasAnswer && answer != null)
                    response.addAnswer(answer);
            }

            responseRepository.save(response);

            survey.setParticipantsNum(survey.getParticipantsNum() + 1);
            surveyRepository.save(survey);

        }catch (Exception e){
            e.printStackTrace();
            return AnswerSubmitResponseDto.databaseError();
        }

        return AnswerSubmitResponseDto.success();
    }

    @Override
    public List<SurveySimpleResponseDto> getSurveyListByUser(Integer userId, Integer lastId, Integer size) {

        List<SurveySimpleResponseDto> surveys = null;

        try {
            Pageable pageable = PageRequest.of(0,size);
            surveys = surveyRepository.findSimpleDtoByWriterIdWithCursor(userId, lastId, pageable);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return surveys;
    }

    @Override
    public ResponseEntity<? super GetSurveyResultResponseDto> getSurveyResult(Integer userId, Integer surveyId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if(userOpt.isEmpty()) return GetSurveyResultResponseDto.notExistedUser();
            User user = userOpt.get();

            Optional<Survey> surveyOpt = surveyRepository.findById(surveyId);
            if(surveyOpt.isEmpty()) return GetSurveyResultResponseDto.notExistedSurvey();
            Survey survey = surveyOpt.get();

            List<Response> responses = responseRepository.findAllBySurveyId(surveyId);
            List<QuestionResultResponseDto> questionResults = new ArrayList<>();

            for (Question q : survey.getQuestions()) {
                if (q.getType() == Question.QuestionType.ANSWER) {
                    QuestionResultResponseDto qRRDto = new QuestionResultResponseDto(q, Collections.emptyList());

                    if(qRRDto.getAnswers() == null) qRRDto.setAnswers(new ArrayList<>());

                    for (Response response : responses) {
                        if(response.getAnswers() == null) continue;

                        for (Answer answer : response.getAnswers()) {
                            if (answer.getQuestion().getId() == q.getId()) {
                                String text =null;
                                text = answer.getAnswerText();
                                if(text != null) qRRDto.getAnswers().add(text);
                            }
                        }
                    }
                    questionResults.add(qRRDto);
                } else{
                    //각 질문 옵션 답변 수 카운팅
                    Map<Integer, Integer> counts = new HashMap<>();
                    List<QuestionOption> optionList = q.getOptions() == null ? Collections.emptyList() : q.getOptions();

                    for (QuestionOption option : optionList)
                        counts.put(option.getId(), 0);

                    for (Response response : responses) {
                        if (response.getAnswers() == null) continue;

                        for (Answer answer : response.getAnswers()) {
                            if (answer.getQuestion() == null) continue;

                            if (answer.getQuestion().getId() == q.getId()) {
                                if (q.getType() == Question.QuestionType.CHECKBOX) {
                                    if (answer.getSelectedOptions() == null) continue;
                                    answer.getSelectedOptions().stream().forEach(questionOption -> {
                                        if (questionOption == null || questionOption.getId() == null) return;
                                        counts.put(questionOption.getId(), counts.getOrDefault(questionOption.getId(), 0) + 1);
                                    });
                                } else {
                                    if (answer.getSelectedOption() != null && answer.getSelectedOption().getId() != null) {
                                        Integer selectedId = answer.getSelectedOption().getId();
                                        counts.put(selectedId, counts.getOrDefault(selectedId, 0) + 1);
                                    }
                                }
                            }
                        }
                    }

                    List<QuestionOptionResultResponseDto> optionDtos = new ArrayList<>();
                    optionList.stream().forEach(option -> {
                        QuestionOptionResultResponseDto optionDto = new QuestionOptionResultResponseDto(option);
                        optionDto.setAnswersCount(counts.getOrDefault(option.getId(), 0));
                        optionDtos.add(optionDto);
                    });
                    QuestionResultResponseDto questionDto = new QuestionResultResponseDto(q,optionDtos);
                    if(questionDto.getAnswers() == null)
                        questionDto.setAnswers(new ArrayList<>());
                    questionResults.add(questionDto);
                }
            }

            SurveyResultResponseDto surveyDto = new SurveyResultResponseDto(survey, questionResults);
            return GetSurveyResultResponseDto.success(surveyDto);
        } catch (Exception e) {
            e.printStackTrace();
            return GetSurveyResultResponseDto.databaseError();
        }
    }

    @Override
    public ResponseEntity<? super SurveyUpdateResponseDto> updateSurvey(Integer userId, Integer surveyId, SurveyRegisterRequestDto dto) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if(userOpt.isEmpty()) return SurveyUpdateResponseDto.authorizationFail();
            User user = userOpt.get();

            Optional<Survey> surveyOpt = surveyRepository.findById(surveyId);
            if(surveyOpt.isEmpty()) return SurveyUpdateResponseDto.notExistedSurvey();
            Survey survey = surveyOpt.get();

            if(!survey.getWriter().getId().equals(user.getId())) return SurveyUpdateResponseDto.authorizationFail();

            List<Question> questionList = questionRepository.findBySurveyIdWithOptions(surveyId);

            Map<UUID, Question> existingQuestions = questionList.stream()
                    .filter(question -> question.getQuestionId() != null)
                    .collect(Collectors.toMap(question -> question.getQuestionId(), question->question));

            //업뎃한 질문 리스트
            Set<Integer> touchedQuestion = new HashSet<>();

            List<QuestionRequestDto> questionRDtos = dto.getQuestions();

            int questionOrder = 1;
            for (QuestionRequestDto questionRDto : questionRDtos) {
                UUID questionUuid = questionRDto.getId();
                Question updatedQuestion = null;

                //기존 질문 업데이트 -> 변수들 새로 업뎃
                if (existingQuestions.containsKey(questionUuid)) {
                    updatedQuestion = existingQuestions.get(questionUuid);
                    updatedQuestion.updateQuestion(questionRDto, questionOrder++);

                    //질문 업뎃한거 리스트에 추가
                    touchedQuestion.add(updatedQuestion.getId());

                    List<String> optionTexts = questionRDto.getOptions() == null ? Collections.emptyList() : questionRDto.getOptions();

                    //질문에 응답 있는지 확인
                    boolean questionHasAnswers = answerRepository.existsByQuestionId(updatedQuestion.getId());

                    //응답 있다면 질문 추가만 가능
                    if (questionHasAnswers) {
                        Set<String> existingOptions = updatedQuestion.getOptions().stream()
                                .map(questionOption -> questionOption.getText())
                                .collect(Collectors.toSet());

                        int optionOrder = updatedQuestion.getOptions().size()+1;
                        for (String questionOptionText : optionTexts) {
                            //새 질문이라면 추가
                            if(!existingOptions.contains(questionOptionText))
                                updatedQuestion.addOption(new QuestionOption(questionOptionText, optionOrder++, updatedQuestion));
                        }
                    }else{
                        //응답 없으면 새로 생성
                        updatedQuestion.getOptions().clear();

                        int optionOrder = 1;
                        for (String questionOptionText : optionTexts)
                            updatedQuestion.addOption(new QuestionOption(questionOptionText, optionOrder++, updatedQuestion));
                    }
                } else{
                    // 새로운 질문
                    Question newQuestion = new Question(questionRDto, questionOrder++, survey);
                    int optionOrder = 1;

                    List<String> optionsTexts = questionRDto.getOptions() == null ? Collections.emptyList(): questionRDto.getOptions();

                    for (String questionOptionText : optionsTexts)
                        newQuestion.addOption(new QuestionOption(questionOptionText, optionOrder++, newQuestion));

                    survey.addQuestion(newQuestion);
                }
            }

            //원래 있었지만 삭제할 질문
            for (Question existingQuestion : existingQuestions.values()) {
                if (!touchedQuestion.contains(existingQuestion.getId())) {
                    boolean hasAnswers = answerRepository.existsByQuestionId(existingQuestion.getId());

                    //답변 없을때만 삭제
                    if(!hasAnswers)
                        survey.getQuestions().remove(existingQuestion);
                }
            }

            survey.setTitle(dto.getTitle());
            surveyRepository.save(survey);
            return SurveyUpdateResponseDto.success();
        } catch (Exception e) {
            e.printStackTrace();
            return SurveyUpdateResponseDto.databaseError();
        }
    }
}
