package ${packageName}

import java.time.LocalDateTime

data class ErrorResponse(
    val timestamp: LocalDateTime? = null,
    val status: Int? = null,
    val error: String? = null,
    val validationErrors: Map<String, List<String>>? = null
)
