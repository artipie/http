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
package com.artipie.http.slice;

import com.artipie.http.Connection;
import com.artipie.http.Headers;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.rs.RsStatus;
import com.google.common.base.Throwables;
import com.jcabi.log.Logger;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import org.reactivestreams.Publisher;

/**
 * Slice that logs incoming requests and outgoing responses.
 *
 * @since 0.8
 * @checkstyle IllegalCatchCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidCatchingGenericException")
public final class LoggingSlice implements Slice {

    /**
     * Logging level.
     */
    private final Level level;

    /**
     * Delegate slice.
     */
    private final Slice slice;

    /**
     * Ctor.
     *
     * @param slice Slice.
     */
    public LoggingSlice(final Slice slice) {
        this(Level.FINE, slice);
    }

    /**
     * Ctor.
     *
     * @param level Logging level.
     * @param slice Slice.
     */
    public LoggingSlice(final Level level, final Slice slice) {
        this.level = level;
        this.slice = slice;
    }

    @Override
    public Response response(
        final String line,
        final Iterable<Map.Entry<String, String>> headers,
        final Publisher<ByteBuffer> body
    ) {
        final StringBuilder msg = new StringBuilder(">> ").append(line);
        LoggingSlice.append(msg, headers);
        Logger.log(this.level, this.slice, msg.toString());
        return connection -> {
            try {
                return this.slice.response(line, headers, body)
                    .send(new LoggingConnection(connection))
                    .exceptionally(
                        throwable -> {
                            this.log(throwable);
                            Throwables.throwIfUnchecked(throwable);
                            throw new CompletionException(throwable);
                        }
                    );
            } catch (final Exception ex) {
                this.log(ex);
                throw ex;
            }
        };
    }

    /**
     * Writes throwable to logger.
     *
     * @param throwable Throwable to be logged.
     */
    private void log(final Throwable throwable) {
        final StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        Logger.log(this.level, this.slice, "Failure: %s", writer.toString());
    }

    /**
     * Append headers to {@link StringBuilder}.
     *
     * @param builder Target {@link StringBuilder}.
     * @param headers Headers to be appended.
     */
    private static void append(
        final StringBuilder builder,
        final Iterable<Map.Entry<String, String>> headers
    ) {
        for (final Map.Entry<String, String> header : headers) {
            builder.append('\n').append(header.getKey()).append(": ").append(header.getValue());
        }
    }

    /**
     * Connection logging response prior to sending.
     *
     * @since 0.8
     */
    private final class LoggingConnection implements Connection {

        /**
         * Delegate connection.
         */
        private final Connection connection;

        /**
         * Ctor.
         *
         * @param connection Delegate connection.
         */
        private LoggingConnection(final Connection connection) {
            this.connection = connection;
        }

        @Override
        public CompletionStage<Void> accept(
            final RsStatus status,
            final Headers headers,
            final Publisher<ByteBuffer> body
        ) {
            final StringBuilder msg = new StringBuilder("<< ").append(status);
            LoggingSlice.append(msg, headers);
            Logger.log(LoggingSlice.this.level, LoggingSlice.this.slice, msg.toString());
            return this.connection.accept(status, headers, body);
        }
    }
}
