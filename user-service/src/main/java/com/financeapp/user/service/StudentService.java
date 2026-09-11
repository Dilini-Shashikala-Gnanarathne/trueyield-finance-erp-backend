package com.financeapp.user.service;

import com.financeapp.user.domain.entity.*;
import com.financeapp.user.domain.enums.Gender;
import com.financeapp.user.domain.enums.UserStatus;
import com.financeapp.user.dto.request.DeleteUserRequest;
import com.financeapp.user.dto.request.UpdateIdentifiersRequest;
import com.financeapp.user.dto.request.UpdateStatusRequest;
import com.financeapp.user.dto.request.UpdateStudentRequest;
import com.financeapp.user.dto.response.PagedResponse;
import com.financeapp.user.dto.response.StudentResponse;
import com.financeapp.user.exception.BusinessException;
import com.financeapp.user.exception.DuplicateIdentifierException;
import com.financeapp.user.exception.InvalidCredentialsException;
import com.financeapp.user.exception.UserNotFoundException;
import com.financeapp.user.mapper.UserMapper;
import com.financeapp.user.repository.*;
import com.financeapp.user.repository.spec.StudentSpecification;
import com.financeapp.user.security.SecurityPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Service managing Student lifecycle, profiles, dynamic query searches,
 * addresses, and status transitions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final BatchRepository batchRepository;
    private final AddressRepository addressRepository;
    private final CityRepository cityRepository;
    private final ProfileRepository profileRepository;
    private final PasswordService passwordService;
    private final UserMapper userMapper;

    /**
     * Finds a student by User ID.
     */
    @Transactional(readOnly = true)
    public StudentResponse getStudentById(String userId) {
        StudentEntity student = studentRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Student not found for ID: " + userId));
        return userMapper.toStudentResponse(student);
    }

    /**
     * Finds a student by unique Academic ID (e.g. S01234).
     */
    @Transactional(readOnly = true)
    public StudentResponse getStudentByAcademicId(String academicId) {
        StudentEntity student = studentRepository.findByAcademicId(academicId)
                .orElseThrow(() -> new UserNotFoundException("Student not found for academic ID: " + academicId));
        return userMapper.toStudentResponse(student);
    }

    /**
     * Dynamically searches and filters students using Spring Data JPA Specification.
     */
    @Transactional(readOnly = true)
    public PagedResponse<StudentResponse> searchStudents(StudentSpecification.FilterParams params, Pageable pageable) {
        Page<StudentEntity> page = studentRepository.findAll(StudentSpecification.withFilters(params), pageable);
        return PagedResponse.from(page.map(userMapper::toStudentResponse));
    }

    /**
     * Updates student profile details, addresses, and batch information.
     */
    @Transactional
    public StudentResponse updateStudent(UpdateStudentRequest request, SecurityPrincipal principal) {
        String userId = request.id();
        log.info("Updating student details for user ID: {} by updater: {}", userId, principal.getUserId());

        StudentEntity student = studentRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Student not found for ID: " + userId));

        UserEntity user = student.getUser();

        // Validate NIC uniqueness if changed
        if (request.nic() != null && !request.nic().isBlank()) {
            if (userRepository.existsByNicAndIdNot(request.nic(), userId)) {
                throw new DuplicateIdentifierException("A user with this NIC already exists.");
            }
            user.setNic(request.nic());
        }

        // Update Student fields
        student.setFname(request.firstName());
        student.setLname(request.lastName());
        if (request.gender() != null && !request.gender().isBlank()) {
            student.setGender(Gender.valueOf(request.gender().toUpperCase()));
        }
        student.setWhatsappNumber(request.whatsappNumber());
        student.setSchool(request.school());
        student.setGuardianName(request.guardianName());
        student.setGuardianMobile(request.guardianMobile());
        student.setUpdatedAt(Instant.now());
        student.setUpdatedBy(principal.getUserId());

        // Update Batch if provided
        if (request.batchId() != null && !request.batchId().isBlank()) {
            BatchEntity batch = batchRepository.findById(request.batchId())
                    .orElseThrow(() -> new BusinessException("Batch not found for ID: " + request.batchId()));
            student.setBatch(batch);
        }

        // Update Profile Image URL if provided
        if (request.profileUrl() != null && !request.profileUrl().isBlank()) {
            ProfileEntity profile = user.getProfile();
            if (profile == null) {
                profile = new ProfileEntity();
                profile.setId(UUID.randomUUID().toString());
                profile.setUser(user);
                user.setProfile(profile);
            }
            profile.setUrl(request.profileUrl());
            profile.setUpdatedAt(Instant.now());
            profile.setUpdatedBy(principal.getUserId());
            profileRepository.save(profile);
        }

        // Update Address if provided
        if (request.addressLine1() != null || request.cityId() != null) {
            AddressEntity address = user.getAddress();
            if (address == null) {
                address = new AddressEntity();
                address.setUser(user);
                address.setCreatedBy(principal.getUserId());
                address.setCreatedAt(Instant.now());
                user.setAddress(address);
            }
            if (request.addressLine1() != null) {
                address.setLine1(request.addressLine1());
            }
            if (request.addressLine2() != null) {
                address.setLine2(request.addressLine2());
            }
            if (request.cityId() != null) {
                CityEntity city = cityRepository.findById(request.cityId())
                        .orElseThrow(() -> new BusinessException("City not found for ID: " + request.cityId()));
                address.setCity(city);
            }
            address.setUpdatedAt(Instant.now());
            address.setUpdatedBy(principal.getUserId());
            addressRepository.save(address);
        }

        user.setUpdatedAt(Instant.now());
        user.setUpdatedBy(principal.getUserId());
        userRepository.save(user);

        StudentEntity saved = studentRepository.save(student);
        return userMapper.toStudentResponse(saved);
    }

    /**
     * Updates sensitive identifiers (mobile or NIC) with uniqueness checks.
     */
    @Transactional
    public void updateIdentifiers(UpdateIdentifiersRequest request, SecurityPrincipal principal) {
        String userId = request.id();
        log.info("Updating identifiers for user ID: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found for ID: " + userId));

        if (request.mobile() != null && !request.mobile().isBlank()) {
            if (userRepository.existsByMobileAndIdNot(request.mobile(), userId)) {
                throw new DuplicateIdentifierException("A user with this mobile number already exists.");
            }
            user.setMobile(request.mobile());
            if ("MOBILE".equalsIgnoreCase(request.usernameType()) || user.getUsername().equals(user.getMobile())) {
                user.setUsername(request.mobile());
            }
        }

        if (request.nic() != null && !request.nic().isBlank()) {
            if (userRepository.existsByNicAndIdNot(request.nic(), userId)) {
                throw new DuplicateIdentifierException("A user with this NIC already exists.");
            }
            user.setNic(request.nic());
            if ("NIC".equalsIgnoreCase(request.usernameType())) {
                user.setUsername(request.nic());
            }
        }

        user.setUpdatedAt(Instant.now());
        user.setUpdatedBy(principal.getUserId());
        userRepository.save(user);
    }

    /**
     * Updates the status of a student account (ACTIVE, INACTIVE, SUSPENDED, DELETED).
     */
    @Transactional
    public void updateStatus(UpdateStatusRequest request, SecurityPrincipal principal) {
        String userId = request.userId();
        log.info("Updating status for user ID: {} to {}", userId, request.status());

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found for ID: " + userId));

        UserStatus newStatus = UserStatus.valueOf(request.status().toUpperCase());
        user.setStatus(newStatus);
        user.setUpdatedAt(Instant.now());
        user.setUpdatedBy(principal.getUserId());
        userRepository.save(user);
    }

    /**
     * Soft-deletes a student account.
     */
    @Transactional
    public void deleteStudent(DeleteUserRequest request, SecurityPrincipal principal) {
        String userId = request.userId();
        log.info("Soft deleting student ID: {} requested by {}", userId, principal.getUserId());

        UserEntity targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found for ID: " + userId));

        // Verify requesting user's password for safety
        UserEntity requestingUser = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new UserNotFoundException("Requesting user not found: " + principal.getUserId()));

        if (!passwordService.matches(request.password(), requestingUser.getPassword())) {
            throw new InvalidCredentialsException("Invalid password confirmation.");
        }

        targetUser.setDeletedBy(principal.getUserId());
        targetUser.setStatus(UserStatus.DELETED);
        userRepository.delete(targetUser); // Trigger @SQLDelete
    }
}
