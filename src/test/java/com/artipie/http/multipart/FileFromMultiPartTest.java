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

import io.reactivex.Flowable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Flow;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.reactivestreams.FlowAdapters;

/**
 * FileFromMultiPartTest.
 *
 * @since 0.1
 * @checkstyle LocalFinalVariableNameCheck (200 lines).
 * @checkstyle ParameterNameCheck (200 lines).
 * @checkstyle AppendCharacterWithChar (200 lines).
 */
public class FileFromMultiPartTest {
    /**
     * Simple test.
     */
    @Test
    public void simple() throws IOException {
        final Flow.Publisher<ByteBuffer> bytes = FlowAdapters.toFlowPublisher(
            Flowable.fromArray(
                ByteBuffer.wrap(
                    this.dataByPath("/multipart/part1.txt").getBytes()
                ),
                ByteBuffer.wrap(
                    this.dataByPath("/multipart/part2.txt").getBytes()
                ),
                ByteBuffer.wrap(
                    this.dataByPath("/multipart/part3.txt").getBytes()
                ),
                ByteBuffer.wrap(
                    this.dataByPath("/multipart/part4.txt").getBytes()
                ),
                ByteBuffer.wrap(
                    this.dataByPath("/multipart/part5.txt").getBytes()
                ),
                ByteBuffer.wrap(
                    this.dataByPath("/multipart/part6.txt").getBytes()
                ),
                ByteBuffer.wrap(
                    this.dataByPath("/multipart/part7.txt").getBytes()
                )
            )
        );
        final FileFromMultiPart file = new FileFromMultiPart(
            "cf57d222ad2d4807b829801bc561a5e1", bytes
        );
        file.content().toList().subscribe(
            byteBufferList -> {
                final StringBuilder fileContent = new StringBuilder();
                byteBufferList.forEach(
                    byteBuffer -> fileContent.append(new ByteBufferAsString(byteBuffer).value())
                );
                MatcherAssert.assertThat(
                    fileContent.toString(),
                    Matchers.equalTo(
                        this.dataByPath("/multipart/file-from-multipart.txt")
                    )
                );
            });
    }

    /**
     * Returns content of the file as a String.
     *
     * @param path Path of the file with the path.
     * @return Content of the file.
     * @throws IOException IOException.
     */
    private String dataByPath(final String path) throws IOException {
        return IOUtils.toString(
            this.getClass().getResourceAsStream(path),
            "UTF-8"
        ).concat("\n");
    }
}
