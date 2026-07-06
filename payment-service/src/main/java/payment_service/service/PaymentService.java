package payment_service.service;

import payment_service.dto.PaymentRequest;
import payment_service.dto.PaymentResponse;
import payment_service.entity.Payment;
import payment_service.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final WebClient webClient;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    public PaymentService(PaymentRepository paymentRepository,
                          WebClient.Builder webClientBuilder) {
        this.paymentRepository = paymentRepository;
        this.webClient = webClientBuilder.build();
    }

    @Transactional
    public PaymentResponse createCheckoutSession(PaymentRequest request) {
        if (paymentRepository.existsByOrderId(request.getOrderId())) {
            throw new RuntimeException("Payment already initiated for order: " + request.getOrderId());
        }

        Stripe.apiKey = stripeSecretKey;

        try {
            // Amount in cents (Stripe requires smallest currency unit)
            long amountInCents = request.getAmount()
                    .multiply(java.math.BigDecimal.valueOf(100))
                    .longValue();

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(request.getSuccessUrl() + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(request.getCancelUrl())
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(request.getCurrency() != null
                                                            ? request.getCurrency() : "usd")
                                                    .setUnitAmount(amountInCents)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData
                                                                    .ProductData.builder()
                                                                    .setName("Order #" + request.getOrderId())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .putMetadata("orderId", String.valueOf(request.getOrderId()))
                    .putMetadata("userId",  String.valueOf(request.getUserId()))
                    .build();

            Session session = Session.create(params);

            // Persist pending payment record
            Payment payment = new Payment();
            payment.setOrderId(request.getOrderId());
            payment.setUserId(request.getUserId());
            payment.setAmount(request.getAmount());
            payment.setStatus(Payment.Status.PENDING);
            payment.setStripeSessionId(session.getId());

            paymentRepository.save(payment);

            return toResponse(payment, session.getUrl());

        } catch (StripeException e) {
            throw new RuntimeException("Stripe error: " + e.getMessage());
        }
    }

    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
        return toResponse(payment, null);
    }

    public List<PaymentResponse> getPaymentsByUser(Long userId) {
        return paymentRepository.findByUserId(userId)
                .stream().map(p -> toResponse(p, null)).collect(Collectors.toList());
    }

    @Transactional
    public void handleWebhookSuccess(String stripeSessionId, String stripePaymentId) {
        Payment payment = paymentRepository.findByStripeSessionId(stripeSessionId)
                .orElseThrow(() -> new RuntimeException("Payment not found for session: " + stripeSessionId));

        payment.setStatus(Payment.Status.COMPLETED);
        payment.setStripePaymentId(stripePaymentId);
        paymentRepository.save(payment);

        // Notify order-service to confirm the order
        webClient.put()
                .uri("http://order-service/api/orders/{id}/status?status=CONFIRMED",
                        payment.getOrderId())
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    @Transactional
    public void handleWebhookFailure(String stripeSessionId) {
        Payment payment = paymentRepository.findByStripeSessionId(stripeSessionId)
                .orElseThrow(() -> new RuntimeException("Payment not found for session: " + stripeSessionId));

        payment.setStatus(Payment.Status.FAILED);
        paymentRepository.save(payment);
    }

    private PaymentResponse toResponse(Payment payment, String checkoutUrl) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getStatus().name(),
                payment.getStripeSessionId(),
                checkoutUrl
        );
    }
}
