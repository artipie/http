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
package com.artipie.http;

import com.artipie.http.rs.RsStatus;
import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.concurrent.CompletionStage;
import org.cactoos.list.ListOf;
import org.cactoos.map.MapEntry;

/**
 * HTTP response.
 * @see <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html">RFC2616</a>
 * @since 0.1
 */
public interface Response {

    /**
     * Empty response.
     */
    Response EMPTY = con -> con.accept(
        RsStatus.OK,
        Collections.emptyList(),
        Flowable.empty()
    );

    /**
     * Not found response.
     */
    Response NOT_FOUND = con -> con.accept(
        RsStatus.NOT_FOUND,
        new ListOf<java.util.Map.Entry<String, String>>(
            new MapEntry<>("Content-Type", "application/json")
        ),
        Flowable.fromArray(ByteBuffer.wrap("{\"error\" : \"not found\"}".getBytes()))
    );

    /**
     * Send the response.
     *
     * @param connection Connection to send the response to
     * @return Completion stage for sending response to the connection.
     */
    CompletionStage<Void> send(Connection connection);
}
