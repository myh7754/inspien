package co.inspien.assignment.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class MonitoringFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(MonitoringFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        MDC.put("requestId", UUID.randomUUID().toString().substring(0, 8));
        long startTime = System.currentTimeMillis();

        try {
            chain.doFilter(request, response);
        } finally {
            long elapsed = System.currentTimeMillis() - startTime;
            String status = response.getStatus() < 400 ? "SUCCESS" : "FAIL";
            log.info("{} {} {} {}ms",
                    status,
                    request.getMethod(),
                    request.getRequestURI(),
                    elapsed);
            MDC.clear();
        }
    }
}
