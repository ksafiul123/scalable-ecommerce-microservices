package payment_service.repository;

import payment_service.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByStripeSessionId(String stripeSessionId);
    List<Payment> findByUserId(Long userId);
    boolean existsByOrderId(Long orderId);
}
