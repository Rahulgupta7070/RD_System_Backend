package com.csrd.RDSystemcd.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.csrd.RDSystemcd.repo.AdminRepo;
import com.csrd.RDSystemcd.entity.Admin;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final AdminRepo repo;

    public EmailService(JavaMailSender mailSender, AdminRepo repo) {
        this.mailSender = mailSender;
        this.repo = repo;
    }

 public void sendLoginAlert(String adminEmail) {

    try {

        Admin superAdmin = repo.findByRole("ROLE_SUPER_ADMIN");

        if (superAdmin == null) {
            System.out.println("No Super Admin found ❌");
            return;
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(superAdmin.getEmail());
        helper.setSubject("⚠️ Admin Login Alert");

        String html = """
        <div style="font-family:Arial;padding:20px;">
            <h2 style="color:#ff4d4f;">🚨 Admin Login Detected</h2>

            <p><b>Admin Email:</b> %s</p>
            <p><b>Time:</b> %s</p>

            <hr>

            <p style="color:red;">
                If this was not you, please take action immediately!
            </p>
        </div>
        """.formatted(adminEmail, java.time.LocalDateTime.now());

        helper.setText(html, true); // 🔥 HTML enable

        mailSender.send(message);

    } catch (Exception e) {
        e.printStackTrace();
    }
}
 
 public void sendLogoutAlert(String adminEmail) {

	    try {

	        Admin superAdmin = repo.findByRole("ROLE_SUPER_ADMIN");

	        if (superAdmin == null) return;

	        MimeMessage message = mailSender.createMimeMessage();
	        MimeMessageHelper helper = new MimeMessageHelper(message, true);

	        helper.setTo(superAdmin.getEmail());
	        helper.setSubject("🔴 Admin Logout Alert");

	        String html = """
	        <div style="font-family:Arial;padding:20px;">
	            <h2 style="color:#ff4d4f;">🔴 Admin Logged Out</h2>

	            <p><b>Admin Email:</b> %s</p>
	            <p><b>Time:</b> %s</p>

	        </div>
	        """.formatted(
	            adminEmail,
	            java.time.LocalDateTime.now()
	        );

	        helper.setText(html, true);

	        mailSender.send(message);

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}