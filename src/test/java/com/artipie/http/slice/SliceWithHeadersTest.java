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

import com.artipie.http.Headers;
import com.artipie.http.headers.Header;
import com.artipie.http.hm.RsHasHeaders;
import com.artipie.http.rs.RsWithHeaders;
import com.artipie.http.rs.StandardRs;
import io.reactivex.Flowable;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link SliceWithHeaders}.
 * @since 0.9
 */
class SliceWithHeadersTest {

    @Test
    void addsHeaders() {
        final String header = "Content-type";
        final String value = "text/plain";
        MatcherAssert.assertThat(
            new SliceWithHeaders(
                new SliceSimple(StandardRs.EMPTY), new Headers.From(header, value)
            ).response("GET /some/text HTTP/1.1", Headers.EMPTY, Flowable.empty()),
            new RsHasHeaders(new Header(header, value))
        );
    }

    @Test
    void addsHeaderToAlreadyExistingHeaders() {
        final String hone = "Keep-alive";
        final String vone = "true";
        final String htwo = "Authorization";
        final String vtwo = "123";
        MatcherAssert.assertThat(
            new SliceWithHeaders(
                new SliceSimple(
                    new RsWithHeaders(StandardRs.EMPTY, hone, vone)
                ), new Headers.From(htwo, vtwo)
            ).response("GET /any/text HTTP/1.1", Headers.EMPTY, Flowable.empty()),
            new RsHasHeaders(
                new Header(hone, vone), new Header(htwo, vtwo)
            )
        );
    }

}
