package com.ifpb.charger_proxy.endpoints;

import com.ifpb.charger_proxy.schemas.MessageRequest;
import com.ifpb.charger_proxy.schemas.MessageResponse;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import lombok.RequiredArgsConstructor;

@Endpoint
@RequiredArgsConstructor
public class HelloEndpoint {
    private static final String NAMESPACE_URI = "http://ifpb.com/charger-proxy";

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "MessageRequest")
    @ResponsePayload
    public MessageResponse handleGetMessage(@RequestPayload MessageRequest request) {
        String message = request.getMessage();

        MessageResponse response = new MessageResponse();
        response.setMessage(String.format("Hello %s! I am %s.", message, getHostname()));
        return response;
    }

    private String getHostname() {
        try {
            return System.getenv("HOSTNAME");
        } catch (Exception e) {
            return "space";
        }
    }
}

