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

import com.artipie.http.headers.Header;
import com.artipie.http.hm.RsHasBody;
import com.artipie.http.hm.RsHasHeaders;
import com.artipie.http.hm.StatefulResponse;
import java.nio.charset.StandardCharsets;
import javax.json.Json;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link RsJson}.
 *
 * @since 0.16
 */
final class RsJsonTest {

    @Test
    void bodyIsCorrect() {
        MatcherAssert.assertThat(
            new RsJson(Json.createObjectBuilder().add("foo", true)),
            new RsHasBody("{\"foo\":true}", StandardCharsets.UTF_8)
        );
    }

    @Test
    void headersHasContentSize() {
        MatcherAssert.assertThat(
            new StatefulResponse(new RsJson(Json.createObjectBuilder().add("bar", 0))),
            new RsHasHeaders(
                Matchers.anything(),
                Matchers.equalTo(new Header("Content-Length", "9"))
            )
        );
    }

    @Test
    void headersHasContentType() {
        MatcherAssert.assertThat(
            new StatefulResponse(
                new RsJson(
                    () -> Json.createObjectBuilder().add("baz", "a").build(),
                    StandardCharsets.UTF_16BE
                )
            ),
            new RsHasHeaders(
                Matchers.equalTo(
                    new Header("Content-Type", "application/json; charset=UTF-16BE")
                ),
                Matchers.anything()
            )
        );
    }
}
