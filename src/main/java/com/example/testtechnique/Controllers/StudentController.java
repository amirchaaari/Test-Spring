package com.example.testtechnique.Controllers;

import com.example.testtechnique.Controllers.dto.ApiResponse;
import com.example.testtechnique.Controllers.dto.StudentRequest;
import com.example.testtechnique.Controllers.dto.StudentResponse;
import com.example.testtechnique.entities.Level;
import com.example.testtechnique.entities.Student;
import com.example.testtechnique.services.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Students", description = "Student management APIs")
@SecurityRequirement(name = "bearerAuth")
public class StudentController {

    @Autowired
    private StudentService studentService;

    private StudentResponse toResponse(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getUsername(),
                student.getLevel(),
                student.getCreatedAt(),
                student.getUpdatedAt()
        );
    }

    @GetMapping
    @Operation(summary = "Get all students", description = "Get paginated list of all students with optional search and filter")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Students retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<Page<StudentResponse>>> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Level level) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            Page<Student> students;

            if (search != null && !search.isEmpty() && level != null) {
                students = studentService.searchAndFilterStudents(search, level, pageable);
            } else if (search != null && !search.isEmpty()) {
                students = studentService.searchStudents(search, pageable);
            } else if (level != null) {
                students = studentService.filterStudentsByLevel(level, pageable);
            } else {
                students = studentService.getAllStudents(pageable);
            }

            Page<StudentResponse> response = students.map(this::toResponse);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving students: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get student by ID", description = "Get a specific student by their ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Student found",
                    content = @Content(schema = @Schema(implementation = StudentResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Student not found",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentById(@PathVariable Long id) {
        try {
            return studentService.getStudentById(id)
                    .map(student -> ResponseEntity.ok(ApiResponse.success(toResponse(student))))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("Student not found with id: " + id)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving student: " + e.getMessage()));
        }
    }

    @PostMapping
    @Operation(summary = "Create student", description = "Create a new student")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Student created successfully",
                    content = @Content(schema = @Schema(implementation = StudentResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Username already exists",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(@Valid @RequestBody StudentRequest request) {
        try {
            Student student = new Student();
            student.setUsername(request.getUsername());
            student.setLevel(request.getLevel());

            Student created = studentService.createStudent(student);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Student created successfully", toResponse(created)));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error creating student: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update student", description = "Update an existing student")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Student updated successfully",
                    content = @Content(schema = @Schema(implementation = StudentResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Student not found",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Username already exists",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentRequest request) {
        try {
            Student student = new Student();
            student.setUsername(request.getUsername());
            student.setLevel(request.getLevel());

            Student updated = studentService.updateStudent(id, student);
            return ResponseEntity.ok(ApiResponse.success("Student updated successfully", toResponse(updated)));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(e.getMessage()));
            }
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error updating student: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete student", description = "Delete a student by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Student deleted successfully",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Student not found",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<String>> deleteStudent(@PathVariable Long id) {
        try {
            studentService.deleteStudent(id);
            return ResponseEntity.ok(ApiResponse.success("Student deleted successfully", null));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting student: " + e.getMessage()));
        }
    }

    @GetMapping("/export")
    @Operation(summary = "Export students", description = "Export all students to CSV format")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Students exported successfully",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    public ResponseEntity<String> exportStudents() {
        try {
            List<Student> students = studentService.getAllStudentsForExport();
            StringBuilder csv = new StringBuilder();
            csv.append("ID,Username,Level,Created At,Updated At\n");

            for (Student student : students) {
                csv.append(student.getId()).append(",")
                        .append(student.getUsername()).append(",")
                        .append(student.getLevel()).append(",")
                        .append(student.getCreatedAt()).append(",")
                        .append(student.getUpdatedAt()).append("\n");
            }

            return ResponseEntity.ok()
                    .header("Content-Type", "text/csv")
                    .header("Content-Disposition", "attachment; filename=students.csv")
                    .body(csv.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error exporting students: " + e.getMessage());
        }
    }

    @PostMapping("/import")
    @Operation(summary = "Import students", description = "Import students from CSV file")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Students imported successfully",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid CSV file format",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<String>> importStudents(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("File is empty"));
            }

            String content = new String(file.getBytes());
            String[] lines = content.split("\n");

            if (lines.length < 2) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Invalid CSV format"));
            }

            int imported = 0;
            int skipped = 0;

            // Skip header line
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    try {
                        String username = parts[0].trim();
                        Level level = Level.valueOf(parts[1].trim().toUpperCase());

                        // Try to create student - service will handle duplicate check
                        try {
                            Student student = new Student();
                            student.setUsername(username);
                            student.setLevel(level);
                            studentService.createStudent(student);
                            imported++;
                        } catch (RuntimeException e) {
                            if (e.getMessage().contains("already exists")) {
                                skipped++;
                            } else {
                                throw e;
                            }
                        }
                    } catch (Exception e) {
                        skipped++;
                    }
                }
            }

            return ResponseEntity.ok(ApiResponse.success(
                    String.format("Import completed: %d imported, %d skipped", imported, skipped), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error importing students: " + e.getMessage()));
        }
    }
}

