package com.services;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

public class SendEmail {
	private String youremail;
	private String yourpwd;
	
	public SendEmail(){
	}
	public void setMail(String emails){
		this.youremail = emails;
	}
	public void setPwd(String yourpwds){
		this.yourpwd = yourpwds;
	}
	public void testEmail(){
		SimpleEmail email = new SimpleEmail();
		email.setHostName("smtp.163.com");
		
		try {
			email.setAuthentication("413246753@163.com", "Apptree2009");
			email.setSSLOnConnect(true);
			email.setFrom("413246753@163.com", "ϵͳ�ʼ�");
			email.setSubject("ϵͳ�ʼ���������Ϣ");
			email.setCharset("UTF-8");
			email.setMsg("���������"+yourpwd); 
			email.addTo(youremail);
			email.send();
		} catch (EmailException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
