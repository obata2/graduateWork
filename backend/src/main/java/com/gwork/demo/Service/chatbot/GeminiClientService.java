package com.gwork.demo.Service.chatbot;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GeminiClientService{
  private final WebClient webClient;

  public GeminiClientService(@Value("${GEMINI_API_KEY}") String apiKey) {
    this.webClient = WebClient.builder()
      .baseUrl("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent")
      .defaultHeader("Content-Type", "application/json")
      .defaultHeader("x-goog-api-key", apiKey)
      .build();
  }

  // promptをgeminiに渡し、レスポンスから回答内容の部分を取り出す
  public String chat(String prompt) {
    Map<String, Object> body = Map.of(
      "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
    );
    return webClient.post()
      .bodyValue(body)
      .retrieve()
      .bodyToMono(Map.class)
      .map(res -> ((Map<String, Object>) ((List<?>) res.get("candidates")).get(0)).get("content").toString())
      .block();
  }
}