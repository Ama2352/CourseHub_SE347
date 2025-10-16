package lms.coursehub.services;

import lms.coursehub.helpers.exceptions.CustomException;
import lms.coursehub.helpers.mapstructs.SectionMapper;
import lms.coursehub.models.dtos.section.CreateSectionRequest;
import lms.coursehub.models.dtos.section.SectionResponseDto;
import lms.coursehub.models.dtos.section.UpdateSectionRequest;
import lms.coursehub.models.entities.Course;
import lms.coursehub.models.entities.Section;
import lms.coursehub.repositories.SectionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SectionService {

    private final SectionRepo sectionRepo;
    private final SectionMapper sectionMapper;
    private final CourseService courseService;

    @Transactional
    public SectionResponseDto createSection(CreateSectionRequest request) {
        Section section = sectionMapper.toEntity(request);
        Course course = courseService.findCourseById(request.getCourseId());
        section.setCourse(course);
        section = sectionRepo.save(section);
        return sectionMapper.toResponseDto(section);
    }

    // GET /section/{id} - Get section by ID
    @Transactional(readOnly = true)
    public SectionResponseDto getSectionById(UUID sectionId) {
        Section section = sectionRepo.findById(sectionId)
                .orElseThrow(() -> new CustomException("Section not found", HttpStatus.NOT_FOUND));
        return sectionMapper.toResponseDto(section);
    }

    // PUT /section/{id} - Update section
    @Transactional
    public SectionResponseDto updateSection(UUID sectionId, UpdateSectionRequest request) {
        Section existingSection = sectionRepo.findById(sectionId)
                .orElseThrow(() -> new CustomException("Section not found", HttpStatus.NOT_FOUND));

        // Update fields
        if (request.getPosition() != null)
            existingSection.setPosition(request.getPosition());
        if (request.getTitle() != null)
            existingSection.setTitle(request.getTitle());
        if (request.getDescription() != null)
            existingSection.setDescription(request.getDescription());

        existingSection = sectionRepo.save(existingSection);
        return sectionMapper.toResponseDto(existingSection);
    }

    // DELETE /section/{id} - Delete section
    @Transactional
    public void deleteSection(UUID sectionId) {
        Section section = sectionRepo.findById(sectionId)
                .orElseThrow(() -> new CustomException("Section not found", HttpStatus.NOT_FOUND));
        sectionRepo.delete(section);
    }

    // GET /course/{courseId}/sections - Get all sections for a course
    @Transactional(readOnly = true)
    public List<SectionResponseDto> getSectionsByCourse(String courseId) {
        // Validate course exists
        courseService.findCourseById(courseId);

        List<Section> sections = sectionRepo.findByCourseIdOrderByPosition(courseId);
        return sections.stream()
                .map(sectionMapper::toResponseDto)
                .toList();
    }
}
