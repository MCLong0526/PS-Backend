package com.points.PS_Backend.dto;

import lombok.Data;

@Data
public class ApiResponse {

    private int code;
    private String msg;
    private Object data;
    private String token;

    public ApiResponse(int code, String msg, Object data, String token) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.token = token;
    }
}