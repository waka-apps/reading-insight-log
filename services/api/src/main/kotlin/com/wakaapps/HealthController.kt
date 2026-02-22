package com.wakaapps

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/health")
class HealthController {
    @Get(produces = [MediaType.TEXT_PLAIN])
    fun index(): String = HEALTH_OK

    private companion object {
        const val HEALTH_OK = "OK"
    }
}
