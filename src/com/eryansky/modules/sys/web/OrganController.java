/**
 *  Copyright (c) 2012-2014 http://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.web;

import com.eryansky.common.exception.ActionException;
import com.eryansky.common.model.Combobox;
import com.eryansky.common.model.Datagrid;
import com.eryansky.common.model.Result;
import com.eryansky.common.model.TreeNode;
import com.eryansky.common.orm.Page;
import com.eryansky.common.orm.PropertyFilter;
import com.eryansky.common.orm.entity.StatusState;
import com.eryansky.common.orm.hibernate.EntityManager;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.web.springmvc.BaseController;
import com.eryansky.modules.sys._enum.OrganType;
import com.eryansky.modules.sys.entity.Organ;
import com.eryansky.modules.sys.entity.User;
import com.eryansky.modules.sys.service.OrganManager;
import com.eryansky.modules.sys.service.UserManager;
import com.eryansky.utils.SelectType;
import com.google.common.collect.Lists;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 机构Organ管理 Controller层.
 *
 * @author 尔演&Eryan eryanwcp@gmail.com
 * @date 2013-09-09 下午21:36:24
 */
@SuppressWarnings("serial")
@Controller
@RequestMapping(value = "/sys/organ")
public class OrganController extends BaseController<Organ> {

    @Autowired
    private OrganManager organManager;
    @Autowired
    private UserManager userManager;

    @Override
    public EntityManager<Organ, Long> getEntityManager() {
        return organManager;
    }

    @RequestMapping(value = {""})
    public String list() {
        return "modules/sys/organ";
    }

    /**
     * @param organ
     * @param parentOrganType 上级类型
     * @return
     * @throws Exception
     */
    @RequestMapping(value = {"input"})
    public String input(@ModelAttribute("model") Organ organ, Integer parentOrganType, Model model) throws Exception {
        if (parentOrganType == null && organ.getParentOrgan() != null) {
            parentOrganType = organ.getParentOrgan().getType();
        }
        model.addAttribute("parentOrganType", parentOrganType);
        return "modules/sys/organ-input";
    }


    @RequestMapping(value = {"treegrid"})
    @ResponseBody
    public Datagrid<Organ> treegrid(String sort, String order) throws Exception {
        List<PropertyFilter> filters = Lists.newArrayList();
        List<Organ> list = getEntityManager().find(filters, sort, order);
        Datagrid<Organ> dg = new Datagrid<Organ>(list.size(), list);
        return dg;
    }

    /**
     * combogrid
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = {"combogrid"})
    @ResponseBody
    public Datagrid<Organ> combogrid(String nameOrCode, @RequestParam(value = "ids", required = false)List<Long> ids, Integer rows) throws Exception {
        Criterion statusCriterion = Restrictions.eq("status", StatusState.normal.getValue());
        Criterion[] criterions = new Criterion[0];
        criterions = (Criterion[]) ArrayUtils.add(criterions, 0, statusCriterion);
        Criterion criterion = null;
        if (Collections3.isNotEmpty(ids)) {
            //in条件
            Criterion inCriterion = Restrictions.in("id", ids);

            if (StringUtils.isNotBlank(nameOrCode)) {
                Criterion nameCriterion = Restrictions.like("name", nameOrCode, MatchMode.ANYWHERE);
                Criterion codeCriterion = Restrictions.like("code", nameOrCode, MatchMode.ANYWHERE);
                Criterion criterion1 = Restrictions.or(nameCriterion, codeCriterion);
                criterion = Restrictions.or(inCriterion, criterion1);
            } else {
                criterion = inCriterion;
            }
            //合并查询条件
            criterions = (Criterion[]) ArrayUtils.add(criterions, 0, criterion);
        } else {
            if (StringUtils.isNotBlank(nameOrCode)) {
                Criterion nameCriterion = Restrictions.like("name", nameOrCode, MatchMode.ANYWHERE);
                Criterion codeCriterion = Restrictions.like("code", nameOrCode, MatchMode.ANYWHERE);
                criterion = Restrictions.or(nameCriterion, codeCriterion);
                //合并查询条件
                criterions = (Criterion[]) ArrayUtils.add(criterions, 0, criterion);
            }
        }

        //分页查询
        Page<Organ> p = new Page<Organ>(rows);//分页对象
        p = organManager.findByCriteria(p, criterions);
        Datagrid<Organ> dg = new Datagrid<Organ>(p.getTotalCount(), p.getResult());
        return dg;
    }

    /**
     * 保存.
     */
    @RequestMapping(value = {"_save"})
    @ResponseBody
    public Result save(@ModelAttribute("model") Organ organ,Long _parentId) {
        Result result = null;
        organ.setParentOrgan(null);
        // 名称重复校验
        Organ checkOrgan = organManager.getByName(organ.getName());
        if (checkOrgan != null && !checkOrgan.getId().equals(organ.getId())) {
            result = new Result(Result.WARN, "名称为[" + organ.getName()
                    + "]已存在,请修正!", "name");
            logger.debug(result.toString());
            return result;
        }

        // 设置上级节点
        if (_parentId != null) {
            Organ parentOrgan = organManager.loadById(_parentId);
            if (parentOrgan == null) {
                logger.error("父级机构[{}]已被删除.", _parentId);
                throw new ActionException("父级机构已被删除.");
            }
            organ.setParentOrgan(parentOrgan);
        }

        //自关联校验
        if (organ.getId() != null) {
            if (organ.getId().equals(organ.get_parentId())) {
                result = new Result(Result.ERROR, "[上级机构]不能与[机构名称]相同.",
                        null);
                logger.debug(result.toString());
                return result;
            }
        }
        organManager.saveOrgan(organ);
        result = Result.successResult();
        return result;
    }

