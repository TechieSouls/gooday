package com.cg.manager;

import java.net.URLEncoder;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cg.user.bo.User;

@Service
public class EmailManager {
	
	@Value("${cenes.domain}")
	private String domain;
	
    // Sender's email ID needs to be mentioned
	private static String from = "support@cenesgroup.com";
	private Session session = null;
	
	public void sendForgotPasswordLink(User user) {
		// Recipient's email ID needs to be mentioned.
	      try {
	    	  setAuthenticateSession();
	          // Create a default MimeMessage object.
	    	  MimeMessage message = new MimeMessage(session);    
	           message.addRecipient(Message.RecipientType.TO,new InternetAddress(user.getEmail()));    
	           message.setSubject("Reset your password");    
	           message.setContent(resetPasswordHTMLTemplate(user.getResetToken(), user.getName()), "text/html");   
	           //send message  
	           Transport.send(message);    
	           System.out.println("message sent successfully");    

	          System.out.println("Sent message successfully....");
	       } catch (Exception ex) {
	    	   ex.printStackTrace();
	       }
	}
	
	
	public void sendForgotPasswordConfirmationLink(User user) {
		// Recipient's email ID needs to be mentioned.
	      try {
	    	  setAuthenticateSession();
	          // Create a default MimeMessage object.
	    	  MimeMessage message = new MimeMessage(session);    
	           message.addRecipient(Message.RecipientType.TO,new InternetAddress(user.getEmail()));    
	           message.setSubject("Email Confirmation Link");    
	           message.setContent(emailConfirmationHTMLTemplate(user.getResetToken(), user.getName()), "text/html");   
	           //send message  
	           Transport.send(message);    
	           System.out.println("message sent successfully");    

	          System.out.println("Sent message successfully....");
	       } catch (Exception ex) {
	    	   ex.printStackTrace();
	       }
	}
	
