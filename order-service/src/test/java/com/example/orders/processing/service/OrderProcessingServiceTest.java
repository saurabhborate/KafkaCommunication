package com.example.orders.processing.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.example.orders.contracts.OrderAcceptedEvent;
import com.example.orders.processing.domain.ConsumedEvent;
import com.example.orders.processing.domain.OrderProcessing;
import com.example.orders.processing.outbox.OutboxEvent;
import com.example.orders.processing.outbox.OutboxEventRepository;
import com.example.orders.processing.repository.ConsumedEventRepository;
import com.example.orders.processing.repository.OrderProcessingRepository;
import java.math.BigDecimal; import java.time.Instant; import java.util.Optional;
import org.junit.jupiter.api.BeforeEach; import org.junit.jupiter.api.Test; import org.mockito.ArgumentCaptor; import tools.jackson.databind.ObjectMapper;

class OrderProcessingServiceTest {
 private OrderProcessingRepository orders; private ConsumedEventRepository consumed; private OutboxEventRepository outbox; private OrderProcessingService service;
 @BeforeEach void setup(){orders=mock(OrderProcessingRepository.class);consumed=mock(ConsumedEventRepository.class);outbox=mock(OutboxEventRepository.class);service=new OrderProcessingService(orders,consumed,outbox,new ObjectMapper());}
 @Test void processesAcceptedOrderAndCreatesProcessedOutbox(){
  var event=new OrderAcceptedEvent("evt-1","ORDER_ACCEPTED",1,Instant.now(),"corr-1","ord-1","c-1","Laptop",1,new BigDecimal("1500"));
  when(consumed.existsById("evt-1")).thenReturn(false);when(orders.existsById("ord-1")).thenReturn(false);
  when(orders.save(any())).thenAnswer(inv->{OrderProcessing o=inv.getArgument(0);o.processed(Instant.now());return o;});
  service.process(event);
  verify(consumed).save(any(ConsumedEvent.class));verify(outbox).save(any(OutboxEvent.class));
 }
 @Test void duplicateAcceptedEventDoesNotCreateAnotherOrderOrOutbox(){
  var event=new OrderAcceptedEvent("evt-1","ORDER_ACCEPTED",1,Instant.now(),"corr-1","ord-1","c-1","Laptop",1,new BigDecimal("1500"));
  when(consumed.existsById("evt-1")).thenReturn(true);service.process(event);
  verifyNoInteractions(orders,outbox);verify(consumed,never()).save(any());
 }
}
