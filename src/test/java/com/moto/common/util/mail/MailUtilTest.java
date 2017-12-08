package com.moto.common.util.mail;

import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;

public class MailUtilTest {
	private static final Logger logger = LogManager.getLogger(MailUtilTest.class);

	@Test
	public void sendMailTest() throws MessagingException {
		final MailInfo mailInfo =  new MailInfo();
		mailInfo.setFrom("itauto1@motorola.com");
		
		final List<String> to = new ArrayList<>();
		to.add("itauto1@motorola.com");
		mailInfo.setTo(to);
		
		final List<String> cc = new ArrayList<>();
		cc.add("itauto1@motorola.com");
		mailInfo.setCc(cc);
		
		final List<String> bcc = new ArrayList<>();
		bcc.add("itauto1@motorola.com");
		mailInfo.setBcc(bcc);
		
		mailInfo.setSubject("Test Mail");
		mailInfo.setMessage("This is a test mail");
		
		MailUtil.sendMail(mailInfo);
	}
}