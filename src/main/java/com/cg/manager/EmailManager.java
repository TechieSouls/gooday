package com.cg.manager;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

import com.cg.user.bo.User;

@Service
public class EmailManager {
	
    // Sender's email ID needs to be mentioned
	private static String from = "no-reply@cenesgroup.com";
	private Session session = null;
	
	public void sendForgotPasswordLink(User user) {
		// Recipient's email ID needs to be mentioned.
	      try {
	    	  setAuthenticateSession();
	          // Create a default MimeMessage object.
	    	  MimeMessage message = new MimeMessage(session);    
	           message.addRecipient(Message.RecipientType.TO,new InternetAddress(user.getEmail()));    
	           message.setSubject("Reset your password");    
	           
	           StringBuffer sb = new StringBuffer();
	           sb.append("Hi "+user.getName()+",").append("\n");
	           sb.append("We received a request to reset the password for your account. Please click on the link below or paste this into your browser to complete the process. This link is valid for one hour.").append("\n");
	           sb.append("http://cenes.test2.redblink.net/reset-password.html?resetToken="+user.getResetToken()).append("\n");
	           sb.append("If you did not ask to change your password, please ignore this email and your account will remain unchanged.").append("\n");
	           message.setText(sb.toString());    
	           //send message  
	           Transport.send(message);    
	           System.out.println("message sent successfully");    

	          System.out.println("Sent message successfully....");
	       } catch (Exception ex) {
	    	   ex.printStackTrace();
	       }
	}
	
	public void setAuthenticateSession() {
		// Get system properties
      	Properties props = System.getProperties();
      	props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

      // Get the default Session object.
		// check the authentication
		session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(from, "Frodo2234");
			}
		});
	}
	
}
