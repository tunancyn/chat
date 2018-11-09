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
			 * ʹ��SqlSessionFactoryBuilder�����mybatis�������ļ���
			 * SqlSessionFactoryBuilder��������Ӧ���Ǿֲ���������Ϊ
			 * ���������ļ�����factory�Ժ�����û������
			 */
			factory = new SqlSessionFactoryBuilder().build(
					Resources.getResourceAsStream("config/mybatis.xml"));
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * ����ģʽ�ṩһ��static�Ĺ�����������factory�������Զ��ύ
	 */
	public static SqlSession getSession(){
		return factory.openSession(true);
	}
	
	private MyBatisUtils(){}
}
