package dev.farhan.codereview.controller;

import dev.farhan.codereview.dto.CreatePatternRequest;
import dev.farhan.codereview.model.ReviewPattern;
import dev.farhan.codereview.service.ReviewPatternService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/patterns")
public class ReviewPatternController {

    private final ReviewPatternService patternService;

    public ReviewPatternController(ReviewPatternService patternService) {
        this.patternService = patternService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewPattern createPattern(@RequestBody CreatePatternRequest request) {
        return patternService.createPattern(request);
    }

    @GetMapping
    public List<ReviewPattern> listPatterns(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String category) {
        return patternService.listPatterns(language, category);
    }

    @GetMapping("/{id}")
    public ReviewPattern getPattern(@PathVariable String id) {
        return patternService.getPattern(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