	public void sendUpdatePhoneNumberConfirmationLink(User user, String newPhoneNumber) {
		// Recipient's email ID needs to be mentioned.
	      try {
	    	  setAuthenticateSession();
	          // Create a default MimeMessage object.
	    	  MimeMessage message = new MimeMessage(session);    
	           message.addRecipient(Message.RecipientType.TO,new InternetAddress(user.getEmail()));    
	           message.setSubject("Update Phone Number Confirmation Link");    
	           message.setContent(phoneUpdateConfirmationHTMLTemplate(user, newPhoneNumber), "text/html");   
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
				return new PasswordAuthentication(from, "Cenes1234");
			}
		});
	}
	
	public String resetPasswordHTMLTemplate(String resetPasswordToken, String name) {
		String resetPasswordLink = "https://betaweb.cenesgroup.com/app/forgetPassword?resetToken="+resetPasswordToken;
		String htmlContent = "<html>\n" + 
				"<body style=\"font-size: 18px;font-family: AvenirLTStd Book;color: #595757;padding-left:10px;padding-right:10px;\">\n" + 
				"	\n" + 
				"<div align=\"center\"><img src=\"https://www.cenesgroup.com/assets/images/Logo.png\"></div>\n" + 
				"<div align=\"center\">\n" + 
				"	\n" + 
				"	<h3 style=\"letter-spacing: 1px;font-size: 28px;font-weight: lighter;\">\n" + 
				"		Reset Password\n" + 
				"	</h3>\n" + 
				"</div>\n" + 
				"<br/>\n" + 
				"<div align=\"center\">Hi "+name+",</div>\n" + 
				"<br>\n" + 
				"<br>\n" + 
				"<div align=\"center\">You have requested a link to reset password. Click the link below to complete your request:</div>\n" + 
				"<br/>\n" + 
				"<br/>\n" + 
				"<br/>\n" + 
				"\n" + 
				"<div align=\"center\">\n" + 
				"	<a href=\""+resetPasswordLink+"\" class=\"reset-btn\" style=\"text-decoration: none;color: #EE9B26;\"><span style=\"font-size: 18px;border:2px solid #EE9B26; padding:15px 75px; border-radius: 35px;text-align:center;\">Reset Password</span></a>\n" + 
				"</div>\n" + 
				"<br>\n" + 
				"<br>\n" + 
				"<br>\n" + 
				"<div align=\"center\">If you didn't request this, please ignore this email.</div>\n" + 
				"<br>\n" + 
				"\n" + 
				"<div align=\"center\">Your account remains the same until you access the link above and reset your password.</div>\n" + 
				"<br>\n" + 
				"<br>\n" + 
				"<div align=\"center\">&#169; CENES</div>\n" + 
				"</body>\n" + 
				"<style type=\"text/css\">\n" + 
				"\n" + 
				" @font-face {\n" + 
				"    font-family: 'AvenirLTStd Book';\n" + 
				"    src: url('https://www.cenesgroup.com/assets/fonts/AvenirLTStd-Book.eot');\n" + 
				"    src: url('https://www.cenesgroup.com/assets/fonts/AvenirLTStd-Book.eot?#iefix') format('embedded-opentype'),\n" + 
				"        url('https://www.cenesgroup.com/assets/fonts/AvenirLTStd-Book.woff2') format('woff2'),\n" + 
				"        url('https://www.cenesgroup.com/assets/fonts/AvenirLTStd-Book.woff') format('woff'),\n" + 
				"        url('https://www.cenesgroup.com/assets/fonts/AvenirLTStd-Book.ttf') format('truetype'),\n" + 
				"        url('https://www.cenesgroup.com/assets/fonts/AvenirLTStd-Book.svg#AvenirLTStd-Book') format('svg');\n" + 
				"    font-weight: normal;\n" + 
				"    font-style: normal;\n" + 
				"}</style>\n" + 
				"</html>\n" + 
				"";
		
		return htmlContent;
	}
	
	public String emailConfirmationHTMLTemplate(String resetPasswordToken, String name) {
		String resetPasswordLink = domain+"/auth/forgetPasswordConfirmation?resetToken="+resetPasswordToken;
		String htmlContent = "<html>\n" + 
				"<body style=\"font-size: 18px;font-family: AvenirLTStd Book;color: #595757;padding-left:10px;padding-right:10px;\">\n" + 
				"	\n" + 
				"<div align=\"center\"><img src=\"https://www.cenesgroup.com/assets/images/Logo.png\"></div>\n" + 
				"<div align=\"center\">\n" + 
				"	\n" + 
				"	<h3 style=\"letter-spacing: 1px;font-size: 28px;font-weight: lighter;\">\n" + 
				"		Reset Password\n" + 
				"	</h3>\n" + 
				"</div>\n" + 
				"<br/>\n" + 
				"<div align=\"center\">Hi "+name+",</div>\n" + 
				"<br>\n" + 
				"<br>\n" + 
				"<div align=\"center\">You have requested a link to reset password. Click the Confirmation link below to complete your request:</div>\n" + 
				"<br/>\n" + 
				"<br/>\n" + 
				"<br/>\n" + 
				"\n" + 
				"<div align=\"center\">\n" + 
				"	<a href=\""+resetPasswordLink+"\" class=\"reset-btn\" style=\"text-decoration: none;color: #EE9B26;\"><span style=\"font-size: 18px;border:2px solid #EE9B26; padding:15px 75px; border-radius: 35px;text-align:center;\">Reset Password</span></a>\n" + 
				"</div>\n" + 
				"<br>\n" + 
				"<br>\n" + 
				"<br>\n" + 
				"<div align=\"center\">If you didn't request this, please ignore this email.</div>\n" + 
				"<br>\n" + 
				"\n" + 
				"<div align=\"center\">Your account remains the same until you access the link above and reset your password.</div>\n" + 
				"<br>\n" + 
				"<br>\n" + 
				"<div align=\"center\">&#169; CENES</div>\n" + 
				"</body>\n" + 
				"<style type=\"text/css\">\n" + 
				"\n" + 
				" @font-face {\n" + 
				"    font-family: 'AvenirLTStd Book';\n" + 
				"    src: url('https://www.cenesgroup.com/assets/fonts/AvenirLTStd-Book.eot');\n" + 
				"    src: url('https://www.cenesgroup.com/assets/fonts/AvenirLTStd-Book.eot?#iefix') format('embedded-opentype'),\n" + 
				"        url('https://www.cenesgroup.com/assets/fonts/AvenirLTStd-Book.woff2') format('woff2'),\n" + 
				"        url('https://www.cenesgroup.com/assets/fonts/AvenirLTStd-Book.woff') format('woff'),\n" + 
				"        url('https://www.cenesgroup.com/assets/fonts/AvenirLTStd-Book.ttf') format('truetype'),\n" + 
				"        url('https://www.cenesgroup.com/assets/fonts/AvenirLTStd-Book.svg#AvenirLTStd-Book') format('svg');\n" + 
				"    font-weight: normal;\n" + 
				"    font-style: normal;\n" + 
				"}</style>\n" + 
				"</html>\n" + 
				"";
		
		return htmlContent;
	}

	public String phoneUpdateConfirmationHTMLTemplate(User user, String newPhone) {
		String updatePhoneLink = domain+"/auth/phoneNumberUpdateConfirmation?phone="+URLEncoder.encode(newPhone)+"&email="+URLEncoder.encode(user.getEmail());
		String htmlContent = "<html>\n" + 
				"<body style=\"font-size: 18px;font-family: AvenirLTStd Book;color: #595757;padding-left:10px;padding-right:10px;\">\n" + 
				"	\n" + 
				"<div align=\"center\"><img src=\"https://www.cenesgroup.com/assets/images/Logo.png\"></div>\n" + 
				"<div align=\"center\">\n" + 
				"	\n" + 
				"	<h3 style=\"letter-spacing: 1px;font-size: 28px;font-weight: lighter;\">\n" + 
				"		Update Phone Number\n" + 
				"	</h3>\n" + 
				"</div>\n" + 
				"<br/>\n" + 
				"<div align=\"center\">Hi "+user.getName()+",</div>\n" + 
				"<br>\n" + 
				"<br>\n" + 
				"<div align=\"center\">You have requested to update your CENES account phone number to "+newPhone+" for "+user.getEmail()+". Click the Confirmation link below to confirm the changes:</div>\n" + 
				"<br/>\n" + 
				"<br/>\n" + 
				"<br/>\n" + 
				"\n" + 
				"<div align=\"center\">\n" + 
				"	<a href=\""+updatePhoneLink+"\" class=\"reset-btn\" style=\"text-decoration: none;color: #EE9B26;\"><span style=\"font-size: 18px;border:2px solid #EE9B26; padding:15px 40px; border-radius: 35px;text-align:center;\">Update Phone Number</span></a>\n" + 
				"</div>\n" + 
				"<br>\n" + 
				"<br>\n" + 
				"<br>\n" + 
				"<div align=\"center\">If you didn't request this, please ignore this email.</div>\n" + 
				"<br>\n" + 
				"\n" + 
				"<div align=\"center\">Your phone number remains the same until you access the link above and update the phone number.</div>\n" + 
				"<br>\n" + 
				"<br>\n" + 
				"<div align=\"center\">&#169; CENES</div>\n" + 
				"</body>\n" + 
				"<style type=\"text/css\">\n" + 
				"\n" + 
				" @font-face {\n" + 
				"    font-family: 'AvenirLTStd Book';\n" + 
				"    src: url('https://www.cenesgroup.com/assets/fonts/AvenirLTStd-Book.eot');\n" + 
				"    src: url('https://www.cenesgroup.com/assets/fonts/AvenirLTStd-Book.eot?#iefix') format('embedded-opentype'),\n" + 
				"        url('https://www.cenesgroup.com/assets/fonts/AvenirLTStd-Book.woff2') format('woff2'),\n" + 
				"        url('https://www.cenesgroup.com/assets/fonts/AvenirLTStd-Book.woff') format('woff'),\n" + 
				"        url('https://www.cenesgroup.com/assets/fonts/AvenirLTStd-Book.ttf') format('truetype'),\n" + 
				"        url('https://www.cenesgroup.com/assets/fonts/AvenirLTStd-Book.svg#AvenirLTStd-Book') format('svg');\n" + 
				"    font-weight: normal;\n" + 
				"    font-style: normal;\n" + 
				"}</style>\n" + 
				"</html>\n" + 
				"";
		
		return htmlContent;
	}
	/*public static void main(String[] args) {
		User user = new User();
		user.setResetToken("wewfwefw");
		user.setName("adadf");
		user.setEmail("neha.thamman89@gmail.com");
		new EmailManager().sendForgotPasswordLink(user);
	}*/
}
