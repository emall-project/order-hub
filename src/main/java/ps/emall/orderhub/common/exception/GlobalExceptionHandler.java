package ps.emall.orderhub.common.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ps.emall.orderhub.common.message.MessageKey;
import ps.emall.orderhub.common.message.MessageService;
import ps.emall.orderhub.common.response.EMallsResponseEntity;
import ps.emall.orderhub.common.response.ErrorCode;

import java.util.List;
import java.util.Locale;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {
    private final MessageService messageService;

    @ExceptionHandler(EMallsException.class)
    public ResponseEntity<EMallsResponseEntity<Void>> handleEMallsException(EMallsException ex, Locale locale) {
        log.error("Business exception occurred: {}", ex.getMessage());

        String translatedMessage =
                messageService.getMessage(ex.getMessage(), locale);

        List<ErrorCode> translatedErrors = null;
        if (ex.getErrorCode() != null) {
            translatedErrors = ex.getErrorCode().stream()
                    .map(ec -> new ErrorCode(
                            ec.getField(),
                            messageService.getMessage(ec.getMessage(), locale)
                    ))
                    .toList();
        }

        log.warn(
                "Business exception [{}]: {}",
                ex.getHttpStatus(),
                translatedMessage
        );

        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(
                        EMallsResponseEntity.<Void>builder()
                                .status(ex.getHttpStatus())
                                .message(translatedMessage)
                                .errorCodes(translatedErrors)
                                .data(null)
                                .build()
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<EMallsResponseEntity<Void>> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            Locale locale
    ) {

        log.warn("Validation failed for request. Locale: {}", locale);

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            log.warn(
                    "Validation error - field: '{}', rejected value: '{}', message key: '{}'",
                    error.getField(),
                    error.getRejectedValue(),
                    error.getDefaultMessage()
            );
        });

        List<ErrorCode> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ErrorCode(
                        error.getField(),
                        messageService.getMessage(
                                error.getDefaultMessage(),
                                locale
                        )
                ))
                .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        EMallsResponseEntity.<Void>builder()
                                .status(HttpStatus.BAD_REQUEST)
                                .message(
                                        messageService.getMessage(
                                                MessageKey.HTTP_BAD_REQUEST.getKey(),
                                                locale
                                        )
                                )
                                .errorCodes(errors)
                                .data(null)
                                .build()
                );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<EMallsResponseEntity<Void>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, Locale locale) {

        log.warn("HTTP message not readable: {}", ex.getMessage());

        Throwable cause = ex.getCause();

        // ── Case A: @JsonCreator threw IllegalArgumentException ───────────────
        //
        // Our ImageRatio @JsonCreator builds a clear error message already:
        //   "Invalid image ratio: 'INVALID_RATIO'. Valid options are: 16:9, ..."
        // We extract it from the IAE and surface it directly in errorCodes.
        //
        // NOTE: InvalidFormatException is a subclass of JsonMappingException,
        // so this block MUST check for IAE cause first to avoid Case B
        // accidentally matching here when the IAE is absent.
        //
        if (cause instanceof JsonMappingException jsonMapping
                && !(cause instanceof InvalidFormatException)
                && jsonMapping.getCause() instanceof IllegalArgumentException iae) {

            String fieldName = jsonMapping.getPath().isEmpty()
                    ? "value"
                    : jsonMapping.getPath().get(jsonMapping.getPath().size() - 1).getFieldName();

            String errorDetail = iae.getMessage();

            log.warn("@JsonCreator validation failed — field: '{}', reason: '{}'",
                    fieldName, errorDetail);

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(EMallsResponseEntity.<Void>builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message(messageService.getMessage(
                                    MessageKey.HTTP_BAD_REQUEST.getKey(), locale))
                            .errorCodes(List.of(new ErrorCode(fieldName, errorDetail)))
                            .data(null)
                            .build());
        }

        // ── Case B: Jackson-native type mismatch (no @JsonCreator involved) ──
        //
        // e.g. sending "price": "abc" where BigDecimal is expected.
        //
        if (cause instanceof InvalidFormatException invalidFormat) {

            String fieldName = invalidFormat.getPath().isEmpty()
                    ? "value"
                    : invalidFormat.getPath().get(
                    invalidFormat.getPath().size() - 1).getFieldName();

            String badValue = invalidFormat.getValue() != null
                    ? invalidFormat.getValue().toString()
                    : "unknown";

            String expectedType = invalidFormat.getTargetType() != null
                    ? invalidFormat.getTargetType().getSimpleName()
                    : "unknown";

            String errorDetail = String.format(
                    "Invalid value '%s' for field '%s'. Expected type: %s.",
                    badValue, fieldName, expectedType);

            log.warn("Type mismatch — field: '{}', value: '{}', expected: '{}'",
                    fieldName, badValue, expectedType);

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(EMallsResponseEntity.<Void>builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message(messageService.getMessage(
                                    MessageKey.HTTP_BAD_REQUEST.getKey(), locale))
                            .errorCodes(List.of(new ErrorCode(fieldName, errorDetail)))
                            .data(null)
                            .build());
        }

        // ── Case C: Structurally broken JSON ─────────────────────────────────

        String errorDetail = messageService.getMessage(
                MessageKey.HTTP_MESSAGE_NOT_READABLE.getKey(), locale);

        log.warn("Malformed JSON body received");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(EMallsResponseEntity.<Void>builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .message(errorDetail)
                        .errorCodes(List.of(new ErrorCode("requestBody", errorDetail)))
                        .data(null)
                        .build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<EMallsResponseEntity<Void>> handleConstraintViolation(
            ConstraintViolationException ex,
            Locale locale
    ) {
        log.warn("Constraint violation: {}", ex.getMessage());

        List<ErrorCode> errors = ex.getConstraintViolations()
                .stream()
                .map(violation -> {
                    // Extract just the field name from the full path (e.g. "updateItemQuantity.request.quantity" → "quantity")
                    String fullPath = violation.getPropertyPath().toString();
                    String fieldName = fullPath.contains(".")
                            ? fullPath.substring(fullPath.lastIndexOf('.') + 1)
                            : fullPath;

                    return new ErrorCode(
                            fieldName,
                            messageService.getMessage(violation.getMessage(), locale)
                    );
                })
                .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        EMallsResponseEntity.<Void>builder()
                                .status(HttpStatus.BAD_REQUEST)
                                .message(
                                        messageService.getMessage(
                                                MessageKey.HTTP_BAD_REQUEST.getKey(),
                                                locale
                                        )
                                )
                                .errorCodes(errors)
                                .data(null)
                                .build()
                );
    }

}
