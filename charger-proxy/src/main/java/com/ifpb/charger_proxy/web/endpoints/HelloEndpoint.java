package com.ifpb.charger_proxy.web.endpoints;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.ifpb.charger_proxy.model.Message;
import com.ifpb.charger_proxy.schemas.MessageResponse;
import com.ifpb.charger_proxy.schemas.ObjectFactory;
import com.ifpb.charger_proxy.service.MessageService;

import jakarta.xml.bind.JAXBElement;
import lombok.RequiredArgsConstructor;

@Endpoint
@RequiredArgsConstructor
public class HelloEndpoint {
    private static final String NAMESPACE_URI = "http://ifpb.com/charger-proxy";

    private final MessageService messageService;
    private final ObjectFactory objectFactory = new ObjectFactory();

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "MessageId")
    @ResponsePayload
    public MessageResponse handleGetMessage(@RequestPayload JAXBElement<Long> id) {
        System.out.println(id.getValue());
        Message message = messageService.getMessageById(id.getValue());
        MessageResponse response = new MessageResponse();
        String msg;

        if (message == null) {
            msg = String.format("No message found for ID: %d\nServed by: %s", id.getValue(), getHostname());
            response.setMessage(msg);
            return response;
        }


        msg = String.format(
            "Message retrieved successfully!\n" +
            "ID: %d\n" +
            "Content: '%s'\n" +
            "Served by: %s",
            id.getValue(),
            message.getContent(),
            getHostname()
        );
        response.setMessage(msg);

        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "MessageContent")
    @ResponsePayload
    public JAXBElement<Long> handleCreateMessage(@RequestPayload JAXBElement<String> message) {
        Long id = messageService.create(message.getValue());
        System.out.println(message.getValue());

        JAXBElement<Long> messageIdElement = objectFactory.createMessageId(id);

        return messageIdElement;
    }

    private String getHostname() {
        try {
            return System.getenv("HOSTNAME");
        } catch (Exception e) {
            return "space";
        }
    }
}
