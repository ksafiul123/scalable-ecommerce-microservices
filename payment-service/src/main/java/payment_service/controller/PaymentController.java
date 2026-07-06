package payment_service.controller;

import payment_service.dto.PaymentRequest;
import payment_service.dto.PaymentResponse;
import payment_service.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> createCheckout(@RequestBody PaymentRequest request) {
        try {
            PaymentResponse response = paymentService.createCheckoutSession(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getByOrderId(@PathVariable Long orderId) {
        try {
            return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentService.getPaymentsByUser(userId));
    }

    // ── Stripe Webhook ───────────────────────────────────────────────
    // Must receive raw request body — do NOT use @RequestBody (parsed JSON)
    @PostMapping(value = "/webhook", consumes = "application/json")
    public ResponseEntity<?> handleWebhook(
            @RequestHeader("Stripe-Signature") String sigHeader,
            @RequestBody String payload) {

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid Stripe signature"));
        }

        switch (event.getType()) {

            case "checkout.session.completed": {
                Session session = (Session) event.getDataObjectDeserializer()
                        .getObject()
                        .orElseThrow(() -> new RuntimeException("Cannot deserialize session"));

                String stripeSessionId  = session.getId();
                String stripePaymentId  = session.getPaymentIntent();

                paymentService.handleWebhookSuccess(stripeSessionId, stripePaymentId);
                break;
            }

            case "checkout.session.expired": {
                Session session = (Session) event.getDataObjectDeserializer()
                        .getObject()
                        .orElseThrow(() -> new RuntimeException("Cannot deserialize session"));

                paymentService.handleWebhookFailure(session.getId());
                break;
            }

            default:
                // Ignore unhandled event types
                break;
        }

        return ResponseEntity.ok(Map.of("received", true));
    }
}
