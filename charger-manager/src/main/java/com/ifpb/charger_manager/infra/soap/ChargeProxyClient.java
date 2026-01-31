package com.ifpb.charger_manager.infra.soap;

import com.ifpb.charger_manager.ws.charge.v1.ChargePortService;
import com.ifpb.charger_manager.ws.charge.v1.ChargePort;
import com.ifpb.charger_manager.ws.charge.v1.CreateChargeRequest;
import com.ifpb.charger_manager.ws.charge.v1.CreateChargeResponse;
import com.ifpb.charger_manager.ws.customer.v1.CustomerPortService;
import com.ifpb.charger_manager.ws.customer.v1.CustomerPort;
import com.ifpb.charger_manager.ws.customer.v1.RegisterCustomerRequest;
import com.ifpb.charger_manager.ws.customer.v1.RegisterCustomerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Cliente SOAP para comunicação com o charger-proxy
 */
@Component
public class ChargeProxyClient {
    
    private static final Logger log = LoggerFactory.getLogger(ChargeProxyClient.class);
    
    private final ChargePort chargePort;
    private final CustomerPort customerPort;
    
    public ChargeProxyClient(ChargePort chargePort, CustomerPort customerPort) {
        this.chargePort = chargePort;
        this.customerPort = customerPort;
    }
    
    /**
     * Registra um cliente no Asaas via charge-proxy
     */
    public RegisterCustomerResponse registerCustomer(String id, String name, String cpfCnpj, String email) {
        log.info("Calling SOAP: registerCustomer for id={}", id);
        
        RegisterCustomerRequest request = new RegisterCustomerRequest();
        request.setId(id);
        request.setName(name);
        request.setCpfCnpj(cpfCnpj);
        request.setEmail(email);
        
        RegisterCustomerResponse response = customerPort.registerCustomer(request);
        log.info("SOAP response: customerExternalId={}", response.getCustomerExternalId());
        
        return response;
    }
    
    /**
     * Cria uma cobrança no Asaas via charge-proxy
     */
    public CreateChargeResponse createCharge(
            String customerId,
            String billingType,
            BigDecimal value,
            String dueDate,
            String description,
            Integer installmentCount) {
        
        log.info("Calling SOAP: createCharge for customerId={}, type={}, value={}", 
                 customerId, billingType, value);
        
        CreateChargeRequest request = new CreateChargeRequest();
        request.setCustomerId(customerId);
        request.setBillingType(billingType);
        request.setValue(value);
        request.setDueDate(dueDate);
        request.setDescription(description);
        
        if (installmentCount != null && installmentCount > 1) {
            request.setInstallmentCount(installmentCount);
        }
        
        CreateChargeResponse response = chargePort.createCharge(request);
        log.info("SOAP response: chargeId={}, status={}", response.getChargeId(), response.getStatus());
        
        return response;
    }
}
