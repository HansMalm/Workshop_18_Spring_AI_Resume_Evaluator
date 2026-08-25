package lexicon.hans.workshop_18_spring_ai_resume_evaluator.controller;

import jakarta.validation.Valid;
import lexicon.hans.workshop_18_spring_ai_resume_evaluator.dto.ResumeEvaluationRequest;
import lexicon.hans.workshop_18_spring_ai_resume_evaluator.dto.ResumeEvaluationResponse;
import lexicon.hans.workshop_18_spring_ai_resume_evaluator.service.ResumeEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/resume")
@RequiredArgsConstructor
public class ResumeEvaluationController {

    private final ResumeEvaluationService resumeEvaluationService;

    // Step 1 (learning): plain text response
    @PostMapping("/evaluate/simple")
    public String evaluateResumeSimple(@RequestBody @Valid ResumeEvaluationRequest request) {
        return resumeEvaluationService.evaluateResumeSimple(request);
    }

    // Step 2: structured response
    @PostMapping("/evaluate")
    public ResumeEvaluationResponse evaluateResume(@RequestBody @Valid ResumeEvaluationRequest request) {
        return resumeEvaluationService.evaluateResume(request);
    }
}
