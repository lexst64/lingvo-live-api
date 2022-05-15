package com.lexschedulerbot.response;

import com.lexschedulerbot.type.LexemModel;

import java.util.Arrays;

public class GetWordFormsResponse extends BaseResponse {
    private LexemModel[] lexemModels;

    public LexemModel[] getLexemModels() {
        return lexemModels;
    }

    @Override
    public String toString() {
        return "GetWordFormsResponse{" +
                "lexemModels=" + Arrays.toString(lexemModels) +
                ", isOk=" + isOk +
                ", code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
