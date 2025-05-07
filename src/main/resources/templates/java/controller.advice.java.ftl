package ${packageName};

import ${dtoPackageName}.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionControllerAdvice {

    <#list exceptions?sort as exception>
    @ExceptionHandler(${exception}.class)
    public ResponseEntity<ErrorResponse> handle${exception}(${exception} e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .timestamp(java.time.LocalDateTime.now())
                        .status(HttpStatus.NOT_FOUND.value())
                        .error(e.getMessage())
                        .build());
    }

    </#list>
}
