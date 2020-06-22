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
package com.artipie.http.group;

import com.artipie.http.Response;
import com.artipie.http.Slice;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.reactivestreams.Publisher;

/**
 * Standard group {@link Slice} implementation.
 *
 * @since 0.11
 */
public final class GroupSlice implements Slice {

    /**
     * Target slices.
     */
    private final List<Slice> targets;

    /**
     * New group slice.
     * @param targets Slices to group
     */
    public GroupSlice(final Slice... targets) {
        this(Arrays.asList(targets));
    }

    /**
     * New group slice.
     * @param targets Slices to group
     */
    public GroupSlice(final List<Slice> targets) {
        this.targets = Collections.unmodifiableList(targets);
    }

    @Override
    public Response response(final String line, final Iterable<Map.Entry<String, String>> headers,
        final Publisher<ByteBuffer> body) {
        return new GroupResponse(
            this.targets.stream()
                .map(slice -> slice.response(line, headers, body))
                .collect(Collectors.toList())
        );
    }
}
