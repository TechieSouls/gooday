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
	           message.setContent(resetPasswordHTMLTemplate(user.getResetToken()), "text/html");   
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
	
	public String resetPasswordHTMLTemplate(String resetPasswordToken) {
		String resetPasswordLink = "https://www.cenesgroup.com/app/forgetPassword?resetToken="+resetPasswordToken;
		String htmlContent = "<html>\n" + 
				"<body style=\"font-size: 18px;font-family: AvenirLTStd Book;color: #595757;\">\n" + 
				"	\n" + 
				"<div align=\"center\"><img src=\"https://www.cenesgroup.com/assets/images/Logo.png\"></div>\n" + 
				"<div align=\"center\">\n" + 
				"	\n" + 
				"	<h3 style=\"letter-spacing: 1px;font-size: 28px;font-weight: lighter;\">\n" + 
				"		Reset Password\n" + 
				"	</h3>\n" + 
				"</div>\n" + 
				"<br/>\n" + 
				"<div align=\"center\">Hi Louisa,</div>\n" + 
				"<br>\n" + 
				"<br>\n" + 
				"<div align=\"center\">You have requested a link to reset password. Click the link below to complete your request:</div>\n" + 
				"<br/>\n" + 
				"<br/>\n" + 
				"<br/>\n" + 
				"\n" + 
				"<div align=\"center\">\n" + 
				"	<a href=\""+resetPasswordLink+"\" style=\"text-decoration: none;color: #EE9B26;\"><span style=\"font-size: 22px;border:2px solid #EE9B26; padding:25px 100px; border-radius: 40px;\">Reset Password</span></a>\n" + 
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
				"}\n" + 
				"  </style>\n" + 
				"</html>\n" + 
				"";
		
		return htmlContent;
	}
	
	/*public static void main(String[] args) {
		User user = new User();
		user.setEmail("mspanesar145@gmail.com");
		new EmailManager().sendForgotPasswordLink(user);
	}*/
}
