package com.cw.main.keyword.web.interceptor;

import com.cw.main.keyword.web.req.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestControllerAdvice
@Slf4j
public class WebExceptionHandler {

    @ExceptionHandler
    public Result noHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.error("接口 [" + request.getRequestURI() + "] 不存在 {}", e);
        return Result.error("接口 [" + request.getRequestURI() + "] 不存在");
    }

//   自定义异常
//    @ExceptionHandler(RpcException.class)
//    public Result<Object> resolveException(HttpServletRequest request, RpcException ex) {
//        log.error("Rpc调用失败！"+ex.getMessage(), ex);
//        Result<Object> Result = new Result<>();
//        Result.setCode(ex.getCode());
//        Result.setMessage(ex.getMessage());
//        return Result;
//    }


    @ExceptionHandler
    public Result constraintViolationException(ConstraintViolationException e) {
        // 方法参数级别校验
        // 类上加 @Validated 即可
        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        List<String> list = new ArrayList<>();
        if (constraintViolations != null && constraintViolations.size() > 0) {
            constraintViolations.forEach(constraintViolation -> {
                list.add(constraintViolation.getMessage());
            });
            log.error("字段验证不通过: " + list);
        }
        return Result.error(list.toString());
    }

    @ExceptionHandler
    public Result methodArgumentNotValidException(MethodArgumentNotValidException m) {
        // @RequestBody Bean级别参数校验
        // 目前是全部校验模式(会校验完所有的属性)
        // @RequestBody @Validated User user
        BindingResult bindingResult = m.getBindingResult();
        List<String> list = new ArrayList<>();
        if (bindingResult.hasErrors()) {
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            fieldErrors.forEach(fieldError -> {
                list.add(fieldError.getDefaultMessage());
                log.error("错误字段: {} , 错误信息: {}", fieldError.getField(), fieldError.getDefaultMessage());
            });
        }
        return Result.error(list.toString());
    }

    @ExceptionHandler
    public Result methodArgumentNotValidException(BindException bindException) {
        // 普通 Bean级别参数校验
        // 目前是全部校验模式(会校验完所有的属性)
        // @Validated User user
        BindingResult bindingResult = bindException.getBindingResult();
        List<String> list = new ArrayList<>();
        if (bindingResult.hasErrors()) {
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            fieldErrors.forEach(fieldError -> {
                boolean contains = fieldError.contains(TypeMismatchException.class); // 类型转化错误
                if (contains) {
                    list.add(fieldError.getRejectedValue() + "不是合法值");
                } else {
                    list.add(fieldError.getDefaultMessage());
                }
                log.info("错误字段: {} , 错误信息: {}", fieldError.getField(), fieldError.getDefaultMessage());
            });
        }
        return Result.error(list.toString());
    }

    @ExceptionHandler
    public Result methodArgumentNotValidException(HttpMessageConversionException exception) {
        // json转reponseBody异常
        // TODO 想打印出具体信息 ...
        log.info(exception.getMessage(), exception);
        return Result.error("参数不合法,操作失败");
    }

    @ExceptionHandler(Throwable.class)
    public Result<Object> resolveException(HttpServletRequest request, Exception ex) {
        HttpSession session = request.getSession(false);
        log.info("出错了 error:", ex);
        Result error = new Result();
        error.setCode("01");
        error.setMessage(ex.getMessage());
        return error;
    }


}
