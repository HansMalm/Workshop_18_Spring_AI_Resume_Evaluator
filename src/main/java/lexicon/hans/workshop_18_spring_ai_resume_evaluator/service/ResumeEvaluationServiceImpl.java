package lexicon.hans.workshop_18_spring_ai_resume_evaluator.service;

import lexicon.hans.workshop_18_spring_ai_resume_evaluator.dto.ResumeEvaluationRequest;
import lexicon.hans.workshop_18_spring_ai_resume_evaluator.dto.ResumeEvaluationResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ResumeEvaluationServiceImpl implements ResumeEvaluationService {

    // Step 1 (learning): low-level ChatModel API
    private final OpenAiChatModel openAiChatModel;

    // Step 2: ChatClient, needed for the structured output method below
    private final ChatClient chatClient;

    // Prompt template: loaded from resources/prompts, used by both methods below
    private final PromptTemplate promptTemplate;

    public ResumeEvaluationServiceImpl(
            OpenAiChatModel openAiChatModel,
            ChatClient.Builder builder,
            @Value("classpath:/prompts/resume-evaluation-prompt.st") Resource promptResource
    ) {
        this.openAiChatModel = openAiChatModel;
        this.chatClient = builder.build();
        this.promptTemplate = new PromptTemplate(promptResource);
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

            // Prompt template: {resumeText} and {jobDescriptionText} filled in from the request
            Message userMessage = promptTemplate.createMessage(Map.of(
                    "resumeText", request.resumeText(),
                    "jobDescriptionText", request.jobDescriptionText()
            ));

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

    // Step 2: structured output, using ChatClient + BeanOutputConverter
    @Override
    public ResumeEvaluationResponse evaluateResume(ResumeEvaluationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("ResumeEvaluationRequest cannot be null");
        }

        try {
            // Converter tied to our response DTO — it can describe the JSON shape the AI must return,
            // and later parse the AI's JSON text back into that DTO
            BeanOutputConverter<ResumeEvaluationResponse> converter =
                    new BeanOutputConverter<>(ResumeEvaluationResponse.class);

            String format = converter.getFormat();

            // Prompt template: {resumeText} and {jobDescriptionText} filled in from the request
            String userText = promptTemplate.render(Map.of(
                    "resumeText", request.resumeText(),
                    "jobDescriptionText", request.jobDescriptionText()
            ));

            ChatResponse response = chatClient.prompt()
                    .system("""
                            You are a Senior Technical Recruiter with 20 years of experience.

                            Your role:
                            - Evaluate how well a resume matches a job description
                            - Identify strengths and missing skills
                            - Give clear, actionable feedback

                            Guidelines:
                            - Be honest and specific
                            - If unsure, say: "I'm not sure about that"

                            Format the output as a JSON object that matches this schema:
                            """ + format)
                    .user(userText)
                    .call()
                    .chatResponse();

            String content = response != null && response.getResult() != null
                    ? response.getResult().getOutput().getText()
                    : null;

            if (content == null || content.isBlank()) {
                throw new RuntimeException("AI response is empty or null");
            }

            return converter.convert(content);

        } catch (Exception e) {
            throw new RuntimeException("Failed to evaluate resume", e);
        }
    }
}
