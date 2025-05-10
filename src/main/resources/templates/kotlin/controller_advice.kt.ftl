package ${packageName}

import ${dtoPackageName}.ErrorResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.LocalDateTime

@RestControllerAdvice
class RestExceptionControllerAdvice : ResponseEntityExceptionHandler() {

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException(e: EntityNotFoundException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.NOT_FOUND.value(),
            error = e.message ?: "Entity not found"
        )

        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {

        val fieldErrorsMap = mutableMapOf<String, MutableList<String>>()
        ex.bindingResult.fieldErrors.forEach { fieldError ->
                fieldErrorsMap.getOrPut(fieldError.field) { mutableListOf() }
                .add(fieldError.defaultMessage ?: "Invalid input")
        }

        val errorDetail = ErrorResponse(
             timestamp = LocalDateTime.now(),
             status = HttpStatus.BAD_REQUEST.value(),
             error = "Validation failed",
             validationErrors = fieldErrorsMap
        )

        return ResponseEntity(errorDetail, HttpStatus.BAD_REQUEST)
    }
}