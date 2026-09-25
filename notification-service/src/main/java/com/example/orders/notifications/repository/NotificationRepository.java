package com.example.orders.notifications.repository;
import com.example.orders.notifications.domain.Notification; import java.util.Optional; import org.springframework.data.jpa.repository.JpaRepository;
public interface NotificationRepository extends JpaRepository<Notification,String>{Optional<Notification> findByOrderId(String orderId);}
