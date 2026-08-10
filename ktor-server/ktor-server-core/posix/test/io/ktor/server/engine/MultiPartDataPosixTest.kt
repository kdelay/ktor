/*
 * Copyright 2014-2026 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.engine

import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlin.test.Test
import kotlin.test.assertEquals

class MultiPartDataPosixTest {

    @Test
    fun testReceiveMultipart() = testApplication {
        routing {
            post("/upload") {
                val parts = StringBuilder()
                call.receiveMultipart().forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> parts.append("form:${part.name}=${part.value};")
                        is PartData.FileItem -> parts.append(
                            "file:${part.originalFileName}=${part.provider().readRemaining().readText()};"
                        )

                        else -> parts.append("other:${part.name};")
                    }
                    part.release()
                }
                call.respondText(parts.toString())
            }
        }

        val response = client.post("/upload") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("title", "native")
                        append(
                            "content",
                            "file body",
                            Headers.build {
                                append(HttpHeaders.ContentDisposition, "filename=\"file.txt\"")
                            }
                        )
                    }
                )
            )
        }

        assertEquals("form:title=native;file:file.txt=file body;", response.bodyAsText())
    }

    @Test
    fun testReceiveParametersFromMultipart() = testApplication {
        routing {
            post("/form") {
                call.respondText(call.receiveParameters()["title"] ?: "missing")
            }
        }

        val response = client.post("/form") {
            setBody(MultiPartFormDataContent(formData { append("title", "native") }))
        }

        assertEquals("native", response.bodyAsText())
    }
}
