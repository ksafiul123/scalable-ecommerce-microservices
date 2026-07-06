package order_service.service;

import order_service.dto.OrderItemDTO;
import order_service.dto.OrderRequest;
import order_service.dto.OrderResponse;
import order_service.entity.Order;
import order_service.entity.OrderItem;
import order_service.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;

    public OrderService(OrderRepository orderRepository, WebClient.Builder webClientBuilder) {
        this.orderRepository = orderRepository;
        this.webClient = webClientBuilder.build();
    }

    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Order must contain at least one item");
        }

        // Decrement stock for each item via product-service
        for (OrderItemDTO item : request.getItems()) {
            webClient.put()
                    .uri("http://product-service/api/products/{id}/stock/decrement?qty={qty}",
                            item.getProductId(), item.getQuantity())
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        }

        // Calculate total
        BigDecimal total = request.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Build order entity
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setShippingAddress(request.getShippingAddress());
        order.setTotalAmount(total);
        order.setStatus(Order.Status.PENDING);

        for (OrderItemDTO dto : request.getItems()) {
            OrderItem item = new OrderItem();
            item.setProductId(dto.getProductId());
            item.setQuantity(dto.getQuantity());
            item.setUnitPrice(dto.getUnitPrice());
            item.setOrder(order);
            order.getItems().add(item);
        }

        orderRepository.save(order);
        return toResponse(order);
    }

    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        return toResponse(order);
    }

    public List<OrderResponse> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByStatus(String status) {
        Order.Status orderStatus = Order.Status.valueOf(status.toUpperCase());
        return orderRepository.findByStatus(orderStatus)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        Order.Status newStatus = Order.Status.valueOf(status.toUpperCase());
        order.setStatus(newStatus);
        orderRepository.save(order);
        return toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (order.getStatus() == Order.Status.SHIPPED ||
                order.getStatus() == Order.Status.DELIVERED) {
            throw new RuntimeException("Cannot cancel order with status: " + order.getStatus());
        }

        // Restore stock for each item
        for (OrderItem item : order.getItems()) {
            webClient.put()
                    .uri("http://product-service/api/products/{id}/stock/increment?qty={qty}",
                            item.getProductId(), item.getQuantity())
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        }

        order.setStatus(Order.Status.CANCELLED);
        orderRepository.save(order);
        return toResponse(order);
    }

    // ── Mapper ───────────────────────────────────────────────────────

    private OrderResponse toResponse(Order order) {
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(i -> new OrderItemDTO(
                        i.getId(),
                        i.getProductId(),
                        i.getQuantity(),
                        i.getUnitPrice()))
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                itemDTOs,
                order.getCreatedAt()
        );
    }
}
