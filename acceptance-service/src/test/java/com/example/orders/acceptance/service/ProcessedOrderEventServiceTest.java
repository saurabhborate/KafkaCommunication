package com.example.orders.acceptance.service;
import static org.mockito.Mockito.*;
import com.example.orders.acceptance.domain.Order;import com.example.orders.acceptance.repository.ConsumedEvent;import com.example.orders.acceptance.repository.ConsumedEventRepository;import com.example.orders.acceptance.repository.OrderRepository;import com.example.orders.contracts.OrderProcessedEvent;
import java.math.BigDecimal;import java.time.Instant;import java.util.Optional;import org.junit.jupiter.api.BeforeEach;import org.junit.jupiter.api.Test;
class ProcessedOrderEventServiceTest{
 private OrderRepository orders;private ConsumedEventRepository consumed;private ProcessedOrderEventService service;
 @BeforeEach void setup(){orders=mock(OrderRepository.class);consumed=mock(ConsumedEventRepository.class);service=new ProcessedOrderEventService(orders,consumed);}
 private OrderProcessedEvent event(){return new OrderProcessedEvent("evt-2","ORDER_PROCESSED",1,Instant.now(),"c","ord-1","cust","Laptop",1,new BigDecimal("1500"),"PROCESSED");}
 @Test void updatesOwnedOrderAndRecordsConsumedEvent(){Order order=new Order("ord-1","cust","Laptop",1,new BigDecimal("1500"),Instant.now());when(consumed.existsById("evt-2")).thenReturn(false);when(orders.findById("ord-1")).thenReturn(Optional.of(order));service.apply(event());verify(consumed).save(any(ConsumedEvent.class));verify(orders).findById("ord-1");}
 @Test void ignoresDuplicateProcessedEvent(){when(consumed.existsById("evt-2")).thenReturn(true);service.apply(event());verifyNoInteractions(orders);verify(consumed,never()).save(any());}
}
