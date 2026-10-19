package org.example.ksynerx.Presentation.Exception;

import org.example.ksynerx.Application.InvalidProductIdException;
import feign.FeignException;
import feign.RetryableException;
import org.example.ksynerx.Application.ProductNotFoundException;
import org.example.ksynerx.Presentation.Contract.ApiResponse;
import org.example.ksynerx.Presentation.Contract.APIResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleProductNotFound(
            ProductNotFoundException exception
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(
                        APIResultCode.RESOURCE_NOT_FOUND,
                        exception.getMessage()
                ));
    }

    @ExceptionHandler({
            InvalidProductIdException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequest(Exception exception) {
        String message = exception instanceof MethodArgumentTypeMismatchException mismatch
                ? "Tham số '" + mismatch.getName() + "' không hợp lệ"
                : exception.getMessage();

        return ResponseEntity.badRequest()
                .body(ApiResponse.error(APIResultCode.INVALID_REQUEST, message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
        var fieldError = exception.getBindingResult().getFieldError();
        String message = fieldError == null ? "Dữ liệu yêu cầu không hợp lệ"
                : fieldError.isBindingFailure()
                        ? "Tham số '" + fieldError.getField() + "' không hợp lệ"
                        : fieldError.getDefaultMessage();

        return ResponseEntity.badRequest()
                .body(ApiResponse.error(APIResultCode.INVALID_REQUEST, message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableBody(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(APIResultCode.INVALID_REQUEST,
                        "Body JSON không hợp lệ hoặc sai kiểu dữ liệu"));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnsupportedMethod(HttpRequestMethodNotSupportedException exception) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .headers(exception.getHeaders())
                .body(ApiResponse.error(APIResultCode.INVALID_REQUEST,
                        "Phương thức HTTP không được hỗ trợ"));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException exception) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .headers(exception.getHeaders())
                .body(ApiResponse.error(APIResultCode.INVALID_REQUEST,
                        "Content-Type không được hỗ trợ"));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiResponse<Void>> handleInventoryFeign(FeignException exception) {
        if (exception instanceof RetryableException) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.error(APIResultCode.EXTERNAL_SERVICE_UNAVAILABLE,
                            "Không kết nối được Inventory Service"));
        }
        if (exception.status() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(APIResultCode.RESOURCE_NOT_FOUND,
                            "Không tìm thấy sản phẩm trong Inventory Service"));
        }
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponse.error(APIResultCode.UPSTREAM_ERROR,
                        "Inventory Service trả lỗi"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        LOGGER.error("Unexpected error while processing request", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        APIResultCode.INTERNAL_SERVER_ERROR,
                        "Đã xảy ra lỗi trong quá trình xử lý yêu cầu"
                ));
    }
}
