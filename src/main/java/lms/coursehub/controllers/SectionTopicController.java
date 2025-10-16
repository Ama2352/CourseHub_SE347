package lms.coursehub.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lms.coursehub.models.dtos.topic.TopicResponseDto;
import lms.coursehub.services.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/topic/section/{sectionId}")
@Tag(name = "Section Topic Management")
public class SectionTopicController {

    private final TopicService topicService;

    @GetMapping
    public ResponseEntity<List<TopicResponseDto>> getAllTopicsForSection(
            @PathVariable UUID sectionId) {

        List<TopicResponseDto> responses = topicService.getAllTopicsForSection(sectionId);
        return ResponseEntity.ok(responses);
    }
}
