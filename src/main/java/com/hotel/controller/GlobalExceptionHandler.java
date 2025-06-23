package com.hotel.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGlobalException(HttpServletRequest request, Exception ex) {
        logger.error("请求URL: {} 发生异常: {}", request.getRequestURL(), ex.getMessage(), ex);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("exception", ex);
        modelAndView.addObject("url", request.getRequestURL());
        modelAndView.setViewName("error"); // 指向 src/main/resources/templates/error.html
        return modelAndView;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleIllegalArgumentException(HttpServletRequest request, IllegalArgumentException ex) {
        logger.warn("请求URL: {} 参数错误: {}", request.getRequestURL(), ex.getMessage());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorMessage", "请求参数无效: " + ex.getMessage());
        modelAndView.addObject("url", request.getRequestURL());
        modelAndView.setViewName("error");
        return modelAndView;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ModelAndView handleIllegalStateException(HttpServletRequest request, IllegalStateException ex) {
        logger.warn("请求URL: {} 状态错误: {}", request.getRequestURL(), ex.getMessage());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorMessage", "操作状态不正确: " + ex.getMessage());
        modelAndView.addObject("url", request.getRequestURL());
        modelAndView.setViewName("error");
        return modelAndView;
    }
}