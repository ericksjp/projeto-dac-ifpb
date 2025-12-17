package com.ifpb.charger_manager.infra.client;

import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import jakarta.xml.bind.JAXBElement;
import lombok.RequiredArgsConstructor;

import com.ifpb.charger_manager.ws.v1.MessageResponse;
import com.ifpb.charger_manager.ws.v1.ObjectFactory;

@Component
@RequiredArgsConstructor
public class MessageClient extends WebServiceGatewaySupport {

    private final WebServiceTemplate webServiceTemplate;
    private final ObjectFactory objectFactory = new ObjectFactory();

    public String getMessage(Long id) {
        JAXBElement<Long> request = objectFactory.createMessageId(id);
        System.out.println(request.getValue());
        MessageResponse response;

        try {
            response = (MessageResponse) webServiceTemplate.marshalSendAndReceive(
                    webServiceTemplate.getDefaultUri(),
                    request,
                    null);
        } catch (Exception e) {
            throw new RuntimeException("communication breakdown", e);
        }

        return response.getMessage();
    }

    public Long createMessage(String message) {
        JAXBElement<String> request = objectFactory.createMessageContent(message);
        JAXBElement<Long> response;

        try {
            response = (JAXBElement<Long>) webServiceTemplate.marshalSendAndReceive(
                    webServiceTemplate.getDefaultUri(),
                    request,
                    null);
        } catch (Exception e) {
            throw new RuntimeException("communication breakdown", e);
        }

        return response.getValue();
    }
}
