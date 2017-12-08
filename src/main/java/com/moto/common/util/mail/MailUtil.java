package com.moto.common.util.mail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.moto.common.util.CommonUtility;

public class MailUtil {
	private static final Logger logger = LogManager.getLogger(MailUtil.class);
	
	public static void sendMail(final MailInfo mailInfo) throws MessagingException {
		if (mailInfo == null || StringUtils.isEmpty(mailInfo.getFrom()) || CollectionUtils.isEmpty(mailInfo.getTo())) {
			logger.error("Mail Info is not correct");
			return;
		}
		// Get the default Session object.
		final Session session = Session.getDefaultInstance(CommonUtility.getPropertiesFromFile("resources/email.properties"));
		
		// Create a default MimeMessage object.
		final MimeMessage message = new MimeMessage(session);
		
		// Set From: header field of the header.
		message.setFrom(new InternetAddress(mailInfo.getFrom()));

		// Set To: header field of the header.
		addRecipient(message, Message.RecipientType.TO, mailInfo.getTo());
		
		// Set cc: header field of the header.
		addRecipient(message, Message.RecipientType.CC, mailInfo.getCc());
		
		// Set bcc: header field of the header.
		addRecipient(message, Message.RecipientType.BCC, mailInfo.getBcc());

		// Set Subject: header field
		message.setSubject(mailInfo.getSubject());

		// Send the actual HTML message, as big as you like
		message.setContent(mailInfo.getMessage(), "text/html");

		// Send message
		Transport.send(message);
		logger.info("Sent message successfully....");
	}
	
	private static void addRecipient(final MimeMessage message, final Message.RecipientType recipientType, final List<String> recipientLst) throws MessagingException {
		if (CollectionUtils.isNotEmpty(recipientLst)) {
			Address[] internetAddressLst = null;
			int iCount = 0;
			for (final String recipient : recipientLst) {
				if (StringUtils.isNotEmpty(recipient)) {
					if (internetAddressLst == null) {
						internetAddressLst = new InternetAddress[recipientLst.size()];
					}
					internetAddressLst[iCount++] = new InternetAddress(recipient);
				}
			}
			if (ArrayUtils.isNotEmpty(internetAddressLst)) {
				message.addRecipients(recipientType, internetAddressLst);
			}
		}
	}

	public static void sendMailMessage(String[] mailing_list, String subjectMessage, String filepath, String fileName, String mailBody) throws Exception {
		/*
		 * This function is used to send email
		 */
		try {
			Session session = getMailSession("remotesmtp.mot-mobility.com");
			MimeMessage message = new MimeMessage(session);
			logger.info("message :"+message);
			logger.info("subject : "+subjectMessage);
			logger.info("mail list[0] :"+mailing_list[0]);
			logger.info("mail list[1] :"+mailing_list[1]);
			logger.info("mail list[2] :"+mailing_list[2]);
			logger.info("mail list[3] :"+mailing_list[3]);
			setMessageAttributes(message, subjectMessage, mailing_list);
			addAttachment(message, filepath, fileName, mailBody);
			Transport.send(message);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Send mail failed with Exception" + e);
		}
	}
	
	public static void sendMailMessage(String[] mailing_list, String subjectMessage, String mailBody) throws Exception {
		/*
		 * This function is used to send email
		 */
		try {
			Session session = getMailSession("uskihldsmtp.kih.kmart.com");
			MimeMessage message = new MimeMessage(session);
			logger.info("message :"+message);
			logger.info("subject : "+subjectMessage);
			logger.info("mail list[0] :"+mailing_list[0]);
			logger.info("mail list[1] :"+mailing_list[1]);
			logger.info("mail list[2] :"+mailing_list[2]);
			logger.info("mail list[3] :"+mailing_list[3]);
			setMessageAttributes(message, subjectMessage, mailing_list);
			addAttachment(message, mailBody);
			Transport.send(message);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Send mail failed with Exception" + e);
		}
	}

	private static Session getMailSession(String mailServer) {
		/*
		 * This function is used to get the mail session
		 */
		Properties props = new Properties();
		try {
			props.put("mail.smtp.host", mailServer);
			props.put("mail.smtp.auth", "false");
		} catch (Exception e) {
			logger.error( "Mail session is failed" + e);
		}
		return Session.getDefaultInstance(props, null);
	}

