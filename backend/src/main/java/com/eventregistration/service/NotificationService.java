package com.eventregistration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.Map;

@Service
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired
    private SesClient sesClient;
    
    @Autowired
    private SnsClient snsClient;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Value("${aws.ses.from-email}")
    private String fromEmail;
    
    @Value("${aws.sns.topic-arn}")
    private String snsTopicArn;
    
    @Async
    public void sendTicketConfirmationEmail(String toEmail, String userName, String eventTitle, 
                                          String ticketNumber, String qrCodeData) {
        try {
            String subject = "Ticket Confirmation - " + eventTitle;
            String htmlBody = generateTicketConfirmationEmail(userName, eventTitle, ticketNumber, qrCodeData);
            
            SendEmailRequest request = SendEmailRequest.builder()
                    .source(fromEmail)
                    .destination(Destination.builder().toAddresses(toEmail).build())
                    .message(Message.builder()
                            .subject(Content.builder().data(subject).charset("UTF-8").build())
                            .body(Body.builder().html(Content.builder().data(htmlBody).charset("UTF-8").build()).build())
                            .build())
                    .build();
            
            SendEmailResponse response = sesClient.sendEmail(request);
            logger.info("Ticket confirmation email sent successfully: {}", response.messageId());
            
        } catch (Exception e) {
            logger.error("Failed to send ticket confirmation email", e);
        }
    }
    
    @Async
    public void sendEventUpdateNotification(String eventTitle, String message) {
        try {
            Map<String, Object> notificationData = Map.of(
                "eventTitle", eventTitle,
                "message", message,
                "timestamp", System.currentTimeMillis()
            );
            
            String messageJson = objectMapper.writeValueAsString(notificationData);
            
            PublishRequest request = PublishRequest.builder()
                    .topicArn(snsTopicArn)
                    .message(messageJson)
                    .subject("Event Update: " + eventTitle)
                    .build();
            
            PublishResponse response = snsClient.publish(request);
            logger.info("Event update notification sent successfully: {}", response.messageId());
            
        } catch (Exception e) {
            logger.error("Failed to send event update notification", e);
        }
    }
    
    @Async
    public void sendAdminNotification(String subject, String message) {
        try {
            SendEmailRequest request = SendEmailRequest.builder()
                    .source(fromEmail)
                    .destination(Destination.builder().toAddresses("admin@eventregistration.com").build())
                    .message(Message.builder()
                            .subject(Content.builder().data(subject).charset("UTF-8").build())
                            .body(Body.builder().text(Content.builder().data(message).charset("UTF-8").build()).build())
                            .build())
                    .build();
            
            SendEmailResponse response = sesClient.sendEmail(request);
            logger.info("Admin notification sent successfully: {}", response.messageId());
            
        } catch (Exception e) {
            logger.error("Failed to send admin notification", e);
        }
    }
    
    private String generateTicketConfirmationEmail(String userName, String eventTitle, 
                                                 String ticketNumber, String qrCodeData) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Ticket Confirmation</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .ticket-info { background-color: white; padding: 15px; margin: 15px 0; border-left: 4px solid #4CAF50; }
                    .qr-code { text-align: center; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎫 Ticket Confirmation</h1>
                    </div>
                    <div class="content">
                        <p>Dear %s,</p>
                        <p>Your ticket has been successfully purchased!</p>
                        
                        <div class="ticket-info">
                            <h3>Event Details:</h3>
                            <p><strong>Event:</strong> %s</p>
                            <p><strong>Ticket Number:</strong> %s</p>
                        </div>
                        
                        <div class="qr-code">
                            <h3>Your QR Code:</h3>
                            <img src="%s" alt="QR Code" style="max-width: 200px;">
                            <p><small>Present this QR code at the event entrance</small></p>
                        </div>
                        
                        <p>Please keep this email safe and present the QR code at the event entrance.</p>
                        <p>Thank you for your registration!</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated message from Event Registration System</p>
                    </div>
                </div>
            </body>
            </html>
            """, userName, eventTitle, ticketNumber, qrCodeData);
    }
} 