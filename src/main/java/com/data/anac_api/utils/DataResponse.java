package com.data.anac_api.utils;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class DataResponse<T> {
    private Date timestamp;
    private boolean isError;
    private String message;
    private T data;

    public DataResponse(Date timestamp, boolean isError, String message, T data) {
        this.timestamp = timestamp;
        this.isError = isError;
        this.message = message;
        this.data = data;
    }

}