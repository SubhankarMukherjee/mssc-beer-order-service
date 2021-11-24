package guru.sfg.beer.order.service.testpackage;

import com.comon.brewery.model.event.ValidateOrderRequest;
import com.comon.brewery.model.event.ValidateOrderResult;
import guru.sfg.beer.order.service.config.JmsConfigConvert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderValidationListener {
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfigConvert.VALIDATE_ORDER_QUEUE)
    public void listen(ValidateOrderRequest validateOrderRequest){
        boolean isValid = true;
        boolean sendResponse = true;

        ValidateOrderRequest request = validateOrderRequest;

        System.out.println("Validation order request to Beer Service received"+validateOrderRequest.toString());
        //condition to fail validation
        if (request.getBeerOrderDto().getCustomerRef() != null) {
            if (request.getBeerOrderDto().getCustomerRef().equals("fail-validation")){
                isValid = false;
            } else if (request.getBeerOrderDto().getCustomerRef().equals("dont-validate")){
                sendResponse = false;
            }
        }

        if (sendResponse) {


            ValidateOrderResult response = ValidateOrderResult.builder()
                    .isValid(isValid)
                    .orderId(request.getBeerOrderDto().getId())
                    .build();
            jmsTemplate.convertAndSend(JmsConfigConvert.VALIDATE_ORDER_RESPONSE_QUEUE,
                    response);

            System.out.println("Validation order response sent from Beer Service "+response.toString());
        }
    }
}
