package com.test.unit;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.junit.Test;

public class TestEmailUnit {
	@Test
	public void testEmail(){
		SimpleEmail email = new SimpleEmail();
		email.setHostName("smtp.163.com");
		
		try {
			email.setAuthentication("413246753@163.com", "Apptree2009");
			email.setSSLOnConnect(true);
			email.setFrom("413246753@163.com", "ϵͳ�ʼ�");
			email.setSubject("ϵͳ�ʼ���������Ϣ");
			email.setCharset("UTF-8");
			email.setMsg("���������123456");
			email.addTo("tunancyn@163.com");
			email.send();
		} catch (EmailException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
