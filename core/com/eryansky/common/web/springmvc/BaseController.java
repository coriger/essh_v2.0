package com.eryansky.common.web.springmvc;

import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.SystemException;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import com.eryansky.common.exception.ActionException;
import com.eryansky.common.model.Datagrid;
import com.eryansky.common.model.Result;
import com.eryansky.common.orm.Page;
import com.eryansky.common.orm.PropertyFilter;
import com.eryansky.common.orm.hibernate.EntityManager;
import com.eryansky.common.orm.hibernate.HibernateWebUtils;
import com.eryansky.common.utils.BeanValidators;
import com.eryansky.common.utils.DateUtils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.utils.reflection.MyBeanUtils;
import com.eryansky.common.utils.reflection.ReflectionUtils;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


/**
 * 控制器支持类
 */
public abstract class BaseController<T> {

    /**
     * 日志对象
     */
    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected int page = 1;// 当前页
    protected int rows = Page.DEFAULT_PAGESIZE;// 每页显示记录数
    protected String sort;// 排序字段
    protected String order = Page.ASC;// asc/desc

    protected Class<T> entityClass;

    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected HttpSession session;


    /**
     * 验证Bean实例对象
     */
    @Autowired
    protected Validator validator;

    protected BaseController() {
        this.entityClass = ReflectionUtils.getClassGenricType(getClass());
    }

    /**
     * EntityManager.
     */
    public abstract EntityManager<T, Long> getEntityManager();

    @ModelAttribute
    public void setReqAndRes(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
        this.session = request.getSession();

    }

    @ModelAttribute
    public void getModel(@RequestParam(value = "id", required = false) Long id, Model model) {
        if (id != null) {
            T entity = getEntityManager().getById(id);
            T cloneEntity;
            //对象拷贝
            if(entity != null){
                try {
                    cloneEntity = (T) MyBeanUtils.cloneBean(entity);
                } catch (Exception e) {
                    cloneEntity = entity;
                    logger.error(e.getMessage(),e);
                }
            }else{
                throw new ActionException("ID为["+id+"]的记录不存在或已被其它用户删除！");
            }
            model.addAttribute("model", cloneEntity);
        }
    }


    /**
     * 新增或修改.
     *
     * @param model
     * @return
     */
    @RequestMapping(value = {"save"})
    @ResponseBody
    public Result save(T model) {
        getEntityManager().saveEntity(model);
        return Result.successResult();
    }

    /**
     * 根据ID删除
     *
     * @param id 主键ID
     * @return
     */
    @RequestMapping(value = {"delete"})
    @ResponseBody
    public Result delete(Long id) {
        getEntityManager().deleteById(id);
        return Result.successResult();
    }

    /**
     * 根据ID集合批量删除.
     *
     * @param ids 主键ID集合
     * @return
     */
    @RequestMapping(value = {"remove"})
    @ResponseBody
    public Result remove(@RequestParam(value = "ids", required = false)List<Long> ids) {
        getEntityManager().deleteByIds(ids);
        return Result.successResult();
    }

    /**
     * 用户跳转JSP页面
     *
     * 此方法不考虑权限控制
     *
     * @param folder
     *            路径
     * @param jspName
     *            JSP名称(不加后缀)
     * @return 指定JSP页面
     */
    @RequestMapping("/{folder}/{jspName}")
    public String redirectJsp(@PathVariable String folder, @PathVariable String jspName) {
        System.out.println("redirectJsp");
        return "/" + folder + "/" + jspName;
    }

    /**
     * EasyUI 列表数据
     *
     * @param page  第几页
     * @param rows  页大小
     * @param sort  排序字段
     * @param order 排序方式 增序:'asc',降序:'desc'
     * @return
     * @throws Exception
     */
    @RequestMapping(value = {"datagrid"})
    @ResponseBody
    public Datagrid datagrid(@RequestParam(value = "page", required = false,defaultValue = "1")int page,
                             @RequestParam(value = "rows", required = false,defaultValue = Page.DEFAULT_PAGESIZE+"")int rows,
                             String sort, String order) {
        // 自动构造属性过滤器
        List<PropertyFilter> filters = HibernateWebUtils.buildPropertyFilters(request);
        Page<T> p = getEntityManager().find(page, rows, sort, order, filters);
        Datagrid<T> datagrid = new Datagrid<T>(p.getTotalCount(), p.getResult());
        return datagrid;
    }


    /**
     * 服务端参数有效性验证
     *
     * @param object 验证的实体对象
     * @param groups 验证组
     * @return 验证成功：返回true；严重失败：将错误信息添加到 message 中
     */
    protected boolean beanValidator(Model model, Object object, Class<?>... groups) {
        try {
            BeanValidators.validateWithException(validator, object, groups);
        } catch (ConstraintViolationException ex) {
            List<String> list = BeanValidators.extractPropertyAndMessageAsList(ex, ": ");
            list.add(0, "数据验证失败：");
            addMessage(model, list.toArray(new String[]{}));
            return false;
        }
        return true;
    }

    /**
     * 服务端参数有效性验证
     *
     * @param object 验证的实体对象
     * @param groups 验证组
     * @return 验证成功：返回true；严重失败：将错误信息添加到 flash message 中
     */
    protected boolean beanValidator(RedirectAttributes redirectAttributes, Object object, Class<?>... groups) {
        try {
            BeanValidators.validateWithException(validator, object, groups);
        } catch (ConstraintViolationException ex) {
            List<String> list = BeanValidators.extractPropertyAndMessageAsList(ex, ": ");
            list.add(0, "数据验证失败：");
            addMessage(redirectAttributes, list.toArray(new String[]{}));
            return false;
        }
        return true;
    }

    /**
     * 添加Model消息
     *
     * @param messages 消息
     */
    protected void addMessage(Model model, String... messages) {
        StringBuilder sb = new StringBuilder();
        for (String message : messages) {
            sb.append(message).append(messages.length > 1 ? "<br/>" : "");
        }
        model.addAttribute("message", sb.toString());
    }

    /**
     * 添加Flash消息
     *
     * @param messages 消息
     */
    protected void addMessage(RedirectAttributes redirectAttributes, String... messages) {
        StringBuilder sb = new StringBuilder();
        for (String message : messages) {
            sb.append(message).append(messages.length > 1 ? "<br/>" : "");
        }
        redirectAttributes.addFlashAttribute("message", sb.toString());
    }

    /**
     * 初始化数据绑定
     * 1. 设置被排除的属性 不自动绑定
     * 2. 将所有传递进来的String进行HTML编码，防止XSS攻击
     * 3. 将字段中Date类型转换为String类型
     */
    @InitBinder
    protected void initBinder(WebDataBinder binder) {

        //设置被排除的属性 不自动绑定
        Object annotationValue = AnnotationUtils.getValue(AnnotationUtils.findAnnotation(entityClass, JsonIgnoreProperties.class));
        if(annotationValue != null){
            String[] jsonIgnoreProperties = (String[]) annotationValue;
            binder.setDisallowedFields(jsonIgnoreProperties);
        }

        // Date 类型转换
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(DateUtils.parseDate(text));
            }
        });


        // String类型转换，将所有传递进来的String进行HTML编码，防止XSS攻击
        binder.registerCustomEditor(String.class, new StringEscapeEditor(true,false));

    }

}
