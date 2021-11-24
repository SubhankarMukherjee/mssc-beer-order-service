package guru.sfg.beer.order.service.testpackage;

import com.comon.brewery.model.event.AllocateOrderRequest;
import com.comon.brewery.model.event.AllocateOrderResult;
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
public class BeerOrderAllocationListener {
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfigConvert.ALLOCATE_ORDER_QUEUE)
    public void listen(AllocateOrderRequest allocateOrderRequest){


        AllocateOrderRequest request = allocateOrderRequest;

        System.out.println("Allocation Request Received from inventory service****"+ request.toString());

    request.getBeerOrderDto().getBeerOrderLines().forEach(line->{
        line.setQuantityAllocated(line.getOrderQuantity());
    });

        AllocateOrderResult allocateOrderResult = AllocateOrderResult.builder()
                .beerOrderDto(request.getBeerOrderDto())
                .pendingInventory(false)
                .allocationError(false)
                .build();
        jmsTemplate.convertAndSend(JmsConfigConvert.ALLOCATE_ORDER_RESPONSE_QUEUE,
                allocateOrderResult);
        System.out.println("Allocation Request Response sent from inventory service****"+ allocateOrderResult.toString());

        }
    }

