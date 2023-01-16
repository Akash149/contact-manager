package com.smartcontactmanager.service;

import java.util.Properties;

import org.springframework.stereotype.Service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {

    public boolean sendEmail(String subject, String message, String to) {
        boolean flag = false;
        String from = "mr.iotdeveloper@gmail.com";
        String password = "password";
        // email send code   
         //Get System properties
         Properties properties = System.getProperties();
        
         //setting important information to properties object
         
         //Host set
         properties.put("mail.smtp.host","smtp.gmail.com");
         properties.put("mail.smtp.port", "465");
         properties.put("mail.smtp.ssl.enable", "true");
         properties.put("mail.smtp.auth", "true");
         
         //To get session object....
         Session session = Session.getInstance(properties, new Authenticator() {
             
             @Override
             protected PasswordAuthentication getPasswordAuthentication() {
                 
                 return new PasswordAuthentication(from, password);
             }
         });
         
         //session.setDebug(true);
         
         //compose the message [text, multimedia]
         MimeMessage m = new MimeMessage(session); 
         
         try {
             //from email
             m.setFrom(from);
             
             //adding recipient to message
             m.addRecipient(Message.RecipientType.TO,new InternetAddress(to));
             
             //adding subject
             m.setSubject(subject);
             
             //adding text to message
             m.setContent(message,"text/html");
             
             //Send the message using Transport class
             Transport.send(m);
             System.out.println("Email has been sent....");
             return flag=true;
         } catch(Exception e) {
             
             e.printStackTrace();
             return flag=false;
         }
    }
    
}
