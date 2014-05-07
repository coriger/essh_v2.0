package com.eryansky.core;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import com.eryansky.common.exception.ServiceException;
import com.eryansky.common.exception.SystemException;
import com.eryansky.common.model.Result;
import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.Exceptions;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.SysConstants;
import com.eryansky.common.utils.SysUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.security.SecurityConstants;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.modules.sys.service.ResourceManager;
import com.google.common.collect.Lists;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.StaleObjectStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * 权限拦截器
 */
public class AuthorityInterceptor implements HandlerInterceptor {


    protected Logger logger = LoggerFactory.getLogger(getClass());

    private List<String> excludeUrls;// 不需要拦截的资源

    public List<String> getExcludeUrls() {
        return excludeUrls;
    }

    public void setExcludeUrls(List<String> excludeUrls) {
        this.excludeUrls = excludeUrls;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse httpServletResponse, Object o) throws Exception {
        ResourceManager resourceManager = SpringContextHolder.getBean(ResourceManager.class);
        //登录用户
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo(request);
        String requestUrl = request.getRequestURI();

        //支持上下文
        List<String> newExcludeUrls = Lists.newArrayList();
        String ctx = request.getContextPath();
        if (Collections3.isNotEmpty(excludeUrls)) {
            for (String excludeUrl : excludeUrls) {
                if (excludeUrl.startsWith(ctx)) {
                    newExcludeUrls.add(excludeUrl);
                } else {
                    newExcludeUrls.add(ctx + excludeUrl);
                }
            }
        }

        //不拦截的URL
        if(newExcludeUrls.contains(requestUrl)){
            return true;
        }


        if(sessionInfo != null){
            //清空session中清空未被授权的访问地址
            Object unAuthorityUrl = request.getSession().getAttribute(SecurityConstants.SESSION_UNAUTHORITY_URL);
            if(unAuthorityUrl != null){
                request.getSession().setAttribute(SecurityConstants.SESSION_UNAUTHORITY_URL,null);
            }

            String url = StringUtils.replaceOnce(requestUrl, request.getContextPath(), "");
            //检查用户是否授权该URL
            boolean isAuthority = resourceManager.isAuthority(url,sessionInfo.getUserId());
            if(!isAuthority){
                logger.warn("用户{}未被授权URL:{}！", sessionInfo.getLoginName(), requestUrl);
                request.getRequestDispatcher(SecurityConstants.SESSION_UNAUTHORITY_PAGE).forward(request, httpServletResponse);
                return false; //返回到登录页面
            }

            return true;
        }else{
            System.out.println(requestUrl);
            request.getRequestDispatcher(SecurityConstants.SESSION_UNAUTHORITY_LOGIN_PAGE).forward(request, httpServletResponse);
            return false; //返回到登录页面
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        if(e != null){

        }
    }
}
