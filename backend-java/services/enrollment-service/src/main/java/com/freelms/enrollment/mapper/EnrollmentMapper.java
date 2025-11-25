package com.freelms.enrollment.mapper;

import com.freelms.enrollment.dto.CertificateDto;
import com.freelms.enrollment.dto.EnrollmentDto;
import com.freelms.enrollment.entity.Certificate;
import com.freelms.enrollment.entity.Enrollment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EnrollmentMapper {

    @Mapping(target = "courseTitle", ignore = true)
    @Mapping(target = "courseThumbnail", ignore = true)
    @Mapping(target = "totalLessons", ignore = true)
    @Mapping(target = "completedLessons", expression = "java(countCompletedLessons(enrollment))")
    @Mapping(target = "hasCertificate", expression = "java(enrollment.getCertificate() != null)")
    EnrollmentDto toDto(Enrollment enrollment);

    List<EnrollmentDto> toDtoList(List<Enrollment> enrollments);

    CertificateDto toCertificateDto(Certificate certificate);

    List<CertificateDto> toCertificateDtoList(List<Certificate> certificates);

    default int countCompletedLessons(Enrollment enrollment) {
        if (enrollment.getLessonProgresses() == null) return 0;
        return (int) enrollment.getLessonProgresses().stream()
                .filter(lp -> lp.isCompleted())
                .count();
    }
}
