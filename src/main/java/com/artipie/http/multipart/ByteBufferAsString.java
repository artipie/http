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

package com.artipie.http.multipart;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * ByteBufferAsString.
 *
 * @since 0.1
 */
public class ByteBufferAsString {
    /**
     * ByteBuffer to build string.
     */
    private final ByteBuffer buffer;

    /**
     * Ctor.
     *
     * @param buffer To create a string.
     */
    public ByteBufferAsString(final ByteBuffer buffer) {
        this.buffer = buffer;
    }

    /**
     * Gets a string value from ByteBuffer.
     *
     * @return String from ByteBuffer.
     */
    public String value() {
        final byte[] bytes;
        if (this.buffer.hasArray()) {
            bytes = this.buffer.array();
        } else {
            bytes = new byte[this.buffer.remaining()];
            this.buffer.get(bytes);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
