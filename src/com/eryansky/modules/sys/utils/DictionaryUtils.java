/**
 *  Copyright (c) 2012-2014 http://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 */
package com.eryansky.modules.sys.utils;

import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.modules.sys.entity.Dictionary;
import com.eryansky.modules.sys.service.DictionaryManager;

/**
 * 数据字典工具类
 * @author : 尔演&Eryan eryanwcp@gmail.com
 * @date : 2014-05-17 21:22
 */
public class DictionaryUtils {

    /**
     * 根据字典逼编码得到字典名称
     * @param dictionaryCode 字典编码
     * @return
     */
    public static String getDictionaryNameByCode(String dictionaryCode){
        DictionaryManager dictionaryManager = SpringContextHolder.getBean(DictionaryManager.class);
        Dictionary dictionary = dictionaryManager.getByCode(dictionaryCode);
        if(dictionary != null){
            return dictionary.getName();
        }
        return null;
    }

}