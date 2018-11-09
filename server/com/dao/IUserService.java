package com.dao;

import java.util.List;

import com.bean.User;

public interface IUserService {
	
	void insertUser(User user);
	User queryUser(String name);
	List<User> queryAllUsers();
	void changePwd(String newpwd,String name);
}
