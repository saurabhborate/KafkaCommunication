package com.example.orders.notifications.service;
import static org.mockito.Mockito.*;
import com.example.orders.contracts.OrderProcessedEvent;import com.example.orders.notifications.domain.ConsumedEvent;import com.example.orders.notifications.domain.Notification;import com.example.orders.notifications.repository.ConsumedEventRepository;import com.example.orders.notifications.repository.NotificationRepository;
import java.math.BigDecimal;import java.time.Instant;import org.junit.jupiter.api.BeforeEach;import org.junit.jupiter.api.Test;import org.mockito.ArgumentCaptor;import static org.junit.jupiter.api.Assertions.*;
class NotificationEventServiceTest{
 private NotificationRepository notifications;private ConsumedEventRepository consumed;private NotificationEventService service;
 @BeforeEach void setup(){notifications=mock(NotificationRepository.class);consumed=mock(ConsumedEventRepository.class);service=new NotificationEventService(notifications,consumed);when(consumed.existsById(any())).thenReturn(false);}
 private OrderProcessedEvent event(String amount){return new OrderProcessedEvent("evt-1","ORDER_PROCESSED",1,Instant.now(),"c","ord-1","cust","Laptop",1,new BigDecimal(amount),"PROCESSED");}
 @Test void amountAtOrBelowThresholdCompletesPayment(){service.accept(event("5000"));ArgumentCaptor<Notification> captor=ArgumentCaptor.forClass(Notification.class);verify(notifications).save(captor.capture());assertEquals("PAYMENT_COMPLETED",captor.getValue().getPaymentStatus());}
 @Test void amountAboveThresholdLeavesPaymentPending(){service.accept(event("5000.01"));ArgumentCaptor<Notification> captor=ArgumentCaptor.forClass(Notification.class);verify(notifications).save(captor.capture());assertEquals("PAYMENT_PENDING",captor.getValue().getPaymentStatus());}
 @Test void duplicateProcessedEventDoesNotCreateDuplicate(){when(consumed.existsById("evt-1")).thenReturn(true);service.accept(event("120"));verifyNoInteractions(notifications);verify(consumed,never()).save(any(ConsumedEvent.class));}
}
