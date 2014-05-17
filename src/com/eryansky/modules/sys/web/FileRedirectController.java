/**
 *  Copyright (c) 2012-2014 http://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.web;

import com.eryansky.common.orm.hibernate.EntityManager;
import com.eryansky.common.web.springmvc.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * jsp页面重定向.
 *
 * @author 尔演&Eryan eryanwcp@gmail.com
 * @date 2014-5-2 下午10:47:59
 */
@Controller
@RequestMapping(value = "/fileRedirect")
public class FileRedirectController extends BaseController {
    /**
     * jsp文件夹
     */
    private String prefix = "/WEB-INF/views/";

    @RequestMapping(value = {"redirect", ""})
    public void redirect(String prefix, String toPage) throws Exception {
        String page = request.getParameter("toPage");
        if ((page == null) || ("".equals(page))) {
            logger.warn("重定向页面为空!");
            response.sendError(404);
        } else {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("重定向到页面:" + this.prefix + page);
            }
            request.getRequestDispatcher(this.prefix + page).forward(request, response);
        }
    }

    @Override
    public EntityManager getEntityManager() {
        return null;
    }
}