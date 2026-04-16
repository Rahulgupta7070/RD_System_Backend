package com.csrd.RDSystemcd.service;


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
 
public void sendUserEmail(String toEmail, String userName, String rdDate, String amount) {

    try {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(toEmail);
        helper.setSubject("🎉 Welcome to RD System");

        String html = """
        <div style="font-family:Arial;padding:20px;">
            <h2 style="color:#4f46e5;">🎉 Welcome to RD System</h2>

            <p>Dear <b>%s</b>,</p>

            <p>Your RD account has been created successfully.</p>

            <p><b>Start Date:</b> %s</p>
            <p><b>Amount:</b> ₹ %s</p>

        </div>
        """.formatted(userName, rdDate, amount);

        helper.setText(html, true);

        mailSender.send(message);

    } catch (Exception e) {
        e.printStackTrace();
    }
}

//public void sendDepositEmail(String toEmail, String userName, String amount, String date) {
//
//    try {
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true);
//
//        helper.setTo(toEmail);
//        helper.setSubject("💰 Deposit Successful");
//
//        String html = """
//        <div style="font-family:Arial;padding:20px;">
//            <h2 style="color:#16a34a;">💰 Deposit Successful</h2>
//
//            <p>Dear <b>%s</b>,</p>
//
//            <p>Your deposit has been successfully added.</p>
//
//            <p><b>Amount:</b> ₹ %s</p>
//            <p><b>Date:</b> %s</p>
//
//            <p style="color:#555;">Thank you for using RD System 🙏</p>
//        </div>
//        """.formatted(userName, amount, date);
//
//        helper.setText(html, true);
//
//        mailSender.send(message);
//
//    } catch (Exception e) {
//        e.printStackTrace();
//    }
//}


public void sendDepositEmail(String toEmail, String userName, 
        String amount, String date,
        int lateDays, int fineAmount, String status) {

try {
MimeMessage message = mailSender.createMimeMessage();
MimeMessageHelper helper = new MimeMessageHelper(message, true);

helper.setTo(toEmail);
helper.setSubject("💰 Deposit Successful");

String html = """
<div style="font-family:Arial;padding:20px;">
<h2 style="color:#16a34a;">💰 Deposit Successful</h2>

<p>Dear <b>%s</b>,</p>

<p>Your deposit has been successfully added.</p>

<p><b>Amount:</b> ₹ %s</p>
<p><b>Date:</b> %s</p>

<p><b>Status:</b> %s</p>
<p><b>Late Days:</b> %d</p>
<p><b>Fine:</b> ₹ %d</p>

<p style="color:#555;">Thank you for using RD System 🙏</p>
</div>
""".formatted(userName, amount, date, status, lateDays, fineAmount);

helper.setText(html, true);

mailSender.send(message);

} catch (Exception e) {
e.printStackTrace();
}
}



}