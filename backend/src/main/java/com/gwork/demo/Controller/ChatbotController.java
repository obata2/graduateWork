package com.gwork.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gwork.demo.Service.chatbot.ChatService;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/chat")
public class ChatbotController {
  private final ChatService chatService;

  // ChatService をコンストラクタで DI
  public ChatbotController(ChatService chatService) {
    this.chatService = chatService;
  }

  @PostMapping("/hello")
  public String hello(@RequestBody Map<String, String> body) {
    return chatService.sampleChat(body.get("text"));
  }

  @PostMapping("/generateMeals")
  public String generateMeals(@RequestBody Map<String, String> body) {
    return chatService.chat(body.get("text"), body.get("userId"));
  }

  @GetMapping("sampleMessage")
  public JsonNode getSampleMessage() {
    ObjectMapper mapper = new ObjectMapper();
    String sampleString = "{\r\n" + //
        "  \"text\": \"料理初心者の方でも作りやすいよう、和食・洋食・中華の3つの献立を作成しました。卵は主菜や副菜に組み込んでいます。\",\r\n" + //
        "  \"meals\": [\r\n" + //
        "    {\r\n" + //
        "      \"menu_name\": \"和食：鶏もも肉の親子丼風煮込みと具だくさん味噌汁定食\",\r\n" + //
        "      \"dish_name\": {\r\n" + //
        "        \"主食\": \"ご飯\",\r\n" + //
        "        \"主菜\": \"鶏もも肉の親子丼風煮込み（卵とじ）\",\r\n" + //
        "        \"副菜1\": \"キャベツともやしのレンジ蒸し\",\r\n" + //
        "        \"副菜2\": \"豆腐、にんじん、えのきの味噌汁\",\r\n" + //
        "        \"その他\": \"牛乳\"\r\n" + //
        "      },\r\n" + //
        "      \"ingredients\": {\r\n" + //
        "        \"うるち米(単一原料米,\\\"コシヒカリ\\\")\": \"200.0g\",\r\n" + //
        "        \"鶏肉(もも)\": \"100.0g\",\r\n" + //
        "        \"牛乳(店頭売り,紙容器)\": \"100.0g\",\r\n" + //
        "        \"鶏卵\": \"60.0g\",\r\n" + //
        "        \"キャベツ\": \"90.0g\",\r\n" + //
        "        \"もやし\": \"40.0g\",\r\n" + //
        "        \"にんじん\": \"30.0g\",\r\n" + //
        "        \"えのきたけ\": \"20.0g\",\r\n" + //
        "        \"豆腐\": \"100.0g\"\r\n" + //
        "      },\r\n" + //
        "      \"instructions\": {\r\n" + //
        "        \"【ご飯】\": \"米を洗い、炊飯器で通常通りに炊飯する。\",\r\n" + //
        "        \"【鶏もも肉の親子丼風煮込み（卵とじ）】\": \"鶏肉を一口大に切る。鍋または小さめのフライパンに醤油、みりん、だし（リスト外）を入れ、鶏肉を煮る。鶏肉に火が通ったら、溶いた卵（60g）を全体に回し入れ、蓋をして半熟状になったら火を止める。\",\r\n"
        + //
        "        \"【キャベツともやしのレンジ蒸し】\": \"キャベツをざく切りにし、もやしと一緒に耐熱容器に入れる。ラップをかけて電子レンジ（600Wで2〜3分目安）で加熱する。水気を軽く絞り、ポン酢やごま油（リスト外）などで味付けをする。\",\r\n"
        + //
        "        \"【豆腐、にんじん、えのきの味噌汁】\": \"にんじんを薄切り、えのきは石づきを取って半分の長さに切る。鍋に水とだし（リスト外）、にんじん、えのきを入れて加熱し、柔らかくなったら豆腐を加え、味噌（リスト外）を溶き入れる。\",\r\n"
        + //
        "        \"【牛乳】\": \"グラスに注ぐ\"\r\n" + //
        "      },\r\n" + //
        "      \"total_price\": 600,\r\n" + //
        "      \"total_calorie\": 930\r\n" + //
        "    },\r\n" + //
        "    {\r\n" + //
        "      \"menu_name\": \"洋食：鶏肉と野菜のオムレツ風プレート\",\r\n" + //
        "      \"dish_name\": {\r\n" + //
        "        \"主食\": \"ライス\",\r\n" + //
        "        \"主菜\": \"鶏肉とキャベツのチーズオムレツ風\",\r\n" + //
        "        \"副菜1\": \"もやしとにんじんのソテー\",\r\n" + //
        "        \"副菜2\": \"豆腐、えのき、牛乳のミルクスープ\",\r\n" + //
        "        \"その他\": \"\"\r\n" + //
        "      },\r\n" + //
        "      \"ingredients\": {\r\n" + //
        "        \"うるち米(単一原料米,\\\"コシヒカリ\\\")\": \"200.0g\",\r\n" + //
        "        \"鶏肉(もも)\": \"100.0g\",\r\n" + //
        "        \"牛乳(店頭売り,紙容器)\": \"100.0g\",\r\n" + //
        "        \"鶏卵\": \"60.0g\",\r\n" + //
        "        \"キャベツ\": \"90.0g\",\r\n" + //
        "        \"もやし\": \"40.0g\",\r\n" + //
        "        \"にんじん\": \"30.0g\",\r\n" + //
        "        \"えのきたけ\": \"20.0g\",\r\n" + //
        "        \"豆腐\": \"100.0g\"\r\n" + //
        "      },\r\n" + //
        "      \"instructions\": {\r\n" + //
        "        \"【ライス】\": \"米を洗い、炊飯器で通常通りに炊飯する。\",\r\n" + //
        "        \"【鶏肉とキャベツのチーズオムレツ風】\": \"鶏肉とキャベツをフライパンで炒め、塩胡椒（リスト外）で味付けをする。溶いた卵（60g）を流し入れ、ピザ用チーズ（リスト外）などを散らして蓋をし、半熟状に火を通す。\",\r\n"
        + //
        "        \"【もやしとにんじんのソテー】\": \"もやしとにんじんをフライパンで炒め、塩胡椒（リスト外）とバター（リスト外）でシンプルに味付けをする。\",\r\n" + //
        "        \"【豆腐、えのき、牛乳のミルクスープ】\": \"えのきをコンソメ（リスト外）と水で煮て、豆腐を崩しながら加え、牛乳を加えて温める。塩胡椒（リスト外）で味を調える。\"\r\n" + //
        "      },\r\n" + //
        "      \"total_price\": 630,\r\n" + //
        "      \"total_calorie\": 950\r\n" + //
        "    },\r\n" + //
        "    {\r\n" + //
        "      \"menu_name\": \"中華：鶏肉と野菜の炒飯風と具だくさんスープ\",\r\n" + //
        "      \"dish_name\": {\r\n" + //
        "        \"主食\": \"鶏肉と野菜の炒飯風（ご飯、鶏肉、卵、にんじん、キャベツ）\",\r\n" + //
        "        \"主菜\": \"\",\r\n" + //
        "        \"副菜1\": \"もやしと豆腐、えのきのサンラータン風スープ\",\r\n" + //
        "        \"副菜2\": \"冷奴（中華風）\",\r\n" + //
        "        \"その他\": \"牛乳\"\r\n" + //
        "      },\r\n" + //
        "      \"ingredients\": {\r\n" + //
        "        \"うるち米(単一原料米,\\\"コシヒカリ\\\")\": \"200.0g\",\r\n" + //
        "        \"鶏肉(もも)\": \"100.0g\",\r\n" + //
        "        \"牛乳(店頭売り,紙容器)\": \"100.0g\",\r\n" + //
        "        \"鶏卵\": \"60.0g\",\r\n" + //
        "        \"キャベツ\": \"90.0g\",\r\n" + //
        "        \"もやし\": \"40.0g\",\r\n" + //
        "        \"にんじん\": \"30.0g\",\r\n" + //
        "        \"えのきたけ\": \"20.0g\",\r\n" + //
        "        \"豆腐\": \"100.0g\"\r\n" + //
        "      },\r\n" + //
        "      \"instructions\": {\r\n" + //
        "        \"【鶏肉と野菜の炒飯風（ご飯、鶏肉、卵、にんじん、キャベツ）】\": \"ご飯はあらかじめ炊いておく。鶏肉、にんじん、キャベツを細かく切り、フライパンで炒める。ご飯と溶き卵（60g）を加え、醤油・鶏ガラ（リスト外）などで手早く炒め合わせ、塩胡椒（リスト外）で味を調える。\",\r\n"
        + //
        "        \"【もやしと豆腐、えのきのサンラータン風スープ】\": \"もやし、豆腐、えのきを中華だし（リスト外）と水で煮て、醤油・酢・ラー油（リスト外）などで味付けをする。お好みで水溶き片栗粉（リスト外）でとろみをつけても良い。\",\r\n"
        + //
        "        \"【冷奴（中華風）】\": \"豆腐を皿に盛り、醤油やごま油、刻みネギ（リスト外）などをかけて食べる。\",\r\n" + //
        "        \"【牛乳】\": \"グラスに注ぐ\"\r\n" + //
        "      },\r\n" + //
        "      \"total_price\": 550,\r\n" + //
        "      \"total_calorie\": 890\r\n" + //
        "    }\r\n" + //
        "  ]\r\n" + //
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
