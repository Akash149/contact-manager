package com.smartcontactmanager.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smartcontactmanager.dao.UserRepository;
import com.smartcontactmanager.entities.User;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Home - Contact Manager");
        return "home";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "About - Contact Manager");
        return "about";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("title", "Signup");
        model.addAttribute("user", new User());
        return "signup";
    }

    //This handler for registering user
    @PostMapping("/do_register")
    public String registerUser(@Valid @ModelAttribute("user") User user, 
    @RequestParam(value="agreement",defaultValue = "false") 
    boolean agreement, Model model, HttpSession session, BindingResult bindingResult,
    RedirectAttributes rat) {
        try {
            if(!agreement) {
                System.out.println("You have not agreed the terms and conditions");
                throw new Exception("You have not agreed the terms and conditions");
            }

            User user2 = userRepository.getUserByUserName(user.getEmail());

            if(user2 != null ) {
                rat.addFlashAttribute("error","This email id is already registered ");
                return "redirect:/signup";
            } else {

                //server side validation.
                System.out.println(user);
                if(bindingResult.hasErrors()) {
                    System.out.println("Error: "+bindingResult.toString());
                    model.addAttribute("user", user);
                    return "signup";
                }
                user.setRole("ROLE_USER");
                user.setEnabled(true);
                user.setImageUrl("default.png");
                user.setPassword(passwordEncoder.encode(user.getPassword()));
        
                this.userRepository.save(user);

                model.addAttribute("user",new User());
            // session.setAttribute("message", new Message("Successfully resgistered !", "alert-success"));
                rat.addFlashAttribute("success", "Successfully registered");
                return "redirect:/signup";
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("user", user);
           // session.setAttribute("message", new Message("Something went wrong", "alert-danger"));
            rat.addFlashAttribute("error", "Something went wrong");
            return "redirect:/signup";
        }
    }

    //handler for custom login
    @GetMapping("/signin")
    public String customLoging(Model model) {
        model.addAttribute("title", "Login");
        return "login";
    }
    
}



 