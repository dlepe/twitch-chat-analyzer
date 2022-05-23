package com.dlepe.twitchchatanalyzer.service.impl;

import static org.mockito.Mockito.when;

import com.dlepe.twitchchatanalyzer.config.TwitchEmoteConfiguration;
import com.dlepe.twitchchatanalyzer.dto.ChatLogRecord;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
public class LogServiceImplTest {

    private static final String VALID_CHAT_LOGS = "valid_chat_logs.txt";
    private static final String TEST_CHANNEL_NAME = "mizkif";

    @Mock
    private WebClient mockWebClient;

    @Mock
    private TwitchEmoteConfiguration mockEmoteConfiguration;

    @Mock
    private RedisTemplate<Object, Object> mockRedisTemplate;


    private LogServiceImpl logService;

    @BeforeEach
    void setup() {
        logService = new LogServiceImpl(mockWebClient, mockEmoteConfiguration, mockRedisTemplate);
    }

    @Test
    @SneakyThrows
    void testGetLogDataForDateRange() {
        final int numberOfExpectedChatRecords = 16;
        final LocalDateTime startTime = LocalDateTime.of(2022, 05, 19, 0, 1, 0);
        final LocalDateTime endTime = LocalDateTime.of(2022, 05, 19, 23, 30, 50);

        final String logData = getTestChatLogs(VALID_CHAT_LOGS);
        setupWebClientMocks(logData);

        List<ChatLogRecord> chatLogRecords = logService.getRawLogDataForDateRange(TEST_CHANNEL_NAME,
            startTime,
            endTime);
        Assertions.assertEquals(numberOfExpectedChatRecords, chatLogRecords.size());
    }

    // @Test
    // @SneakyThrows
    // void testParseChatLogs() {
    // Map<String, List<String>> mockEmoteMapping = new HashMap<>();
    // mockEmoteMapping.put("humor", List.of("OMEGALUL"));
    // when(mockEmoteConfiguration.getKeywords()).thenReturn(mockEmoteMapping);

    // final LocalDateTime startTime = LocalDateTime.of(2022, 05, 19, 0, 0, 0);
    // final LocalDateTime endTime = LocalDateTime.of(2022, 05, 19, 23, 30, 50);
    // final String logData = getTestChatLogs(VALID_CHAT_LOGS);
    // setupWebClientMocks(logData);

    // List<ChatLogRecord> chatLogRecords =
    // logService.getRawLogDataForDateRange(TEST_CHANNEL_NAME, startTime,
    // endTime);
    // // ChatLogAnalysis logAnalysis = logService.parseChatLogs(TEST_CHANNEL_NAME,
    // // chatLogRecords);

    // // Assert.assertEquals(3, logAnalysis.emoteMetrics().keySet().size());

    // // Will fix in a dedicated PR for log parsing
    // // Assert.assertEquals(LocalDateTime.of(2022, 05, 19, 0, 11, 0),
    // // logAnalysis.mostPopularOccurrence());
    // }

    @SneakyThrows
    private String getTestChatLogs(final String filename) {
        final ClassLoader classLoader = getClass().getClassLoader();
        final String pathString = classLoader.getResource("chatlogs/" + filename).getPath();
        return Files.readString(Paths.get(pathString), StandardCharsets.UTF_8);
    }

    private void setupWebClientMocks(final String webClientResponse) {
        final WebClient.RequestHeadersUriSpec uriSpecMock = Mockito.mock(
            WebClient.RequestHeadersUriSpec.class);
        final WebClient.RequestHeadersSpec headersSpecMock = Mockito.mock(
            WebClient.RequestHeadersSpec.class);
        final WebClient.RequestHeadersSpec mediaTypeSpecMock = Mockito.mock(
            WebClient.RequestHeadersSpec.class);
        final WebClient.ResponseSpec responseSpecMock = Mockito.mock(WebClient.ResponseSpec.class);

        when(mockWebClient.get()).thenReturn(uriSpecMock);
        when(uriSpecMock.uri(Mockito.any(Function.class))).thenReturn(headersSpecMock);
        when(headersSpecMock.accept(MediaType.TEXT_PLAIN)).thenReturn(mediaTypeSpecMock);
        when(mediaTypeSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(String.class))
            .thenReturn(Mono.just(webClientResponse));
    }

    @Test
    void parseChatMessage() {
        final Map<String, List<String>> emoteConfigurationMap = new HashMap<>();
        emoteConfigurationMap.put("humor", List.of("OMEGALUL", "LULW"));

        when(mockEmoteConfiguration.getKeywords()).thenReturn(emoteConfigurationMap);
        final Map<String, Long> metrics = logService.parseChatMessage("OMEGALUL OMEGALUL LULW");

        Assertions.assertEquals(Long.valueOf(3L), metrics.get("humor"));
    }
}
