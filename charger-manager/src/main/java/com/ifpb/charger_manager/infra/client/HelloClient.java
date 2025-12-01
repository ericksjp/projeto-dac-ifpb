package com.ifpb.charger_manager.infra.client;

import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import com.ifpb.charger_manager.wsdl.MessageResponse;
import com.ifpb.charger_manager.wsdl.MessageRequest;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HelloClient extends WebServiceGatewaySupport {

    private final WebServiceTemplate webServiceTemplate;

    public String sayHi(String person) {
        var request = new MessageRequest();
        request.setMessage(person);

        MessageResponse response = (MessageResponse) webServiceTemplate.marshalSendAndReceive(
                webServiceTemplate.getDefaultUri(),
                request,
                null);

        return response.getMessage();
    }
}
