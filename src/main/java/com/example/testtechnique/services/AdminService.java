package com.example.testtechnique.services;

import com.example.testtechnique.entities.Admin;
import com.example.testtechnique.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Admin registerAdmin(String username, String password) {
        if (adminRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        return adminRepository.save(admin);
    }

    public boolean existsByUsername(String username) {
        return adminRepository.existsByUsername(username);
    }
}

