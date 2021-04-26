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
package com.artipie.http.headers;

import com.artipie.http.rq.RqHeaders;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Accept header, check
 * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept">documentation</a>
 * for more details.
 * @since 0.19
 */
public final class Accept {

    /**
     * Header name.
     */
    public static final String NAME = "Accept";

    /**
     * Headers.
     */
    private final Iterable<Map.Entry<String, String>> headers;

    /**
     * Ctor.
     * @param headers Headers to extract `accept` header from
     */
    public Accept(final Iterable<Map.Entry<String, String>> headers) {
        this.headers = headers;
    }

    /**
     * Parses `Accept` header values, sorts them according to weight and returns in
     * corresponding order.
     * @return Set or the values
     */
    public List<String> values() {
        final Map<String, Float> map = new HashMap<>();
        new RqHeaders(this.headers, Accept.NAME).stream().flatMap(
            val -> Arrays.stream(val.split(", "))
        ).forEach(
            item -> {
                final int index = item.indexOf(";");
                String sub = item;
                final float weight;
                if (index > 0) {
                    sub = item.substring(0, index);
                    // @checkstyle MagicNumberCheck (1 line)
                    weight = Float.parseFloat(item.substring(index + 3));
                } else {
                    weight = 1;
                }
                map.compute(
                    sub, (key, val) -> {
                        float min = weight;
                        if (val != null) {
                            min = Float.min(weight, val);
                        }
                        return min;
                    }
                );
            }
        );
        return map.entrySet().stream()
            .sorted((one, two) -> Float.compare(two.getValue(), one.getValue()))
            .map(Map.Entry::getKey).collect(Collectors.toList());
    }
}
