package com.gwork.demo.Service.chatbot;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Service
public class GeminiClientService {
  private final WebClient webClient;

  public GeminiClientService(@Value("${gemini.api.key}") String apiKey) {
    System.out.println("API KEY = " + apiKey);
    this.webClient = WebClient.builder()
        .baseUrl("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent")
        .defaultHeader("Content-Type", "application/json")
        .defaultHeader("x-goog-api-key", apiKey)
        .build();
  }

  // プロンプトをgeminiに渡し、レスポンスから回答内容の部分を取り出す
  public JsonNode chat(String prompt) {
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, Object> schema = difineSchema();
    // ↓の形式でリクエストを送る仕様にgemini側で定められているっぽい
    Map<String, Object> body = Map.of(
        "contents", List.of(
            Map.of("parts", List.of(Map.of("text", prompt)))));
    return webClient.post()
        .bodyValue(body)
        .retrieve()
        .bodyToMono(Map.class)
        .map(res -> {
          Map<String, Object> cand = (Map<String, Object>) ((List<?>) res.get("candidates")).get(0);
          Map<String, Object> content = (Map<String, Object>) cand.get("content");
          List<Object> parts = (List<Object>) content.get("parts");
          Map<String, Object> part0 = (Map<String, Object>) parts.get(0);
          String text = part0.get("text").toString();
          text = text.replaceFirst("^```json\\s*", "").replaceFirst("```$", "");    // 前後の ```json と ``` があるなら削除しておく
        try {
          return objectMapper.readTree(text); // ← ここで JsonNode 化
        } catch (JsonProcessingException e) {
          throw new RuntimeException("JSON parse error", e);
        }
    })
    .block();
  }

  // レスポンスの形式をこちらで指定する
  private static Map<String, Object> difineSchema() {
    Map<String, Object> schema = Map.ofEntries(
        Map.entry("type", "object"),
        Map.entry("description", "いくつかの種類の献立と、付随するメッセージをレスポンスとして返してください。"),
        Map.entry("properties", Map.ofEntries(
            Map.entry("text", Map.of(
                "type", "string",
                "description", "どのような観点で献立を作成したかと、以下の献立の中から1つを選択する旨のメッセージをここに記述してください。")),
            Map.entry("meals", Map.ofEntries(
                Map.entry("type", "array"),
                Map.entry("description", "1つの献立についての情報をまとめ、それらを並べた配列です。"),
                Map.entry("items", Map.ofEntries(
                    Map.entry("type", "object"),
                    Map.entry("description", "1つの献立についての詳細な情報をまとめたオブジェクトです。"),
                    Map.entry("properties", Map.ofEntries(
                        Map.entry("menu_name", Map.of(
                            "type", "string",
                            "description", "献立につける題名をここに記述してください。")),
                        Map.entry("dish_name", Map.ofEntries(
                            Map.entry("type", "object"),
                            Map.entry("description", "主食は何、副菜は何、というように、各料理の役割についての辞書です。"),
                            Map.entry("additionalProperties", Map.of(
                                "type", "string",
                                "description", "主食：料理名 のように各料理の役割を記載します。")))),
                        Map.entry("ingredients", Map.ofEntries(
                            Map.entry("type", "object"),
                            Map.entry("description", "必要となる食材と、そのグラム数についての辞書です。"),
                            Map.entry("additionalProperties", Map.of(
                                "type", "string",
                                "description", "食材名：グラム数 のように必要な量を記載します。単位である'g'をつけ忘れないよう注意してください。")))),
                        Map.entry("instructions", Map.ofEntries(
                            Map.entry("type", "object"),
                            Map.entry("description", "各料理の調理手順についての辞書です。"),
                            Map.entry("additionalProperties", Map.of(
                                "type", "string",
                                "description", "料理名： 調理手順 のように記載します。なお、料理名は【】で囲んで強調してください。")))),
                        Map.entry("total_price", Map.of(
                            "type", "number",
                            "description", "その献立にかかるおおよその合計金額です。単位は'円'を付けてください。")),
                        Map.entry("total_calorie", Map.of(
                            "type", "number",
                            "description", "その献立のおおよその合計カロリーです。単位は'kcal'を付けてください。"))))
                    )))))));
    return schema;
  }

  // 動作確認用のchat
  public String sampleChat(String prompt) {
    Map<String, Object> schema = difineSampleSchema();
    // 以下の形式でリクエストを送る仕様にgemini側で定められているっぽい
    Map<String, Object> body = Map.of(
        "contents", List.of(
            Map.of("parts", List.of(Map.of("text", prompt)))),
        "generationConfig", Map.of(
            "response_mime_type", "application/json",
            "response_schema", schema));
    return webClient.post()
        .bodyValue(body)
        .retrieve()
        .bodyToMono(Map.class)
        .map(res -> {
          Map<String, Object> cand = (Map<String, Object>) ((List<?>) res.get("candidates")).get(0);
          Map<String, Object> content = (Map<String, Object>) cand.get("content");
          List<Object> parts = (List<Object>) content.get("parts");
          Map<String, Object> part0 = (Map<String, Object>) parts.get(0);
          return part0.get("text").toString();
        })
        .block();
  }

  // 動作確認用のschema定義
  public static Map<String, Object> difineSampleSchema() {
    Map<String, Object> schema = Map.of(
        "type", "object",
        "properties", Map.of(
            "text", Map.of("type", "string", "description", "")),
        "required", List.of("text"));
    return schema;
  }
}