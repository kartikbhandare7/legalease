package com.legalease.ai;

import com.legalease.ai.dto.AIParseRequest;
import com.legalease.ai.service.AIParserService;
import com.legalease.common.enums.AIParseType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AIParserService Tests")
class AIParserServiceTest {

    // ChatClient has a fluent builder chain — needs careful mocking
    @Mock private ChatClient chatClient;
    @Mock private ChatClient.ChatClientRequestSpec requestSpec;
    @Mock private ChatClient.CallResponseSpec callResponseSpec;

    @InjectMocks private AIParserService aiParserService;

    private void mockChatChain(String returnValue) {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(returnValue);
    }

    @Test
    @DisplayName("Should parse intake note and return structured JSON")
    void shouldParseIntakeSuccessfully() {
        String mockJson = """
            {"clientName":"John Doe","opposingParty":"Ravi Kumar",
             "caseBackground":"Theft case 2022","caseType":"CRIMINAL",
             "courtName":null,"phone":null,"email":null}
            """;

        mockChatChain(mockJson);

        var request = new AIParseRequest();
        request.setRawText("John Doe theft case opposing Ravi Kumar 2022");
        request.setParseType(AIParseType.INTAKE);

        var response = aiParserService.parse(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getParsedFields()).contains("John Doe");
        assertThat(response.getParsedFields()).contains("CRIMINAL");
        assertThat(response.getParseType()).isEqualTo("INTAKE");
        assertThat(response.getProcessingTimeMs()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("Should parse hearing note and return structured JSON")
    void shouldParseHearingSuccessfully() {
        String mockJson = """
            {"hearingDate":"2024-08-01","nextDate":"2024-09-05",
             "outcome":"Judge asked for evidence",
             "actionItems":["Submit FIR copy","Notify client"]}
            """;

        mockChatChain(mockJson);

        var request = new AIParseRequest();
        request.setRawText("Sharma case today judge asked evidence next date sep 5");
        request.setParseType(AIParseType.HEARING);

        var response = aiParserService.parse(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getParsedFields()).contains("2024-09-05");
        assertThat(response.getParsedFields()).contains("Submit FIR copy");
        assertThat(response.getParseType()).isEqualTo("HEARING");
    }

    @Test
    @DisplayName("Should strip markdown code fences from LLM response")
    void shouldStripMarkdownFences() {
        String withFences = "```json\n{\"clientName\":\"John\"}\n```";
        mockChatChain(withFences);

        var request = new AIParseRequest();
        request.setRawText("John intake note");
        request.setParseType(AIParseType.INTAKE);

        var response = aiParserService.parse(request);

        assertThat(response.getParsedFields()).doesNotContain("```");
        assertThat(response.getParsedFields()).contains("John");
    }

    @Test
    @DisplayName("Should return success false and empty JSON when LLM throws")
    void shouldReturnFallbackOnLLMFailure() {
        when(chatClient.prompt()).thenThrow(
                new RuntimeException("LLM service unavailable"));

        var request = new AIParseRequest();
        request.setRawText("some note");
        request.setParseType(AIParseType.INTAKE);

        var response = aiParserService.parse(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getParsedFields()).isEqualTo("{}");
        assertThat(response.getErrorMessage())
                .contains("AI parsing failed");
    }
}