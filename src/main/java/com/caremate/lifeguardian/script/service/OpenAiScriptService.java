package com.caremate.lifeguardian.script.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OpenAiScriptService {

	private final ChatClient.Builder chatClientBuilder;

	public String generate(String prompt) {
		return chatClientBuilder.build()
				.prompt()
				.user(prompt)
				.call()
				.content();
	}
}