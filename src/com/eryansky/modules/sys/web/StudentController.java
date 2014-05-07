package com.eryansky.modules.sys.web;

import org.springframework.beans.factory.annotation.Autowired;

import com.eryansky.common.orm.hibernate.EntityManager;
import com.eryansky.common.web.springmvc.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.eryansky.modules.sys.entity.Student;
import com.eryansky.modules.sys.service.StudentManager;

/**
 * action
 */
@Controller
@RequestMapping(value = "/sys/student")
public class StudentController
    extends BaseController<Student> {

    @Autowired
    private StudentManager studentManager;

    @Override
    public EntityManager<Student, Long> getEntityManager() {
        return studentManager;
    }

    @RequestMapping(value = {""})
    public String list() {
        return "modules/sys/student";
    }

}
