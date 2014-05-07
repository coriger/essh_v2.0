package com.eryansky.modules.sys.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.eryansky.common.orm.entity.BaseEntity;

/**
 * entity
 */
@Entity
@Table(name="t_sys_student")
@JsonIgnoreProperties (value = { "hibernateLazyInitializer", "handler","fieldHandler" })
public class Student extends BaseEntity{

   	/**
	 *
	 **/
	    @Column(name = "name", length = 255)
    private String name;
   	/**
	 *
	 **/
	    @Column(name = "studentNo", length = 10)
    private Integer studentno;

    public void setName(String name){
       this.name = name;
    }
    
    public String getName(){
       return this.name ;
    }
    public void setStudentno(Integer studentno){
       this.studentno = studentno;
    }
    
    public Integer getStudentno(){
       return this.studentno ;
    }
	
}