    /**
     * 设置机构用户 页面
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = {"user"})
    public String user(@ModelAttribute("model") Organ organ, Model model) throws Exception {
        List<User> organUsers = organ.getUsers();
        Datagrid<User> dg = new Datagrid<User>(organUsers.size(), organUsers);
        String managerUserCombogridData = JsonMapper.nonDefaultMapper().toJson(dg);
        logger.debug(managerUserCombogridData);
        model.addAttribute("managerUserCombogridData", managerUserCombogridData);
        return "modules/sys/organ-user";
    }


    /**
     * 设置机构用户
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = {"updateOrganUser"})
    @ResponseBody
    public Result updateOrganUser(@RequestParam(value = "userIds", required = false)List<Long> userIds, @ModelAttribute("model") Organ organ, Long managerUserId) throws Exception {
        Result result;
        //设置机构用户
        List<User> userList = Lists.newArrayList();
        if (Collections3.isNotEmpty(userIds)) {
            for (Long userId : userIds) {
                User user = userManager.loadById(userId);
                userList.add(user);
            }
        }
        organ.setUsers(userList);
        organ.setManagerUserId(managerUserId);
        organManager.saveOrgan(organ);
        result = Result.successResult();
        logger.debug(result.toString());
        return result;
    }

    /**
     * 机构树.
     */
    @RequestMapping(value = {"tree"})
    @ResponseBody
    public List<TreeNode> tree(String selectType) throws Exception {
        List<TreeNode> treeNodes = Lists.newArrayList();
        List<TreeNode> titleList = Lists.newArrayList();
        // 添加 "---全部---"、"---请选择---"
        if (!StringUtils.isBlank(selectType)) {
            SelectType s = SelectType.getSelectTypeValue(selectType);
            if (s != null) {
                TreeNode selectTreeNode = new TreeNode("",
                        s.getDescription());
                titleList.add(selectTreeNode);
            }
        }
        treeNodes = organManager.getOrganTree(null, true);
        List<TreeNode> unionList = ListUtils.union(titleList, treeNodes);
        return unionList;
    }

    /**
     * 机构类型下拉列表.
     */
    @RequestMapping(value = {"organTypeCombobox"})
    @ResponseBody
    public List<Combobox> organTypeCombobox(String selectType, String parentOrganType) throws Exception {
        List<Combobox> cList = Lists.newArrayList();
        //为combobox添加  "---全部---"、"---请选择---"
        if (!StringUtils.isBlank(selectType)) {
            SelectType s = SelectType.getSelectTypeValue(selectType);
            if (s != null) {
                Combobox selectCombobox = new Combobox("", s.getDescription());
                cList.add(selectCombobox);
            }
        }

        OrganType _enumParentType = OrganType.getOrganType(parentOrganType);
        if (_enumParentType != null) {
            if (_enumParentType.equals(OrganType.organ)) {
                OrganType[] rss = OrganType.values();
                for (int i = 0; i < rss.length; i++) {
                    Combobox combobox = new Combobox();
                    combobox.setValue(rss[i].getValue().toString());
                    combobox.setText(rss[i].getDescription());
                    cList.add(combobox);
                }
            } else if (_enumParentType.equals(OrganType.department)) {
                Combobox departmentCombobox = new Combobox(OrganType.department.getValue().toString(), OrganType.department.getDescription().toString());
                Combobox groupCombobox = new Combobox(OrganType.group.getValue().toString(), OrganType.group.getDescription().toString());
                cList.add(departmentCombobox);
                cList.add(groupCombobox);
            } else if (_enumParentType.equals(OrganType.group)) {
                Combobox groupCombobox = new Combobox(OrganType.group.getValue().toString(), OrganType.group.getDescription().toString());
                cList.add(groupCombobox);
            }
        } else {
            Combobox groupCombobox = new Combobox(OrganType.organ.getValue().toString(), OrganType.organ.getDescription().toString());
            cList.add(groupCombobox);
        }
        return cList;
    }

    /**
     * 父级机构下拉列表.
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = {"parentOrgan"})
    @ResponseBody
    public List<TreeNode> parentOrgan(String selectType, @ModelAttribute("model") Organ organ) throws Exception {
        List<TreeNode> treeNodes = Lists.newArrayList();
        List<TreeNode> titleList = Lists.newArrayList();
        // 添加 "---全部---"、"---请选择---"
        if (!StringUtils.isBlank(selectType)) {
            SelectType s = SelectType.getSelectTypeValue(selectType);
            if (s != null) {
                TreeNode selectTreeNode = new TreeNode("",
                        s.getDescription());
                titleList.add(selectTreeNode);
            }
        }
        treeNodes = organManager.getOrganTree(organ.getId(), true);
        List<TreeNode> unionList = ListUtils.union(titleList, treeNodes);
        return unionList;
    }

    /**
     * 排序最大值.
     */
    @RequestMapping(value = {"maxSort"})
    @ResponseBody
    public Result maxSort() throws Exception {
        Result result;
        Integer maxSort = organManager.getMaxSort();
        result = new Result(Result.SUCCESS, null, maxSort);
        logger.debug(result.toString());
        return result;
    }


}
