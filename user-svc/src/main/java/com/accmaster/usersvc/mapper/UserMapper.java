package com.accmaster.usersvc.mapper;

import com.accmaster.usersvc.domain.entity.*;
import com.accmaster.usersvc.dto.response.AdminResponse;
import com.accmaster.usersvc.dto.response.StudentResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public StudentResponse toStudentResponse(StudentEntity student) {
        if (student == null) {
            return null;
        }

        UserEntity user = student.getUser();
        BatchEntity batch = student.getBatch();
        ProfileEntity profile = user != null ? user.getProfile() : null;
        AddressEntity address = user != null ? user.getAddress() : null;
        CityEntity city = address != null ? address.getCity() : null;
        DistrictEntity district = city != null ? city.getDistrict() : null;

        return new StudentResponse(
                user != null ? user.getId() : student.getUserId(),
                student.getAcademicId(),
                student.getFname(),
                student.getLname(),
                user != null ? user.getEmail() : null,
                user != null ? user.getMobile() : null,
                user != null ? user.getNic() : null,
                student.getGender() != null ? student.getGender().name() : null,
                student.getWhatsappNumber(),
                student.getSchool(),
                student.getGuardianName(),
                student.getGuardianMobile(),
                batch != null ? batch.getId() : null,
                batch != null ? batch.getName() : null,
                user != null && user.getStatus() != null ? user.getStatus().name() : null,
                profile != null ? profile.getUrl() : null,
                user != null ? user.getRegisteredAt() : null,
                user != null ? user.getUpdatedAt() : null,
                user != null ? user.getLastPasswordResetAt() : null,
                address != null ? address.getLine1() : null,
                address != null ? address.getLine2() : null,
                city != null ? city.getId() : null,
                city != null ? city.getName() : null,
                district != null ? district.getId() : null,
                district != null ? district.getName() : null,
                city != null ? city.getZipcode() : null
        );
    }

    public AdminResponse toAdminResponse(AdminEntity admin) {
        if (admin == null) {
            return null;
        }

        UserEntity user = admin.getUser();
        ProfileEntity profile = user != null ? user.getProfile() : null;

        return new AdminResponse(
                user != null ? user.getId() : admin.getUserId(),
                user != null ? user.getUsername() : null,
                user != null ? user.getMobile() : null,
                user != null ? user.getEmail() : null,
                user != null ? user.getNic() : null,
                admin.getFname(),
                admin.getLname(),
                admin.getRole() != null ? admin.getRole().name() : null,
                user != null && user.getStatus() != null ? user.getStatus().name() : null,
                profile != null ? profile.getUrl() : null,
                user != null ? user.getRegisteredAt() : null,
                user != null ? user.getUpdatedAt() : null,
                user != null ? user.getLastPasswordResetAt() : null
        );
    }
}
