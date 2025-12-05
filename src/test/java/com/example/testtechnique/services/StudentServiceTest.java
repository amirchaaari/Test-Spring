package com.example.testtechnique.services;

import com.example.testtechnique.entities.Level;
import com.example.testtechnique.entities.Student;
import com.example.testtechnique.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    private Student testStudent;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setUsername("teststudent");
        testStudent.setLevel(Level.BEGINNER);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void testGetAllStudents_Success() {
        // Given
        List<Student> students = Arrays.asList(testStudent);
        Page<Student> studentPage = new PageImpl<>(students, pageable, 1);

        when(studentRepository.findAll(pageable)).thenReturn(studentPage);

        // When
        Page<Student> result = studentService.getAllStudents(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("teststudent", result.getContent().get(0).getUsername());
        verify(studentRepository, times(1)).findAll(pageable);
    }

    @Test
    void testGetStudentById_Success() {
        // Given
        Long id = 1L;
        when(studentRepository.findById(id)).thenReturn(Optional.of(testStudent));

        // When
        Optional<Student> result = studentService.getStudentById(id);

        // Then
        assertTrue(result.isPresent());
        assertEquals("teststudent", result.get().getUsername());
        verify(studentRepository, times(1)).findById(id);
    }

    @Test
    void testGetStudentById_NotFound() {
        // Given
        Long id = 999L;
        when(studentRepository.findById(id)).thenReturn(Optional.empty());

        // When
        Optional<Student> result = studentService.getStudentById(id);

        // Then
        assertFalse(result.isPresent());
        verify(studentRepository, times(1)).findById(id);
    }

    @Test
    void testCreateStudent_Success() {
        // Given
        Student newStudent = new Student();
        newStudent.setUsername("newstudent");
        newStudent.setLevel(Level.INTERMEDIATE);

        when(studentRepository.existsByUsername("newstudent")).thenReturn(false);
        when(studentRepository.save(newStudent)).thenReturn(newStudent);

        // When
        Student result = studentService.createStudent(newStudent);

        // Then
        assertNotNull(result);
        assertEquals("newstudent", result.getUsername());
        verify(studentRepository, times(1)).existsByUsername("newstudent");
        verify(studentRepository, times(1)).save(newStudent);
    }

    @Test
    void testCreateStudent_UsernameExists() {
        // Given
        Student newStudent = new Student();
        newStudent.setUsername("existingstudent");
        newStudent.setLevel(Level.INTERMEDIATE);

        when(studentRepository.existsByUsername("existingstudent")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            studentService.createStudent(newStudent);
        });

        assertEquals("Username already exists", exception.getMessage());
        verify(studentRepository, times(1)).existsByUsername("existingstudent");
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void testUpdateStudent_Success() {
        // Given
        Long id = 1L;
        Student updatedStudent = new Student();
        updatedStudent.setUsername("updatedstudent");
        updatedStudent.setLevel(Level.ADVANCED);

        when(studentRepository.findById(id)).thenReturn(Optional.of(testStudent));
        when(studentRepository.existsByUsername("updatedstudent")).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenReturn(updatedStudent);

        // When
        Student result = studentService.updateStudent(id, updatedStudent);

        // Then
        assertNotNull(result);
        assertEquals("updatedstudent", result.getUsername());
        assertEquals(Level.ADVANCED, result.getLevel());
        verify(studentRepository, times(1)).findById(id);
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void testUpdateStudent_NotFound() {
        // Given
        Long id = 999L;
        Student updatedStudent = new Student();
        updatedStudent.setUsername("updatedstudent");
        updatedStudent.setLevel(Level.ADVANCED);

        when(studentRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            studentService.updateStudent(id, updatedStudent);
        });

        assertEquals("Student not found with id: 999", exception.getMessage());
        verify(studentRepository, times(1)).findById(id);
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void testDeleteStudent_Success() {
        // Given
        Long id = 1L;
        when(studentRepository.existsById(id)).thenReturn(true);
        doNothing().when(studentRepository).deleteById(id);

        // When
        studentService.deleteStudent(id);

        // Then
        verify(studentRepository, times(1)).existsById(id);
        verify(studentRepository, times(1)).deleteById(id);
    }

    @Test
    void testDeleteStudent_NotFound() {
        // Given
        Long id = 999L;
        when(studentRepository.existsById(id)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            studentService.deleteStudent(id);
        });

        assertEquals("Student not found with id: 999", exception.getMessage());
        verify(studentRepository, times(1)).existsById(id);
        verify(studentRepository, never()).deleteById(any());
    }

    @Test
    void testSearchStudents_Success() {
        // Given
        String searchTerm = "test";
        List<Student> students = Arrays.asList(testStudent);
        Page<Student> studentPage = new PageImpl<>(students, pageable, 1);

        when(studentRepository.searchStudents(searchTerm, pageable)).thenReturn(studentPage);

        // When
        Page<Student> result = studentService.searchStudents(searchTerm, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(studentRepository, times(1)).searchStudents(searchTerm, pageable);
    }

    @Test
    void testFilterStudentsByLevel_Success() {
        // Given
        Level level = Level.BEGINNER;
        List<Student> students = Arrays.asList(testStudent);
        Page<Student> studentPage = new PageImpl<>(students, pageable, 1);

        when(studentRepository.findByLevel(level, pageable)).thenReturn(studentPage);

        // When
        Page<Student> result = studentService.filterStudentsByLevel(level, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(studentRepository, times(1)).findByLevel(level, pageable);
    }

    @Test
    void testGetAllStudentsForExport_Success() {
        // Given
        List<Student> students = Arrays.asList(testStudent);
        when(studentRepository.findAllByOrderByIdAsc()).thenReturn(students);

        // When
        List<Student> result = studentService.getAllStudentsForExport();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("teststudent", result.get(0).getUsername());
        verify(studentRepository, times(1)).findAllByOrderByIdAsc();
    }
}

