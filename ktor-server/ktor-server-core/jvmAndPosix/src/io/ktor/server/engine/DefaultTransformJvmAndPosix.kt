/*
 * Copyright 2014-2026 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.engine

import io.ktor.http.*
import io.ktor.http.cio.*
import io.ktor.http.cio.internals.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.UnsupportedMediaTypeException
import io.ktor.server.request.*
import io.ktor.util.pipeline.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*

@OptIn(InternalAPI::class)
internal actual fun PipelineContext<*, PipelineCall>.multiPartData(rc: ByteReadChannel): MultiPartData {
    val contentType = call.request.header(HttpHeaders.ContentType)
        ?: throw UnsupportedMediaTypeException(null)

    val contentLength = call.request.header(HttpHeaders.ContentLength)?.toLong()

    try {
        return CIOMultipartDataBase(
            coroutineContext + Dispatchers.Unconfined,
            rc,
            contentType,
            contentLength,
            formFieldLimit = call.formFieldLimit
        )
    } catch (_: UnsupportedMediaTypeExceptionCIO) {
        throw UnsupportedMediaTypeException(ContentType.parse(contentType))
    }
}
