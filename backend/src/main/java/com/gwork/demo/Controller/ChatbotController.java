package com.gwork.demo.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gwork.demo.Service.chatbot.ChatService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/chat")
public class ChatbotController {
  private final ChatService chatService;

  // ChatService をコンストラクタで DI
  public ChatbotController(ChatService chatService) {
    this.chatService = chatService;
  }

  @GetMapping("/hello")
  public String hello() {
    return chatService.chat("こんにちは");
  }

  @GetMapping("sampleMessage")
  public JsonNode getSampleMessage() {
    ObjectMapper mapper = new ObjectMapper();
    String sampleString = "{\n" + //
            "  \"text\": \"成人男性1食分の献立テストデータ（和食・洋食・中華の3パターン）を作成しました。使用食材は全てリスト内のものを使用し、調理の手間を最小限に抑えています。\",\n" + //
            "  \"meals\": [\n" + //
            "    {\n" + //
            "      \"menu_name\": \"和食：鶏もも肉の照り焼き定食\",\n" + //
            "      \"dish_name\": [\n" + //
            "        \"ご飯\",\n" + //
            "        \"鶏もも肉の簡単照り焼き\",\n" + //
            "        \"キャベツともやしの塩炒め\",\n" + //
            "        \"豆腐と彩り野菜の味噌汁\",\n" + //
            "        \"卵焼き\"\n" + //
            "      ],\n" + //
            "      \"ingredients\": {\n" + //
            "        \"うるち米(単一原料米,\\\"コシヒカリ\\\")\": \"200.0g\",\n" + //
            "        \"鶏肉(もも)\": \"100.0g\",\n" + //
            "        \"牛乳(店頭売り,紙容器)\": \"100.0g\",\n" + //
            "        \"鶏卵\": \"60.0g\",\n" + //
            "        \"キャベツ\": \"90.0g\",\n" + //
            "        \"もやし\": \"40.0g\",\n" + //
            "        \"にんじん\": \"30.0g\",\n" + //
            "        \"えのきたけ\": \"20.0g\",\n" + //
            "        \"豆腐\": \"100.0g\"\n" + //
            "      },\n" + //
            "      \"instructions\": \"【照り焼き】鶏肉をフライパンで焼き、醤油・みりん・砂糖（リスト外）を絡める。【塩炒め】キャベツ、もやしを炒め、塩コショウ（リスト外）で味付けする。【味噌汁】にんじん、えのきを煮て、豆腐と味噌（リスト外）を加える。【卵焼き】卵を溶いて焼く。\",\n" + //
            "      \"total_price\": 550,\n" + //
            "      \"total_calorie\": 880\n" + //
            "    },\n" + //
            "    {\n" + //
            "      \"menu_name\": \"洋食：チキンソテーと具だくさんミルクポタージュセット\",\n" + //
            "      \"dish_name\": [\n" + //
            "        \"ライス\",\n" + //
            "        \"鶏もも肉の簡単チキンソテー（レモンバター風味）\",\n" + //
            "        \"キャベツともやしのコールスロー\",\n" + //
            "        \"豆腐とえのきのミルクポタージュ\",\n" + //
            "        \"目玉焼き\"\n" + //
            "      ],\n" + //
            "      \"ingredients\": {\n" + //
            "        \"うるち米(単一原料米,\\\"コシヒカリ\\\")\": \"200.0g\",\n" + //
            "        \"鶏肉(もも)\": \"100.0g\",\n" + //
            "        \"牛乳(店頭売り,紙容器)\": \"100.0g\",\n" + //
            "        \"鶏卵\": \"60.0g\",\n" + //
            "        \"キャベツ\": \"90.0g\",\n" + //
            "        \"もやし\": \"40.0g\",\n" + //
            "        \"にんじん\": \"30.0g\",\n" + //
            "        \"えのきたけ\": \"20.0g\",\n" + //
            "        \"豆腐\": \"100.0g\"\n" + //
            "      },\n" + //
            "      \"instructions\": \"【チキンソテー】鶏肉に塩胡椒（リスト外）を振り、フライパンで焼く。焼き上がりにバター・レモン汁（リスト外）を絡める。【コールスロー】キャベツ、にんじんを千切りにし、もやしを茹で、マヨネーズ・酢（リスト外）で和える。【ミルクポタージュ】コンソメ（リスト外）で野菜と豆腐を煮て、牛乳を加える。【目玉焼き】卵をフライパンで焼く。\",\n" + //
            "      \"total_price\": 600,\n" + //
            "      \"total_calorie\": 920\n" + //
            "    },\n" + //
            "    {\n" + //
            "      \"menu_name\": \"中華：鶏肉と野菜のオイスターソース炒め\",\n" + //
            "      \"dish_name\": [\n" + //
            "        \"ご飯\",\n" + //
            "        \"鶏肉とキャベツ、にんじんのオイスター炒め\",\n" + //
            "        \"もやしとえのきの卵スープ\",\n" + //
            "        \"冷奴（中華風）\",\n" + //
            "        \"牛乳\"\n" + //
            "      ],\n" + //
            "      \"ingredients\": {\n" + //
            "        \"うるち米(単一原料米,\\\"コシヒカリ\\\")\": \"200.0g\",\n" + //
            "        \"鶏肉(もも)\": \"100.0g\",\n" + //
            "        \"牛乳(店頭売り,紙容器)\": \"100.0g\",\n" + //
            "        \"鶏卵\": \"60.0g\",\n" + //
            "        \"キャベツ\": \"90.0g\",\n" + //
            "        \"もやし\": \"40.0g\",\n" + //
            "        \"にんじん\": \"30.0g\",\n" + //
            "        \"えのきたけ\": \"20.0g\",\n" + //
            "        \"豆腐\": \"100.0g\"\n" + //
            "      },\n" + //
            "      \"instructions\": \"【オイスター炒め】鶏肉、キャベツ、にんじんを炒め、オイスターソース・醤油（リスト外）で味付けする。【卵スープ】鶏ガラだし（リスト外）でもやしとえのきを煮て、溶き卵を回し入れる。【冷奴】豆腐に醤油・ラー油（リスト外）などをかけて中華風に仕上げる。【ご飯】うるち米を炊飯する。\",\n" + //
            "      \"total_price\": 530,\n" + //
            "      \"total_calorie\": 850\n" + //
            "    }\n" + //
            "  ]\n" + //
            "}";
    JsonNode sampleMessage = null;
    try {
      sampleMessage = mapper.readTree(sampleString);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return sampleMessage;
  }
  
}
