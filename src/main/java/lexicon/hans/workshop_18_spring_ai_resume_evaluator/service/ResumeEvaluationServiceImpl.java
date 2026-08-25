package lexicon.hans.workshop_18_spring_ai_resume_evaluator.service;

import lexicon.hans.workshop_18_spring_ai_resume_evaluator.dto.ResumeEvaluationRequest;
import lexicon.hans.workshop_18_spring_ai_resume_evaluator.dto.ResumeEvaluationResponse;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

@Service
public class ResumeEvaluationServiceImpl implements ResumeEvaluationService {

    // Step 1 (learning): low-level ChatModel API
    private final OpenAiChatModel openAiChatModel;

    public ResumeEvaluationServiceImpl(OpenAiChatModel openAiChatModel) {
        this.openAiChatModel = openAiChatModel;
    }

    // Step 1 (learning): persona + prompt template, plain text output
    @Override
    public String evaluateResumeSimple(ResumeEvaluationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("ResumeEvaluationRequest cannot be null");
        }

        try {
            // Persona: tells the AI who it is and how to behave
            SystemMessage systemMessage = SystemMessage.builder()
                    .text("""
                            You are a Senior Technical Recruiter with 20 years of experience.

                            Your role:
                            - Evaluate how well a resume matches a job description
                            - Identify strengths and missing skills
                            - Give clear, actionable feedback

                            Guidelines:
                            - Be honest and specific
                            - Use bullet points or sections
                            - If unsure, say: "I'm not sure about that"
                            """)
                    .build();

            // Prompt template: placeholders filled in with the request's data
            String userInput = String.format("""
                            Evaluate this resume against the job description below.

                            Resume:
                            %s

                            Job Description:
                            %s

                            Include:
                            1. Overall match assessment
                            2. Key strengths
                            3. Missing skills or gaps
                            4. Actionable feedback
                            """,
                    request.resumeText(),
                    request.jobDescriptionText()
            );

            UserMessage userMessage = UserMessage.builder()
                    .text(userInput)
                    .build();

            // model/temperature/max-tokens come from application.yaml — no override needed here
            Prompt prompt = Prompt.builder()
                    .messages(systemMessage, userMessage)
                    .build();

            ChatResponse response = openAiChatModel.call(prompt);

            String content = response.getResult() != null
                    ? response.getResult().getOutput().getText()
                    : null;

            return (content != null && !content.isBlank())
                    ? content
                    : "Sorry, I couldn't generate a response at the moment.";

        } catch (Exception e) {
            // replace with a more specific exception if needed
            throw new RuntimeException("Failed to evaluate resume", e);
        }
    }

    // Next step: structured output using ChatClient + BeanOutputConverter
    @Override
    public ResumeEvaluationResponse evaluateResume(ResumeEvaluationRequest request) {
        throw new UnsupportedOperationException("Not implemented yet — next step");
    }
}
