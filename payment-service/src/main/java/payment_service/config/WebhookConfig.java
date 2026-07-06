package payment_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class WebhookConfig {

    // Ensures /webhook receives raw String body unparsed
    // Spring Boot's default StringHttpMessageConverter handles this
    // No special config needed when using @RequestBody String payload
    // This bean adds request logging for debugging webhook calls

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(1000);
        filter.setIncludeHeaders(true);
        return filter;
    }
}
