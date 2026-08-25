package lexicon.hans.workshop_18_spring_ai_resume_evaluator.service;

import lexicon.hans.workshop_18_spring_ai_resume_evaluator.dto.ResumeEvaluationRequest;
import lexicon.hans.workshop_18_spring_ai_resume_evaluator.dto.ResumeEvaluationResponse;

public interface ResumeEvaluationService {

    // Step 1 (learning): simple prompt, plain text — persona + basic prompt
    String evaluateResumeSimple(ResumeEvaluationRequest request);

    // Final: structured output using the persona + prompt template
    ResumeEvaluationResponse evaluateResume(ResumeEvaluationRequest request);
}
