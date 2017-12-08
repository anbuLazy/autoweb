package com.moto.common.util.mail;

import java.util.List;

public class MailInfo {
	private String from;
	private List<String> to;
	private List<String> cc;
	private List<String> bcc;
	private String subject;
	private String message;

	public MailInfo() {
	}

	public MailInfo(final String from, final List<String> to, final List<String> cc,
			final List<String> bcc, final String subject, final String message) {
		this.from = from;
		this.to = to;
		this.cc = cc;
		this.bcc = bcc;
		this.subject = subject;
		this.message = message;
	}

	public final String getFrom() {
		return from;
	}

	public final void setFrom(final String from) {
		this.from = from;
	}

	public final List<String> getTo() {
		return to;
	}

	public final void setTo(final List<String> to) {
		this.to = to;
	}

	public final List<String> getCc() {
		return cc;
	}

	public final void setCc(final List<String> cc) {
		this.cc = cc;
	}

	public final List<String> getBcc() {
		return bcc;
	}

	public final void setBcc(final List<String> bcc) {
		this.bcc = bcc;
	}

	public final String getSubject() {
		return subject;
	}

	public final void setSubject(final String subject) {
		this.subject = subject;
	}

	public final String getMessage() {
		return message;
	}

	public final void setMessage(final String message) {
		this.message = message;
	}
}