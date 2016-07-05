package com.user.manager;

import com.apartment.database.tables.UserFlatMapping;
import com.apartment.events.dispatch.DispatchMessageQueue;
import com.apartment.events.dispatch.EmailAndSmsMessage;
import com.apartment.events.dispatch.EmailMessage;
import com.apartment.events.dispatch.SmsMessage;

public class EmailAndSmsManager {
	public void sendEmailAndSms(String title, String msg, String shortText,
			String fromName, String fromEmail, String fromNumber,
			UserFlatMapping fromUser, UserFlatMapping toAddress) {
		EmailAndSmsMessage message = new EmailAndSmsMessage();
		message.addToAddress(toAddress);
		message.setFromAddress(fromName, fromEmail, fromNumber, fromUser);
		message.setTitle(title);
		message.setText(shortText);
		message.setBody(msg);
		message.setApartmentId(toAddress.getFlat().getApartment().getId());		
		DispatchMessageQueue.getInstance().addMessageToQueue(message);
	}	
}
