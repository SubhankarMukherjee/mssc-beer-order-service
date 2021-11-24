package guru.sfg.beer.order.service;

import com.comon.brewery.model.BeerDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderLine;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.domain.Customer;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.repositories.CustomerRepository;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import guru.sfg.beer.order.service.services.beer.BeerServiceImpl;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import static com.github.tomakehurst.wiremock.client.WireMock.get;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.github.jenspiegsa.wiremockextension.ManagedWireMockServer.with;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
@ExtendWith(WireMockExtension.class)
@SpringBootTest
public class BeerOrderManagerIT {

    @Autowired
    BeerOrderManager beerOrderManager;

    @Autowired
    BeerOrderRepository beerOrderRepository;

    @Autowired
    CustomerRepository customerRepository;

    Customer testCustomer;

    UUID beerId= UUID.randomUUID();
    @Autowired
    WireMockServer wireMockServer;

    @Autowired
    ObjectMapper objectMapper;

    @TestConfiguration
    static class RestTemplateBuilderProvider {
        @Bean(destroyMethod = "stop")
        public WireMockServer wireMockServer(){
            WireMockServer server = with(wireMockConfig().port(8083));
            server.start();
            return server;
        }
    }
    @BeforeEach
    void setUp()
    {
        testCustomer= customerRepository.save(Customer.builder().customerName("Test Customer").build());
    }
    @Test
    void testNewToAllocated() throws JsonProcessingException, InterruptedException {
        BeerDTO beerDTO = BeerDTO.builder().id(beerId).upc("12345").price(new BigDecimal("100.21")).beerStyle("IPA").build();

        wireMockServer.stubFor(get(BeerServiceImpl.BEER_UPC_PATH_V1+"12345")
                .willReturn(okJson(objectMapper.writeValueAsString(beerDTO))));

        BeerOrder beerOrder= createBeerOrder();


        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        //System.out.println("Sleeping");
        //Thread.sleep(5000);
        //System.out.println("Awake");

        await().untilAsserted(()->{
            BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
            //allocated status
            assertEquals(BeerOrderStatusEnum.ALLOCATED,foundOrder.getOrderStatus());
        });
        await().untilAsserted(()->{
            BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
            BeerOrderLine line = foundOrder.getBeerOrderLines().iterator().next();
            assertEquals(line.getOrderQuantity(),line.getQuantityAllocated());
        });

        BeerOrder savedBeerOrder2 = beerOrderRepository.findById(savedBeerOrder.getId()).get();

        assertNotNull(savedBeerOrder2);
        assertEquals(BeerOrderStatusEnum.ALLOCATED,savedBeerOrder2.getOrderStatus());
    }

    public BeerOrder createBeerOrder()
    {
        BeerOrder beerOrder= BeerOrder.builder().customer(testCustomer).build();

        //create order line and add beer order into it
        Set<BeerOrderLine> beerOrderLineSet = new HashSet<>();
        beerOrderLineSet.add(BeerOrderLine.builder().beerOrder(beerOrder).upc("12345").orderQuantity(1).beerId(beerId).build());

        // now reverse add beerlines into beer order

        beerOrder.setBeerOrderLines(beerOrderLineSet);
        return  beerOrder;
    }

}
