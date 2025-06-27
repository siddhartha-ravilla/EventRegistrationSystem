package com.eventregistration.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class QRCodeService {
    
    @Value("${qr.width:300}")
    private int width;
    
    @Value("${qr.height:300}")
    private int height;
    
    @Value("${qr.format:PNG}")
    private String format;
    
    public String generateQRCode(String data) throws WriterException, IOException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2);
        
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height, hints);
        
        BufferedImage qrImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = qrImage.createGraphics();
        
        // Set background to white
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        
        // Set QR code color to black
        graphics.setColor(Color.BLACK);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (bitMatrix.get(x, y)) {
                    graphics.fillRect(x, y, 1, 1);
                }
            }
        }
        
        graphics.dispose();
        
        // Convert to Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, format, baos);
        byte[] imageBytes = baos.toByteArray();
        
        return "data:image/" + format.toLowerCase() + ";base64," + Base64.getEncoder().encodeToString(imageBytes);
    }
    
    public String generateTicketQRCode(String ticketNumber, String eventId, String userId) {
        String qrData = String.format("TICKET:%s|EVENT:%s|USER:%s", ticketNumber, eventId, userId);
        try {
            return generateQRCode(qrData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
} 