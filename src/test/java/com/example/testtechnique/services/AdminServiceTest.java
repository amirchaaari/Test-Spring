package com.example.testtechnique.services;

import com.example.testtechnique.entities.Admin;
import com.example.testtechnique.repository.AdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    private Admin testAdmin;

    @BeforeEach
    void setUp() {
        testAdmin = new Admin();
        testAdmin.setId(1L);
        testAdmin.setUsername("testadmin");
        testAdmin.setPassword("encodedPassword");
    }

    @Test
    void testRegisterAdmin_Success() {
        // Given
        String username = "newadmin";
        String password = "password123";

        when(adminRepository.existsByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);

        // When
        Admin result = adminService.registerAdmin(username, password);

        // Then
        assertNotNull(result);
        assertEquals("testadmin", result.getUsername());
        verify(adminRepository, times(1)).existsByUsername(username);
        verify(passwordEncoder, times(1)).encode(password);
        verify(adminRepository, times(1)).save(any(Admin.class));
    }

    @Test
    void testRegisterAdmin_UsernameExists() {
        // Given
        String username = "existingadmin";
        String password = "password123";

        when(adminRepository.existsByUsername(username)).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminService.registerAdmin(username, password);
        });

        assertEquals("Username already exists", exception.getMessage());
        verify(adminRepository, times(1)).existsByUsername(username);
        verify(adminRepository, never()).save(any(Admin.class));
    }

    @Test
    void testExistsByUsername_True() {
        // Given
        String username = "testadmin";
        when(adminRepository.existsByUsername(username)).thenReturn(true);

        // When
        boolean result = adminService.existsByUsername(username);

        // Then
        assertTrue(result);
        verify(adminRepository, times(1)).existsByUsername(username);
    }

    @Test
    void testExistsByUsername_False() {
        // Given
        String username = "nonexistent";
        when(adminRepository.existsByUsername(username)).thenReturn(false);

        // When
        boolean result = adminService.existsByUsername(username);

        // Then
        assertFalse(result);
        verify(adminRepository, times(1)).existsByUsername(username);
    }
}

