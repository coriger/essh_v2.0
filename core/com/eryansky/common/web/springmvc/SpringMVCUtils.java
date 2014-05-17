/**
 *  Copyright (c) 2012-2014 http://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 */
package com.eryansky.common.web.springmvc;

import com.eryansky.common.utils.IpUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * SpringMVC工具类
 *
 * @author : 尔演&Eryan eryanwcp@gmail.com
 * @date : 2014-05-17 20:39
 */
public class SpringMVCUtils {

    /**
     * 取得HttpServletRequest简化函数.
     *
     * @return
     */
    public static HttpServletRequest getRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return request;

    }

    /**
     * 取得HttpSession的简化函数.
     *
     * @return
     */
    public static HttpSession getSession() {
        return getRequest().getSession();
    }

    /**
     * 取得HttpSession的简化函数.
     *
     * @param create
     * @return
     */
    public static HttpSession getSession(boolean create) {
        return getRequest().getSession(create);
    }


    /**
     * 取得HttpSession中Attribute的简化函数.
     */
    public static Object getSessionAttribute(String name) {
        HttpSession session = getSession(false);
        return (session != null ? session.getAttribute(name) : null);
    }

    /**
     * 取得HttpRequest中Parameter的简化方法.
     */
    public static String getParameter(String name) {
        return getRequest().getParameter(name);
    }

    /**
     * 取得HttpResponse的简化函数.
     */
    public static HttpServletResponse getResponse() {
        return ((ServletWebRequest) RequestContextHolder.getRequestAttributes()).getResponse();
    }


    /**
     * 取得客户端IP.
     */
    public static String getIp() {
        HttpServletRequest request = getRequest();
        return IpUtils.getIpAddr(request);
    }
}
