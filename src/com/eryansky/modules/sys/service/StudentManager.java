package com.eryansky.modules.sys.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eryansky.modules.sys.entity.Student ;
import com.eryansky.common.orm.hibernate.HibernateDao;
import com.eryansky.common.orm.hibernate.EntityManager;
/**
 * service
 */
@Service
public class StudentManager extends EntityManager<Student, Long>  {
	private static final Logger logger = LoggerFactory.getLogger(StudentManager.class);
	
	private HibernateDao studentDao;

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        studentDao = new HibernateDao<Student, Long>(sessionFactory, Student.class);
    }

    @Override
    protected HibernateDao<Student, Long> getEntityDao() {
        return studentDao;
    }

}
