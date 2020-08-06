/*
 * MIT License
 *
 * Copyright (c) 2020 Artipie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.artipie.http.rs;

import com.artipie.http.Connection;
import com.artipie.http.Headers;
import com.artipie.http.Response;
import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;

/**
 * Standard responses.
 * @since 0.8
 */
public enum StandardRs implements Response {
    /**
     * Empty response.
     */
    EMPTY(con -> con.accept(RsStatus.OK, Headers.EMPTY, Flowable.empty())),
    /**
     * OK 200 response.
     */
    OK(EMPTY),
    /**
     * Not found response.
     */
    NOT_FOUND(new RsWithStatus(RsStatus.NOT_FOUND)),
    /**
     * Not found with json.
     */
    JSON_NOT_FOUND(
        new RsWithBody(
            new RsWithHeaders(
                new RsWithStatus(RsStatus.NOT_FOUND),
                new Headers.From("Content-Type", "application/json")
            ),
            ByteBuffer.wrap("{\"error\" : \"not found\"}".getBytes())
        )
    );

    /**
     * Origin response.
     */
    private final Response origin;

    /**
     * Ctor.
     * @param origin Origin response
     */
    StandardRs(final Response origin) {
        this.origin = origin;
    }

    @Override
    public CompletionStage<Void> send(final Connection connection) {
        return this.origin.send(connection);
    }
}
