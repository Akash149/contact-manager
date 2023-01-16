package com.smartcontactmanager.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartcontactmanager.entities.Contact;
import com.smartcontactmanager.entities.User;

public interface ContactRepository extends JpaRepository<Contact, Integer> {

    //pegination...
    @Query("from Contact as c where c.user.id =:userId")
    //contain two information 1. current-page 2. contact per page
    public Page<Contact> findContactByUser(@Param("userId") int userId, Pageable pageable);
    //search contact method
    public List<Contact> findByNameContainingAndUser(String name,User user);
    
}
