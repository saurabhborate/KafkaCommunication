package com.example.orders.notifications.api;
import com.example.orders.notifications.repository.NotificationRepository; import java.util.List; import org.springframework.http.HttpStatus; import org.springframework.web.bind.annotation.*; import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/notifications") public class NotificationController{
 private final NotificationRepository repository; public NotificationController(NotificationRepository repository){this.repository=repository;}
 @GetMapping("/{orderId}") public NotificationResponse byOrder(@PathVariable String orderId){return repository.findByOrderId(orderId).map(NotificationResponse::from).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Notification not found"));}
 @GetMapping public List<NotificationResponse> all(){return repository.findAll().stream().map(NotificationResponse::from).toList();}
}
