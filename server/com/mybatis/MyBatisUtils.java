package com.mybatis;

import java.io.IOException;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class MyBatisUtils {
	private static SqlSessionFactory factory = null;
	
	static{
		try {
			/**
			 * 使用SqlSessionFactoryBuilder类加载mybatis的配置文件，
			 * SqlSessionFactoryBuilder的作用域应该是局部作用域，因为
			 * 加载配置文件生成factory以后，它就没有用了
			 */
			factory = new SqlSessionFactoryBuilder().build(
					Resources.getResourceAsStream("config/mybatis.xml"));
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 单例模式提供一个static的工厂方法返回factory，设置自动提交
	 */
	public static SqlSession getSession(){
		return factory.openSession(true);
	}
	
	private MyBatisUtils(){}
}
