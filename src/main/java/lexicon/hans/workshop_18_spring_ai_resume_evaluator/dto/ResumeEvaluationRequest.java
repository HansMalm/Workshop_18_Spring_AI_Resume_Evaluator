package lexicon.hans.workshop_18_spring_ai_resume_evaluator.dto;

public record ResumeEvaluationRequest(
        String resumeText,
        String jobDescriptionText
) {
}
