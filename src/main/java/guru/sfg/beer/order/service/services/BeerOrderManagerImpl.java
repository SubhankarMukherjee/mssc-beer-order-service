package guru.sfg.beer.order.service.services;

import com.comon.brewery.model.BeerOrderDto;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.statemachine.BeerOrderStateChangeInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class BeerOrderManagerImpl implements BeerOrderManager {

    public static final String ORDER_ID_HEADER = "order_id";
    private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachineFactory;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderStateChangeInterceptor beerOrderStateChangeInterceptor;

    @Transactional
    @Override
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {
        //Defensive coding
        beerOrder.setId(null);
        beerOrder.setOrderStatus(guru.sfg.beer.order.service.domain.BeerOrderStatusEnum.NEW);
        //Defensive coding

        //Save it to repo
        BeerOrder savedBeerOrder = beerOrderRepository.save(beerOrder);
        System.out.println("New Beer Saved****"+ savedBeerOrder.getId().toString());
        // create msg with event and send the event to state machine which is dehidrate from database
        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATE_ORDER);
        return savedBeerOrder;
    }

    @Transactional
    @Override
    public void processValidationResult(UUID orderId, boolean valid) {

        while(beerOrderRepository.findById(orderId)==null)
        {
            try {
                Thread.sleep(200);
            }
            catch (Exception e)
            {

            }
        }


                    BeerOrder beerOrder =beerOrderRepository.findById(orderId).get();

      beerOrderRepository.findById(orderId).get();
                if (valid) {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_PASSED);

            // Need to take a fresh object becuae once above event was sent interceptor will update the status and we will be runing with stale object
            awaitForStatus(beerOrder.getId(), BeerOrderStatusEnum.VALIDATED);
            BeerOrder validatedOrder = beerOrderRepository.findById(orderId).get();

            System.out.println("************ Check 2"+ validatedOrder.getId().toString());
            sendBeerOrderEvent(validatedOrder, BeerOrderEventEnum.ALLOCATE_ORDER);
        } else sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_FAILED);
    }

    @Override
    public void beerOrderAllocationPassed(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());

        try {
            sendBeerOrderEvent(beerOrderOptional.get(), BeerOrderEventEnum.ALLOCATION_SUCCESS);
            awaitForStatus(beerOrderOptional.get().getId(), BeerOrderStatusEnum.ALLOCATED);
            updateAllocatedQty(beerOrderDto);
        } catch (Exception e) {
            log.error("Order Id Not Found: " + beerOrderDto.getId());
        }
    }

    @Override
    public void beerOrderAllocationPendingInventory(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());

        try {
            beerOrderOptional.ifPresent(beerOrder -> {
                sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);
                awaitForStatus(beerOrder.getId(), BeerOrderStatusEnum.PENDING_INVENTORY);
                updateAllocatedQty(beerOrderDto);
            });
        } catch (Exception e) {
            log.error("Order Id Not Found: " + beerOrderDto.getId());
        }

    }

    private void updateAllocatedQty(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> allocatedOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());

        try {
            allocatedOrderOptional.ifPresent(allocatedOrder -> {
                allocatedOrder.getBeerOrderLines().forEach(beerOrderLine -> {
                    beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
                        if (beerOrderLine.getId().equals(beerOrderLineDto.getId()))
                            beerOrderLine.setQuantityAllocated(beerOrderLineDto.getQuantityAllocated());

                    });
                });

                beerOrderRepository.saveAndFlush(allocatedOrder);
            });
        } catch (Exception e) {
            log.error("Order Not Found. Id: " + beerOrderDto.getId());
        }
    }

    @Override
    public void beerOrderAllocationFailed(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());
        try {
            beerOrderOptional.ifPresent(beerOrder -> {
                sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_FAILED);
            });
        } catch (Exception e) {
            log.error("Order Not Found. Id: " + beerOrderDto.getId());
        }

    }

    @Override
    public void beerOrderPickedUp(UUID orderId) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(orderId);
        try {
            beerOrderOptional.ifPresent(beerOrder -> {
                // Generate Process for Picked up
                sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.BEERORDER_PICKED_UP);
            });
        } catch (Exception e) {
            log.error("Order Not Found. Id: " + orderId);
        }
    }

    @Override
    public void cancelOrder(UUID orderId) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(orderId);
        try {
            beerOrderOptional.ifPresent(beerOrder -> {
                // Generate Process for Picked up
                sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.CANCEL_ORDER);
            });
        } catch (Exception e) {
            log.error("Order Not Found. Id: " + orderId.toString());
        }
    }

    private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEventEnum eventEnum) {
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine = build(beerOrder);
        Message msg = MessageBuilder.withPayload(eventEnum).setHeader(ORDER_ID_HEADER, beerOrder.getId().toString()).build();
        stateMachine.sendEvent(msg);
    }

    private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> build(BeerOrder beerOrder) {
        // Re-hidrate
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine = stateMachineFactory.getStateMachine(beerOrder.getId());
        stateMachine.stop();
        stateMachine.getStateMachineAccessor().doWithAllRegions(sma -> {
            sma.addStateMachineInterceptor(beerOrderStateChangeInterceptor);
            sma.resetStateMachine(new DefaultStateMachineContext(beerOrder.getOrderStatus(), null, null, null));
        });
        stateMachine.start();
        return stateMachine;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    private void awaitForStatus(UUID beerOrderId, BeerOrderStatusEnum statusEnum) {

        AtomicBoolean found = new AtomicBoolean(false);
        AtomicInteger loopCount = new AtomicInteger(0);

        while (!found.get()) {
            if (loopCount.incrementAndGet() > 10) {
                found.set(true);
                log.debug("Loop Retries exceeded");
            }

          /*  beerOrderRepository.findById(beerOrderId).ifPresentOrElse(beerOrder -> {
                if (beerOrder.getOrderStatus().equals(statusEnum)) {
                    found.set(true);
                    log.debug("Order Found");
                } else {
                    log.debug("Order Status Not Equal. Expected: " + statusEnum.name() + " Found: " + beerOrder.getOrderStatus().name());
                }
            }, () -> {
                log.debug("Order Id Not Found");
            });*/

            beerOrderRepository.findById(beerOrderId).ifPresent(beerOrder -> {
                if (beerOrder.getOrderStatus().equals(statusEnum)) {
                    found.set(true);
                    log.debug("Order Found");
                } else {
                    log.debug("Order Status Not Equal. Expected: " + statusEnum.name() + " Found: " + beerOrder.getOrderStatus().name());
                }
            });
            if(beerOrderRepository.findById(beerOrderId)==null)
                log.debug("Order Id Not Found");



            if (!found.get()) {
                try {
                    log.debug("Sleeping for retry");
                    Thread.sleep(100);
                } catch (Exception e) {
                    // do nothing
                }
            }
        }
    }

}
