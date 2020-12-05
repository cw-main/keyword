package com.cw.main.keyword.web.req;

import lombok.Data;

@Data
public class Result<T> {

    private String code;

    private String message;

    private T data;


    public static <T> Result<T> success(T t) {

        Result objectResult = new Result<>();
        objectResult.setCode("0");
        objectResult.setMessage("成功!");
        if (t != null) {
            objectResult.setData(t);
        }
        return objectResult;
    }

    public static Result error(String msg) {

        Result objectResult = new Result<>();
        objectResult.setCode("0");
        objectResult.setMessage(msg);
        return objectResult;
    }

}
