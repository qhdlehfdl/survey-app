package com.example.demo.common;

public interface ResponseMessage {
    //200
    String SUCCESS = "Success.";

    //400
    String VALIDATION_FAILED = "Validation Failed.";
    String DUPLICATE_ID = "Duplicate ID.";
    String DUPLICATE_EMAIL = "Duplication Email.";
    String DUPLICATE_NICKNAME = "Duplicate nickname.";
    String DUPLICATE_STUDENTID = "Duplicate studentID.";
    String NOT_EXISTED_USER = "This user does not exist";
    String ALREADY_ANSWER = "Already answered.";

    //401
    String SIGN_IN_FAIL = "Login information mismatch";
    String MISMATCH_FAIL = "Information mismatch";
    String AUTHORIZATION_FAIL = "Authorization Failed";
    String INVALID_REFRESH_TOKEN = "Invalid refresh token.";
    String INVALID_ANSWER = "Invalid answer.";
    String INVALID_QUESTION_OPTION = "Invalid question option";
    String EXPIRED_REFRESH_TOKEN = "Expired refresh token.";

    //403
    String NO_PERMISSION = "Do not have permission";

    //404
    String NOT_EXISTED_SURVEY = "This survey does not exist";
    String EMPTY_ANSWER = "Required entry is empty.";

    //500
    String DATABASE_ERROR = "Database error.";
}
