package com.example.testtechnique.services;

import com.example.testtechnique.entities.Level;
import com.example.testtechnique.entities.Student;
import com.example.testtechnique.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    public Page<Student> getAllStudents(Pageable pageable) {
        return studentRepository.findAll(pageable);
    }

    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    public Student createStudent(Student student) {
        if (studentRepository.existsByUsername(student.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        return studentRepository.save(student);
    }

    public Student updateStudent(Long id, Student studentDetails) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));

        // Check if username is being changed and if it already exists
        if (!student.getUsername().equals(studentDetails.getUsername()) &&
            studentRepository.existsByUsername(studentDetails.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        student.setUsername(studentDetails.getUsername());
        student.setLevel(studentDetails.getLevel());
        return studentRepository.save(student);
    }

    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new RuntimeException("Student not found with id: " + id);
        }
        studentRepository.deleteById(id);
    }

    public Page<Student> searchStudents(String searchTerm, Pageable pageable) {
        return studentRepository.searchStudents(searchTerm, pageable);
    }

    public Page<Student> filterStudentsByLevel(Level level, Pageable pageable) {
        return studentRepository.findByLevel(level, pageable);
    }

    public Page<Student> searchAndFilterStudents(String searchTerm, Level level, Pageable pageable) {
        return studentRepository.searchStudentsByLevel(searchTerm, level, pageable);
    }

    public List<Student> getAllStudentsForExport() {
        return studentRepository.findAllByOrderByIdAsc();
    }
}

