package com.gwork.demo.Service.chatbot;

import org.springframework.stereotype.Service;

@Service
public class ChatService {
  private final GeminiClientService geminiClientService;

  public ChatService(GeminiClientService geminiClientService){
    this.geminiClientService = geminiClientService;
  }

  public String chat(String userMessage){

    //外部知識はここで用意する

    /*
    String prompt = "以下の文書を参考に、質問に答えてください。\n\n"
                  + "参考文書:\n" + context + "\n\n"
                  + "質問:\n" + userMessage;
    return geminiClientService.chat(prompt);
    */
    return geminiClientService.chat(userMessage);
  }
}