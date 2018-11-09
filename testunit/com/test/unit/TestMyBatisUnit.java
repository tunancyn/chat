package com.test.unit;

import javax.jws.soap.SOAPBinding.Use;

import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import com.bean.User;
import com.dao.IUserService;
import com.mybatis.MyBatisUtils;

public class TestMyBatisUnit {
	//@Test
	public void testMybatis(){
		SqlSession session = MyBatisUtils.getSession();
		try{
			IUserService service = session.getMapper(IUserService.class);
			User user = new User();
			user.setUsername("zhang san");
			user.setPassword("111111");
			user.setEmail("111@163.com");
			service.insertUser(user);
		}finally{
			session.close();
		}
	}
	
	//@Test
	public void p(){
		User user = null;
		SqlSession session = MyBatisUtils.getSession();
		try{
			IUserService service = session.getMapper(IUserService.class);
			user = service.queryUser("zhang san");
			System.out.println(user + " ");
		}finally{
			session.close();
		}
	}
}
