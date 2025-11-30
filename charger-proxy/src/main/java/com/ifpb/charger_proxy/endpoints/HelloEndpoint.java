package com.ifpb.charger_proxy.endpoints;

import com.ifpb.charger_proxy.schemas.GetMessageResponse;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import lombok.RequiredArgsConstructor;

@Endpoint
@RequiredArgsConstructor
public class HelloEndpoint {
    private static final String NAMESPACE_URI = "http://ifpb.com/charger-proxy";

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getMessageResponse")
    @ResponsePayload
    public GetMessageResponse handleGetMessage() {
        GetMessageResponse response = new GetMessageResponse();
        response.setMessage("hello!");
        return response;
    }
}

