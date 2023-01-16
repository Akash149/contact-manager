package com.smartcontactmanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smartcontactmanager.dao.UserRepository;
import com.smartcontactmanager.entities.User;
import com.smartcontactmanager.helper.Message;
import com.smartcontactmanager.service.MailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {

    // @Autowired
    // private MailService mailService;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private User user;
    boolean status;
    String email;

    int otp;

    @GetMapping("/forgot-password")
    public String openEmailForm(Model model) {
        model.addAttribute("title", "Forgot Password");
        return "forgetform";
    }

    @PostMapping("/send-otp")
    public String sendOTP(@RequestParam("email") String email, RedirectAttributes rat, Model model) {

        this.email = email;
        user = userRepository.getUserByUserName(email);
        if(user != null) {

            //Generating otp of 4 digit
            otp = (int) (Math.random()*9000)+1000;
            System.out.println("OTP: "+otp);

            //code for send otp mail
            String subject = "OTP";
            String to = email;
            String message = "<div style='border:1px solid #e2e2e2; padding:20px'>" +
            "Hey, "+user.getName()+"<br>"+" Your OTP is "+otp+". don't share with other person."+"</div>";
            System.out.println("Message: "+message);
            try {
                // this.emailService.sendSimpleMail(new EmailDetails(to,message,subject));
                // send OTP mail
                status = this.mailService.sendEmail(subject, message, to);
                rat.addFlashAttribute("success", "OTP sent on your mail");
            } catch (Exception e) {
                // ! handle exception
                rat.addFlashAttribute("error", "Something went wrong !");
                e.printStackTrace();
                System.out.println(e.getMessage());
                model.addAttribute("title", "Forgot Password");
                return "redirect:/forgot-password";
            }

        } else {
            rat.addFlashAttribute("error", "This email is not registered !");
            return "redirect:/forgot-password";
        }
        model.addAttribute("title", "Enter OTP");
        return "verifyotp";
    }

    // @PostMapping("/resend-otp")
    // public String resendOtp() {
    //     sendOTP(email, HttpSession session1);
    // }
    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("first") String f,
    @RequestParam("second") String s, @RequestParam("third") String t,
    @RequestParam("four") String fo, HttpSession session, Model model) {
        System.out.println("=================== verify OTP ===================");
        String rotp = f+s+t+fo;
        System.out.println("Received OPT: "+rotp);
        session.removeAttribute("message");
        if(this.otp == Integer.parseInt(rotp)) {
            model.addAttribute("title", "New Password");
            return "setnewPassword";
        } else {
            System.out.println("Invalid OTP");
            session.setAttribute("message", new Message("Invalid OTP","danger"));
            model.addAttribute("titel","Enter OTP");
            return "verifyotp";
        }

    }

    //Set new password handler
    @PostMapping("/set-password")
    public String setNewPassword(@RequestParam("npassword") String npassword,
    @RequestParam("cpassword") String cpassword,HttpSession session,Model model,
    RedirectAttributes rat) {
        session.removeAttribute("message");
        if(npassword.equals(cpassword)) {
            user.setPassword(bCryptPasswordEncoder.encode(cpassword));
            userRepository.save(user);
            rat.addFlashAttribute("success", "Password changed successfully");
            session.removeAttribute("message");
            return "redirect:/signin";
        } else {
            model.addAttribute("title","Set password");
            session.setAttribute("message", new Message("Password not Matching, plese match both field","danger"));
            return "setNewPassword";
        }
    }
}
