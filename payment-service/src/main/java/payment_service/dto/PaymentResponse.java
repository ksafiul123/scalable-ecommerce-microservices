package payment_service.dto;

import java.math.BigDecimal;

public class PaymentResponse {
    private Long id;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String status;
    private String stripeSessionId;
    private String checkoutUrl;

    public PaymentResponse() {}

    public PaymentResponse(Long id, Long orderId, Long userId, BigDecimal amount,
                           String status, String stripeSessionId, String checkoutUrl) {
        this.id              = id;
        this.orderId         = orderId;
        this.userId          = userId;
        this.amount          = amount;
        this.status          = status;
        this.stripeSessionId = stripeSessionId;
        this.checkoutUrl     = checkoutUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStripeSessionId() { return stripeSessionId; }
    public void setStripeSessionId(String stripeSessionId) { this.stripeSessionId = stripeSessionId; }

    public String getCheckoutUrl() { return checkoutUrl; }
    public void setCheckoutUrl(String checkoutUrl) { this.checkoutUrl = checkoutUrl; }
}
