package com.prewave.prewavetask.core.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.ErrorResponseException

class ProblemDetailException(
    status: HttpStatus,
    title: String,
    detail: String,
    cause: Throwable,
) : ErrorResponseException(
    status,
    asProblemDetail(status = status, title = title, detail = detail),
    cause,
) {
    companion object {
        private fun asProblemDetail(
            status: HttpStatus,
            title: String,
            detail: String,
        ) = ProblemDetail
            .forStatusAndDetail(status, detail)
            .apply { this.title = title }
    }
}