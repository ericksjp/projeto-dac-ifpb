package com.ifpb.charger_manager.infra.config;

import com.ifpb.charger_manager.ws.charge.v1.ChargePortService;
import com.ifpb.charger_manager.ws.charge.v1.ChargePort;
import com.ifpb.charger_manager.ws.customer.v1.CustomerPortService;
import com.ifpb.charger_manager.ws.customer.v1.CustomerPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.xml.ws.BindingProvider;
import java.net.URL;

/**
 * Configuração dos clientes SOAP para charger-proxy
 */
@Configuration
public class WebServiceConfig {
    
    @Value("${charger-proxy.soap.charge-url}")
    private String chargeServiceUrl;
    
    @Value("${charger-proxy.soap.customer-url}")
    private String customerServiceUrl;
    
    @Bean
    public ChargePort chargePort() throws Exception {
        URL wsdlUrl = new URL(chargeServiceUrl + ".wsdl");
        ChargePortService service = new ChargePortService(wsdlUrl);
        ChargePort port = service.getChargePortSoap11();
        
        // Configura o endpoint address
        BindingProvider bindingProvider = (BindingProvider) port;
        bindingProvider.getRequestContext().put(
            BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
            chargeServiceUrl
        );
        
        return port;
    }
    
    @Bean
    public CustomerPort customerPort() throws Exception {
        URL wsdlUrl = new URL(customerServiceUrl + ".wsdl");
        CustomerPortService service = new CustomerPortService(wsdlUrl);
        CustomerPort port = service.getCustomerPortSoap11();
        
        // Configura o endpoint address
        BindingProvider bindingProvider = (BindingProvider) port;
        bindingProvider.getRequestContext().put(
            BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
            customerServiceUrl
        );
        
        return port;
    }
}
