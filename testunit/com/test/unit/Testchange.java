package com.test.unit;

import org.apache.ibatis.session.SqlSession;

import com.dao.IUserService;
import com.mybatis.MyBatisUtils;

public class Testchange {
	public static void main(String[] args){
		String newpwd = "2";
		String name = "1";
		SqlSession session = MyBatisUtils.getSession();
		try{
			IUserService service = session.getMapper(IUserService.class);
			service.changePwd(newpwd,name);
		}finally{
			session.close();
		}
	}
}
