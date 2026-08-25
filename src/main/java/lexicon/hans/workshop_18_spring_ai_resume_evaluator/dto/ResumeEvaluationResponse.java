package lexicon.hans.workshop_18_spring_ai_resume_evaluator.dto;

import java.util.List;

public record ResumeEvaluationResponse(
        int matchScore,
        List<String> strengths,
        List<String> missingSkills,
        String feedback
) {
}
