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

import com.artipie.asto.Content;
import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.http.hm.ResponseMatcher;
import com.artipie.http.rq.RequestLine;
import io.reactivex.Flowable;
import java.util.Collections;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SliceList}.
 *
 * @since 0.10
 * @todo #158:30min Implement SliceList
 *  Implement slice list, which receives a key and return a list of values
 *  found in repository bound to that key, printing result to an HTML. Then
 *  enable the test below and return coverage missed classes values to 15.
 */
public class SliceListTest {

    /**
    * String value for root.
     */
    private static final String COM = "com";

    /**
     * String value for first level.
     */
    private static final String ARTIPIE = "artipie";

    @Test
    @Disabled
    void returnsList() {
        final Storage storage = new InMemoryStorage();
        storage.save(
            new Key.From(SliceListTest.COM, SliceListTest.ARTIPIE, "FileOne.txt"),
            new Content.From("File One Content".getBytes())
        ).join();
        storage.save(
            new Key.From(SliceListTest.COM, SliceListTest.ARTIPIE, "FileTwo.txt"),
            new Content.From("File Two Content".getBytes())
        ).join();
        storage.save(
            new Key.From(SliceListTest.COM, SliceListTest.ARTIPIE, "FileThree.txt"),
            new Content.From("File Three Content".getBytes())
        ).join();
        storage.save(
            new Key.From("other", "path", "FileFour.txt"),
            new Content.From("File Four Content".getBytes())
        ).join();
        MatcherAssert.assertThat(
            new SliceList(
                storage,
                new Key.From(SliceListTest.COM, SliceListTest.ARTIPIE)
            ).response(
                new RequestLine("GET", "list", "HTTP/1.1").toString(),
                Collections.emptyList(),
                Flowable.empty()
            ),
            new ResponseMatcher(
                //@checkstyle LineLengthCheck (1 line)
                "<html><body><ul><li>FileOne.txt</li><li>FileTwo.txt</li><li>FileThree.txt</li></ul></body></html>".getBytes()
            )
        );
    }
}
