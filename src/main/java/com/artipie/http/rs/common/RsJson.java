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
package com.artipie.http.rs.common;

import com.artipie.http.Connection;
import com.artipie.http.Headers;
import com.artipie.http.Response;
import com.artipie.http.headers.ContentLength;
import com.artipie.http.headers.ContentType;
import com.artipie.http.rs.RsStatus;
import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonStructure;

/**
 * Response with Yaml document.
 * @since 0.16
 */
public final class RsJson implements Response {

    /**
     * Json supplier.
     */
    private final Supplier<? extends JsonStructure> json;

    /**
     * Charset encoding.
     */
    private final Charset encoding;

    /**
     * Response from Json structure.
     * @param json Json structure
     */
    public RsJson(final JsonStructure json) {
        this(() -> json);
    }

    /**
     * Json response from builder.
     * @param builder JSON object builder
     */
    public RsJson(final JsonObjectBuilder builder) {
        this(builder::build);
    }

    /**
     * Json response from builder.
     * @param builder JSON array builder
     */
    public RsJson(final JsonArrayBuilder builder) {
        this(builder::build);
    }

    /**
     * Response from Json supplier.
     * @param json Json supplier
     */
    public RsJson(final Supplier<? extends JsonStructure> json) {
        this(json, StandardCharsets.UTF_8);
    }

    /**
     * Response from Json supplier with charset encoding.
     * @param json Json supplier
     * @param encoding Charset encoding
     */
    public RsJson(final Supplier<? extends JsonStructure> json, final Charset encoding) {
        this.json = json;
        this.encoding = encoding;
    }

    @Override
    public CompletionStage<Void> send(final Connection connection) {
        final byte[] bytes = this.json.get().toString().getBytes(this.encoding);
        return connection.accept(
            RsStatus.OK,
            new Headers.From(
                new ContentType(
                    String.format("application/json; charset=%s", this.encoding.displayName())
                ),
                new ContentLength(bytes.length)
            ),
            Flowable.just(ByteBuffer.wrap(bytes))
        );
    }
}
