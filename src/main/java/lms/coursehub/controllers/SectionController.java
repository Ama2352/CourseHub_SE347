package lms.coursehub.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lms.coursehub.models.dtos.section.CreateSectionRequest;
import lms.coursehub.models.dtos.section.SectionResponseDto;
import lms.coursehub.services.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/section")
@Tag(name = "Section Management")
public class SectionController {

    private final SectionService sectionService;

    @PostMapping
    public ResponseEntity<SectionResponseDto> createSection(@Valid @RequestBody CreateSectionRequest request) {
        SectionResponseDto response = sectionService.createSection(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{sectionId}")
    public ResponseEntity<SectionResponseDto> getSectionById(@PathVariable UUID sectionId) {
        SectionResponseDto response = sectionService.getSectionById(sectionId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{sectionId}")
    public ResponseEntity<SectionResponseDto> updateSection(
            @PathVariable UUID sectionId,
            @Valid @RequestBody CreateSectionRequest request) {
        SectionResponseDto response = sectionService.updateSection(sectionId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{sectionId}")
    public ResponseEntity<Void> deleteSection(@PathVariable UUID sectionId) {
        sectionService.deleteSection(sectionId);
        return ResponseEntity.noContent().build();
    }
}

// Separate controller for course-section relationship
@RestController
@RequiredArgsConstructor
@RequestMapping("/course/{courseId}/sections")
@Tag(name = "Course Section Management")
class CourseSectionController {

    private final SectionService sectionService;

    @GetMapping
    public ResponseEntity<List<SectionResponseDto>> getSectionsByCourse(@PathVariable String courseId) {
        List<SectionResponseDto> responses = sectionService.getSectionsByCourse(courseId);
        return ResponseEntity.ok(responses);
    }
}