	@SuppressWarnings("unchecked")
	private static InternetAddress[] convertToInternetAddress(String[] addressList) throws AddressException {
		/*
		 * This function converts the given mail id's to the internet address
		 */
		@SuppressWarnings("rawtypes")
		List internetAddressList = new ArrayList();
		InternetAddress[] internetAddress = null;
		try {
			if (addressList != null) {
				int address = addressList.length;
				for (int count = 0; count < address; count++) {
					if (addressList[count] != null && addressList[count].trim().length() != 0) {
						internetAddressList.add(new InternetAddress(addressList[count]));
					}
				}
				/* Convert the ArrayList to an Array */
				int listSize = internetAddressList.size();
				if (listSize != 0) {
					internetAddress = new InternetAddress[listSize];
				}
				for (int counter = 0; counter < listSize; counter++) {
					internetAddress[counter] = (InternetAddress) internetAddressList.get(counter);
				}
			} else {
				internetAddress = null;
			}
		} catch (Exception e) {
			logger.error("Convert to internet address failed" + e);
		}
		return internetAddress;
	}

	private static MimeMessage setMessageAttributes(MimeMessage message, String subjectMessage, String[] mailing_list) throws MessagingException {
		try {
			String mailList[] = new String[4];
			mailList[0] = mailing_list[0];
			mailList[1] = mailing_list[1];
			mailList[2] = mailing_list[2];
			mailList[3] = mailing_list[3];
			String delimiter = ",";
			String newToList = mailList[1];
			String[] temp = new String[1000];
			for (int i = 0; i <= newToList.length(); i++) {
				temp = newToList.split(delimiter);
			}
			String[] toList = new String[1000];
			toList = temp;
			String newccList = mailList[2];
			String[] cctemp = new String[1000];
			for (int i = 0; i <= newccList.length(); i++) {
				cctemp = newccList.split(delimiter);
			}
			String[] ccList = new String[1000];
			ccList = cctemp;
			String newbccList = mailList[3];
			String[] bcctemp = new String[1000];
			for (int i = 0; i <= newbccList.length(); i++) {
				bcctemp = newbccList.split(delimiter);
			}
			String[] bccList = new String[1000];
			bccList = bcctemp;

			InternetAddress[] toAddress = null;
			InternetAddress[] ccAddress = null;
			InternetAddress[] bccAddress = null;
			if (toList == null || toList.length == 0) {
				logger.error("To List is null");
			} else {
				toAddress = convertToInternetAddress(toList);
			}
			message.setFrom(new InternetAddress(mailList[0]));
			message.setRecipients(Message.RecipientType.TO, toAddress);
			ccAddress = convertToInternetAddress(ccList);
			if (ccAddress != null) {
				message.setRecipients(Message.RecipientType.CC, ccAddress);
			}
			bccAddress = convertToInternetAddress(bccList);
			if (bccAddress != null) {
				message.setRecipients(Message.RecipientType.BCC, bccAddress);
			}
			message.setSentDate(new Date(System.currentTimeMillis()));
			message.setSubject(subjectMessage);

		} catch (Exception e) {
			logger.error("Set message attributes failed" + e);
		}
		return message;
	}

	private static MimeMessage addAttachment(MimeMessage message, String filePath, String fileName, String mailBody) throws MessagingException {
		try {
			MimeMultipart multipart = new MimeMultipart("related");
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(mailBody, "text/html");
			multipart.addBodyPart(messageBodyPart);
			filePath = filePath.concat(fileName);
			if (filePath.length() != 0) {
				messageBodyPart = new MimeBodyPart();
				DataSource source = new FileDataSource(filePath);
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(fileName);
				multipart.addBodyPart(messageBodyPart);
			}
			message.setContent(multipart);
			message.addHeader("X-Priority", "3");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Add attachment failed" +e);
		}
		return message;
	}

	private static MimeMessage addAttachment(MimeMessage message, String mailBody) throws MessagingException {
		try {
			MimeMultipart multipart = new MimeMultipart("related");
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(mailBody, "text/html");
			multipart.addBodyPart(messageBodyPart);
			message.setContent(multipart);
			message.addHeader("X-Priority", "3");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Add attachment failed" + e.toString());
		}
		return message;
	}
}