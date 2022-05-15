package com.lexschedulerbot.response;

import java.util.Arrays;

public class GetSuggestsResponse extends BaseResponse {
    private String[] suggests;

    public String[] getSuggests() {
        return suggests;
    }

    @Override
    public String toString() {
        return "GetSuggestsResponse{" +
                "suggests=" + Arrays.toString(suggests) +
                ", isOk=" + isOk +
                ", code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
