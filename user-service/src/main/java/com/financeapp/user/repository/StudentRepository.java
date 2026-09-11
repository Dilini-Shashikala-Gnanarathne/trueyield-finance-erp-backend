package com.financeapp.user.repository;

import com.financeapp.user.domain.entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<StudentEntity, String>, JpaSpecificationExecutor<StudentEntity> {

    Optional<StudentEntity> findByAcademicId(String academicId);

    boolean existsByAcademicId(String academicId);

    @Modifying
    @Query("""
        UPDATE StudentEntity s SET
            s.fname = :fname, s.lname = :lname, s.gender = :gender,
            s.whatsappNumber = :whatsapp, s.school = :school,
            s.guardianName = :guardianName, s.guardianMobile = :guardianMobile,
            s.updatedAt = :now, s.updatedBy = :updatedBy
        WHERE s.userId = :userId
    """)
    int updateStudentDetails(@Param("userId") String userId,
                              @Param("fname") String fname,
                              @Param("lname") String lname,
                              @Param("gender") com.financeapp.user.domain.enums.Gender gender,
                              @Param("whatsapp") String whatsapp,
                              @Param("school") String school,
                              @Param("guardianName") String guardianName,
                              @Param("guardianMobile") String guardianMobile,
                              @Param("now") Instant now,
                              @Param("updatedBy") String updatedBy);
}
