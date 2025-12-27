package com.gwork.demo.Service.chatbot;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gwork.demo.Service.ilp.ILPResultDTO;
import com.gwork.demo.Service.ilp.SortILPResult;

@Service
public class ChatService {
  private final GeminiClientService geminiClientService;

  public ChatService(GeminiClientService geminiClientService){
    this.geminiClientService = geminiClientService;
  }

  // 動作確認用のチャット
  public String sampleChat(String sample){
    return geminiClientService.sampleChat(sample);
  }

  // 送られてきたキーワードを使ってプロンプトを作り、geminiに質問する
  public String chat(String keyWord){

    //外部知識はここで用意する
    SortILPResult sortILPResult = new SortILPResult();
    List<ILPResultDTO> trimmedList = sortILPResult.trimILPResultDTOs();
    ObjectMapper mapper = new ObjectMapper();
    String context = null;
    try {
      context = mapper.writeValueAsString(trimmedList);
    }catch(Exception e) {
      
    }
    
    String schema = difineSchema();
    String prompt = "下記の条件を満たし、かつ下記のキーワードにあてはまる食事の献立を、3種類生成してください。なお、調理手順は料理初心者でも分かりやすいよう詳細に記述してください。\nレスポンスの形式は下記のスキーマに従うこと。\n使用する食材の組み合わせは、コンテキスト内にあるingredientsの中から選び、**必ずその中のすべての食材を**使用するようにしてください。その分量も併せて記載されているので、それに注意すること。\nただし、キーワードで使用する食材を指定され、コンテキスト内にそれを満たす食材の組み合わせが存在しない場合は、mealsを空として設定を変更する旨のメッセージのみを返すようにしてください。\n\n"
                  + "条件:\n" + "調理にかかる手間が少ないこと、日本の家庭で一般的に食べられている一般的な料理であること、3種類の献立は和洋バランスよく生成すること。\n"
                  + "キーワード:\n" + keyWord
                  + "\nスキーマ:\n" + schema
                  + "\nコンテキスト" + context;
    return geminiClientService.chat(prompt);
  }


  public static String difineSchema() {
    String schema = "{\r\n" + //
            "  \"type\": \"object\",\r\n" + //
            "  \"description\": \"いくつかの種類の献立と、付随するメッセージをレスポンスとして返してください。\",\r\n" + //
            "  \"properties\": {\r\n" + //
            "    \"text\": {\r\n" + //
            "      \"type\": \"string\",\r\n" + //
            "      \"description\": \"どのような観点で献立を作成したかと、以下の献立の中から1つを選択する旨のメッセージをここに記述してください。\"\r\n" + //
            "    },\r\n" + //
            "    \"meals\": {\r\n" + //
            "      \"type\": \"array\",\r\n" + //
            "      \"description\": \"1つの献立についての情報をまとめ、それらを並べた配列です。\",\r\n" + //
            "      \"items\": {\r\n" + //
            "        \"type\": \"object\",\r\n" + //
            "        \"description\": \"1つの献立についての詳細な情報をまとめたオブジェクトです。\",\r\n" + //
            "        \"properties\": {\r\n" + //
            "          \"selectedId\": {\r\n" + //
            "            \"type\": \"number\",\r\n" + //
            "            \"description\": \"どの食材の組み合わせを選択したのかを識別するための値です。context内のフィールドである、'id'の値をそのまま抜き出してください。\"\r\n" + //
            "          },\r\n" + //
            "          \"menuName\": {\r\n" + //
            "            \"type\": \"string\",\r\n" + //
            "            \"description\": \"献立名をここに記述してください。\"\r\n" + //
            "          },\r\n" + //
            "          \"dishName\": {\r\n" + //
            "            \"type\": \"object\",\r\n" + //
            "            \"description\": \"主食は何、副菜は何、というように、各料理の役割についての辞書です。\",\r\n" + //
            "            \"additionalProperties\": {\r\n" + //
            "              \"type\": \"string\",\r\n" + //
            "              \"description\": \"主食：料理名 のように各料理の役割を記載します。\"\r\n" + //
            "            }\r\n" + //
            "          },\r\n" + //
            "          \"instructions\": {\r\n" + //
            "            \"type\": \"object\",\r\n" + //
            "            \"description\": \"各料理(dishNameの要素)の調理手順についての辞書です。\",\r\n" + //
            "            \"additionalProperties\": {\r\n" + //
            "              \"type\": \"string\",\r\n" + //
            "              \"description\": \"料理名： 調理手順 のように記載します。なお、料理名(食材名ではない)は【】で囲んで強調してください。\"\r\n" + //
            "            }\r\n" + //
            "          }\r\n" + //
            "          \"seasonings\": {\r\n" + //
            "            \"type\": \"object\",\r\n" + //
            "            \"description\": \"食材リストの他に必要となる、調味料とその量についての辞書です。\",\r\n" + //
            "            \"additionalProperties\": {\r\n" + //
            "              \"type\": \"string\",\r\n" + //
            "              \"description\": \"調味料名：分量 のように必要な量を記載します。\"\r\n" + //
            "            }\r\n" + //
            "          },\r\n" + //
            "        }\r\n" + //
            "      }\r\n" + //
            "    }\r\n" + //
            "  }\r\n" + //
            "}\r\n" + //
            "";
    return schema;
  }
}