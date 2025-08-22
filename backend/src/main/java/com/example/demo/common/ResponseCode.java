package com.example.demo.common;

public interface ResponseCode {
    //200
    String SUCCESS = "SU";

    //400
    String VALIDATION_FAILED = "VF";
    String DUPLICATE_ID = "DI";
    String DUPLICATE_EMAIL = "DE";
    String DUPLICATE_NICKNAME = "DN";
    String DUPLICATE_STUDENTID = "DSID";
    String NOT_EXISTED_USER = "NU";
    String ALREADY_ANSWER = "AA";

    //401
    String SIGN_IN_FAIL = "SF";
    String MISMATCH_FAIL = "MF";
    String AUTHORIZATION_FAIL = "AF";
    String INVALID_REFRESH_TOKEN = "IRT";
    String INVALID_ANSWER = "IA";
    String INVALID_QUESTION_OPTION = "IQO";
    String EXPIRED_REFRESH_TOKEN = "ERT";

    //403
    String NO_PERMISSION = "NP";

    //404
    String NOT_EXISTED_SURVEY = "NS";
    String EMPTY_ANSWER = "EA";
    //500
    String DATABASE_ERROR = "DBE";
}
