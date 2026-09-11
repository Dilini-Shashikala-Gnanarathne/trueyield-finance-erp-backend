package com.financeapp.user.controller;

import com.financeapp.user.dto.request.DeleteUserRequest;
import com.financeapp.user.dto.request.UpdateIdentifiersRequest;
import com.financeapp.user.dto.request.UpdateStatusRequest;
import com.financeapp.user.dto.request.UpdateStudentRequest;
import com.financeapp.user.dto.response.ApiResponse;
import com.financeapp.user.dto.response.PagedResponse;
import com.financeapp.user.dto.response.StudentResponse;
import com.financeapp.user.repository.spec.StudentSpecification;
import com.financeapp.user.security.SecurityPrincipal;
import com.financeapp.user.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Student Management", description = "Student profiles, dynamic search, identifiers, and lifecycle management")
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SUB_ADMIN') or #id == principal.userId")
    @Operation(summary = "Get student profile by User ID")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentById(@PathVariable String id) {
        StudentResponse response = studentService.getStudentById(id);
        return ResponseEntity.ok(ApiResponse.ok("Student profile retrieved successfully.", response));
    }

    @GetMapping("/academic/{academicId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SUB_ADMIN')")
    @Operation(summary = "Get student profile by Academic ID")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentByAcademicId(@PathVariable String academicId) {
        StudentResponse response = studentService.getStudentByAcademicId(academicId);
        return ResponseEntity.ok(ApiResponse.ok("Student profile retrieved successfully.", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SUB_ADMIN')")
    @Operation(summary = "Dynamically filter and paginate students")
    public ResponseEntity<ApiResponse<PagedResponse<StudentResponse>>> searchStudents(
            @RequestParam(required = false) String academicId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String nic,
            @RequestParam(required = false) String whatsappNumber,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String batchId,
            @RequestParam(required = false) String school,
            @RequestParam(required = false) String guardianName,
            @RequestParam(required = false) String guardianMobile,
            @RequestParam(required = false) Instant registeredAtFrom,
            @RequestParam(required = false) Instant registeredAtTo,
            @PageableDefault(size = 20, sort = "fname", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        StudentSpecification.FilterParams params = new StudentSpecification.FilterParams(
                academicId, name, mobile, email, nic, whatsappNumber, username,
                gender, status, batchId, school, guardianName, guardianMobile,
                registeredAtFrom, registeredAtTo
        );

        PagedResponse<StudentResponse> response = studentService.searchStudents(params, pageable);
        return ResponseEntity.ok(ApiResponse.ok("Students retrieved successfully.", response));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SUB_ADMIN') or #request.id() == principal.userId")
    @Operation(summary = "Update student profile details")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(
            @Valid @RequestBody UpdateStudentRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        StudentResponse response = studentService.updateStudent(request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Student profile updated successfully.", response));
    }

    @PutMapping("/identifiers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN') or #request.id() == principal.userId")
    @Operation(summary = "Update sensitive identifiers (mobile, NIC)")
    public ResponseEntity<ApiResponse<Void>> updateIdentifiers(
            @Valid @RequestBody UpdateIdentifiersRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        studentService.updateIdentifiers(request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Identifiers updated successfully."));
    }

    @PutMapping("/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SUB_ADMIN')")
    @Operation(summary = "Update student account status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @Valid @RequestBody UpdateStatusRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        studentService.updateStatus(request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Status updated successfully."));
    }

    @DeleteMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or #request.userId() == principal.userId")
    @Operation(summary = "Soft delete student account")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(
            @Valid @RequestBody DeleteUserRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        studentService.deleteStudent(request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Student account deleted successfully."));
    }
}
