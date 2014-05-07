/**
 *  Copyright (c) 2012-2014 http://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.web;

import com.eryansky.common.model.Result;
import com.eryansky.common.orm.hibernate.EntityManager;
import com.eryansky.common.web.springmvc.BaseController;
import com.eryansky.modules.sys.service.CommonManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 提供公共方法的Action.
 *
 * @author 尔演&Eryan eryanwcp@gmail.com
 * @date 2013-2-25 下午1:59:38
 */
@Controller
@RequestMapping("/common")
public class CommonController extends BaseController {

    @Autowired
    private CommonManager commonManager;

    /**
     * 字段校验
     *
     * @param entityName 实体类名称 例如: "Resource"
     * @param fieldName  属性名称
     * @param fieldValue 属性值
     * @param rowId      主键ID
     * @return
     */
    @RequestMapping("fieldCheck")
    @ResponseBody
    public Result fieldCheck(String entityName, String fieldName, String fieldValue, Long rowId) {
        Long entityId = commonManager.getIdByProperty(entityName, fieldName,
                fieldValue);
        boolean isCheck = true;// 是否通过检查
        if (entityId != null) {
            if (rowId != null) {
                if (!rowId.equals(entityId)) {
                    isCheck = false;
                }
            } else {
                isCheck = false;
            }

        }
        return new Result(Result.SUCCESS, null, isCheck);
    }

    @Override
    public EntityManager getEntityManager() {
        return commonManager;
    }
}
