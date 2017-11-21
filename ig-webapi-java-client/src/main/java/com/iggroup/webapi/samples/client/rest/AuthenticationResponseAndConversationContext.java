package com.iggroup.webapi.samples.client.rest;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthenticationResponseAndConversationContext {

	public ConversationContext getConversationContext() {
		return conversationContext;
	}

	public String getAccountId() {
		return accountId;
	}

	public String getLightstreamerEndpoint() {
		return lightstreamerEndpoint;
	}

	private ConversationContext conversationContext;
	private String accountId;
	private String lightstreamerEndpoint;
}
