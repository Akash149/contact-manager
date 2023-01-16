package com.smartcontactmanager.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smartcontactmanager.dao.ContactRepository;
import com.smartcontactmanager.dao.MyOrderRepository;
import com.smartcontactmanager.dao.UserRepository;
import com.smartcontactmanager.entities.Contact;
import com.smartcontactmanager.entities.MyOrder;
import com.smartcontactmanager.entities.User;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private MyOrderRepository myOrderRepository;
    User user;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    //method for adding common data to response
    //It will execute before the handler
    @ModelAttribute
    public void addCommonData(Model model, Principal principal) {
        String username = principal.getName();
        System.out.println("UserName: "+username);

        //get user from user Repository
        user = this.userRepository.getUserByUserName(username);
        System.out.println("User: " + user);
        model.addAttribute("user", user);
    }

    @RequestMapping("/index")
    //Principal use to get unique identification of user
    public String dashboard(Model model) {
        model.addAttribute("title", user.getName());
        return "normal/home";
    }

    //open add form handler
    @GetMapping("/add-contact")
    public String openAddcontactForm(Model model) {
        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", new Contact());
        return "normal/addcontactform";
    }

    //processing add contact form
    @PostMapping("/process-contact")
    public String processContact(@ModelAttribute Contact contact,
    @RequestParam("profileImage") MultipartFile file, Principal principal,
    RedirectAttributes rat) {
        try {
            contact.setUser(user);

            //processing and uploading file.
            if(file.isEmpty()) {
                contact.setImage("default.png");
            } else {
                contact.setImage(file.getOriginalFilename()+user.getId());
                File saveFile = new ClassPathResource("static/image").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath()+
                File.separator+file.getOriginalFilename()+user.getId());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("----------- file uploading process is done ----------------");
            }

            user.getContacts().add(contact);
            this.userRepository.save(user);
            rat.addFlashAttribute("success", "Your contact is added !");
            System.out.println(user);
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
            rat.addFlashAttribute("success", "Something went wrong !");
        }
        return "redirect:/user/add-contact";
    }
     
    //show contacts handler
    @GetMapping("/show-contacts/{page}")
    public String showContacts(@PathVariable("page") Integer page, Model m) {
        m.addAttribute("title", "All contacts");
        //1. current-page  2. size of page
        Pageable pageable = PageRequest.of(page, 5);
        Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(),pageable);
        m.addAttribute("contacts", contacts);
        m.addAttribute("currentPage", page);

        m.addAttribute("totalPages", contacts.getTotalPages());
        return "normal/sowcontacts";
    }

    //get specific contact details
    @RequestMapping("/{cId}/contact")
    public String showContactDetail(@PathVariable("cId") Integer cId, Model model) {
        System.out.println(cId);
        Optional<Contact> contactOptional = this.contactRepository.findById(cId);
        Contact contact = contactOptional.get(); 
        //Match user and their contact Id
        if(user.getId() == contact.getUser().getId()) {
            model.addAttribute("title", contact.getName());
            model.addAttribute("contact", contact);
        } else {
            model.addAttribute("title", "Permission denied");
        }
        
        return "normal/contact-details";
    }

    //Delete contact Handler
    @GetMapping("/delete/{cid}")
    public String deleteContact(@PathVariable("cid") Integer cid, Model model, RedirectAttributes rat) {
        Contact contact = this.contactRepository.findById(cid).get();
         //Match user and their contact Id
        if(user.getId() == contact.getUser().getId()) {
            //Remove user from this contact to delete contact
            // contact.setUser(null);
            // this.contactRepository.delete(contact);
            user.getContacts().remove(contact);
            this.userRepository.save(user);
            rat.addFlashAttribute("success","contact deleted successfully");
        } else {
            rat.addFlashAttribute("error","Permission denied !");
        }
        return "redirect:/user/show-contacts/0";
    }

    //open Upadate form handler
    @PostMapping("/update/{cid}")
    public String openContact(@PathVariable("cid") Integer cid,Model model) { 
        Contact cont = this.contactRepository.findById(cid).get();
        model.addAttribute("contact", cont);
        return "normal/updateform";
    }

    //update-contact handler
    @PostMapping("/process-update")
    public String updateContact(@ModelAttribute Contact contact, Model model,
    @RequestParam("profileImage") MultipartFile file, RedirectAttributes rat) {
        
        System.out.println(contact);
        System.out.println("Incoming Fille: "+file.getOriginalFilename());
        try {
            //old contact details
            Contact oldContact = this.contactRepository.findById(contact.getcId()).get();
            model.addAttribute("title", contact.getName());
            //image processing
            if(!file.isEmpty()) {
                //Delete old photo
                File deleteFile = new ClassPathResource("static/image").getFile();
                File file1 = new File(deleteFile, oldContact.getImage());
                file1.delete();

                //udate new photo
                File savFile = new ClassPathResource("static/image").getFile();
                Path path = Paths.get(savFile.getAbsolutePath() + File.separator + file.getOriginalFilename()+user.getId());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                contact.setImage(file.getOriginalFilename()+user.getId());
            } else {
                contact.setImage(oldContact.getImage());
            }
            contact.setUser(user);
            this.contactRepository.save(contact);
            System.out.println("Saved Image: "+contact.getImage());

            //feedback message in session.
            rat.addFlashAttribute("success","Successfully updated");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/user/"+contact.getcId()+"/contact";
    }

    //your profile handler
    @GetMapping("/profile")
    public String yourProfile(Model model) {
        model.addAttribute("title", user.getName());
        model.addAttribute("usr", user);
        return "normal/user_dashboard";
    }

    //settings
    @GetMapping("/settings") 
    public String setting(Model model) {
        model.addAttribute("title", "settings");
        model.addAttribute("user", user);
        return "normal/settings";
    }

    //change password handler
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
    @RequestParam("newPassword") String newPassword, 
    @RequestParam("cnfPassword") String cnfPassword, 
    RedirectAttributes rat) {

        if(newPassword.equals(cnfPassword)) {
            if(this.bCryptPasswordEncoder.matches(oldPassword,user.getPassword())) {
                //change the password
                user.setPassword(this.bCryptPasswordEncoder.encode(cnfPassword));
                this.userRepository.save(user);
                rat.addFlashAttribute("success","Your password is successfully changed");
            } else {
                //error
                rat.addFlashAttribute("error","Please, Enter correct old password");
                return "redirect:/user/settings";
            }
        } else {
            rat.addFlashAttribute("error","Please, Match Your password");
            return "redirect:/user/settings";
        }
        return "redirect:/user/index";
    }

    // Create an order for payment
    @PostMapping("/create_order")
    @ResponseBody
    public String createOrder(@RequestBody Map<String, Object> data) throws Exception {
        System.out.println("Order funtion executed");
        System.out.println(data);
        int amt = Integer.parseInt(data.get("amount").toString());
        // TODO: put the Razorpay key and secret
        var client = new RazorpayClient("key", "value");

        JSONObject obj = new JSONObject();
        obj.put("amount", amt*100);
        obj.put("currency", "INR");
        obj.put("receipt", "txn_23434");

        // * creating order
        Order order = client.orders.create(obj);
        System.out.println(order);

        //save order details in database
        MyOrder myOrder = new MyOrder();
        myOrder.setAmount(order.get("amount")+"");
        myOrder.setOrderId(order.get("order_id"));
        myOrder.setPaymentId(order.get("null"));
        myOrder.setStatus("created");
        myOrder.setUser(user);
        myOrder.setReciept(order.get("receipt"));

        this.myOrderRepository.save(myOrder);

        return order.toString();
    }

    @PostMapping("/update_order")
    public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data) {
        try {
            MyOrder myOrder = this.myOrderRepository.findByOrderId(data.get("order_id").toString());
            myOrder.setPaymentId(data.get("payment_id").toString());
            myOrder.setStatus(data.get("status").toString());

            this.myOrderRepository.save(myOrder);
            return ResponseEntity.ok(Map.of("msg","updated"));
        } catch (Exception e) {
            // TODO: handle exception
            return ResponseEntity.internalServerError().body("Updated failed");
        }

    }

}

