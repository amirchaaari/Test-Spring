package com.example.testtechnique.repository;

import com.example.testtechnique.entities.Level;
import com.example.testtechnique.entities.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * Find student by username
     */
    Optional<Student> findByUsername(String username);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Find students by level
     */
    Page<Student> findByLevel(Level level, Pageable pageable);

    /**
     * Search students by username or ID
     */
    @Query("SELECT s FROM Student s WHERE " +
            "LOWER(s.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "CAST(s.id AS string) LIKE CONCAT('%', :searchTerm, '%')")
    Page<Student> searchStudents(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Search students by username or ID with level filter
     */
    @Query("SELECT s FROM Student s WHERE " +
            "(LOWER(s.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "CAST(s.id AS string) LIKE CONCAT('%', :searchTerm, '%')) AND " +
            "s.level = :level")
    Page<Student> searchStudentsByLevel(
            @Param("searchTerm") String searchTerm,
            @Param("level") Level level,
            Pageable pageable
    );

    /**
     * Get all students for export (without pagination)
     */
    List<Student> findAllByOrderByIdAsc();
}