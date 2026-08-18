package br.com.petos.project.security;

import br.com.petos.project.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SecurityErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public void write(HttpServletRequest request, HttpServletResponse response,
                      HttpStatus status, String error, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                error,
                message,
                request.getRequestURI()
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}

