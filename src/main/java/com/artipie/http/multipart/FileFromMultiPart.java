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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Flow;
import java.util.logging.Logger;
import org.reactivestreams.FlowAdapters;

/**
 * FileFromMultiPart.
 *
 * @since 0.1
 * @checkstyle LocalFinalVariableNameCheck (200 lines).
 * @checkstyle ParameterNameCheck (200 lines).
 * @checkstyle MemberNameCheck (200 lines).
 * @checkstyle ConfusingTernary (200 lines).
 */
public final class FileFromMultiPart {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(FileFromMultiPart.class.getName());

    /**
     * Boundary in MultiPart.
     */
    private final String boundary;

    /**
     * Boundary in MultiPart.
     */
    private final String finalBoundary;

    /**
     * Content in MultiPart.
     */
    private final Flow.Publisher<ByteBuffer> bytes;

    /**
     * Indicates if the current part from multi part is file.
     */
    private final boolean[] isFile;

    /**
     * Name of file in multi part.
     */
    private final String[] fileName;

    /**
     * Indicates if file content is started in the current part.
     */
    private final boolean[] fileStarted;

    /**
     * Indicates if content() method is called.
     */
    private final boolean[] contentCalled;

    /**
     * Ctor.
     *
     * @param boundary Boundary in MultiPart.
     * @param bytes Content in Multipart.
     */
    public FileFromMultiPart(final String boundary, final Flow.Publisher<ByteBuffer> bytes) {
        this.boundary = String.format("--%s", boundary);
        this.finalBoundary = String.format("%s--", this.boundary);
        this.bytes = bytes;
        this.fileName = new String[]{""};
        this.isFile = new boolean[]{false};
        this.fileStarted = new boolean[]{false};
        this.contentCalled = new boolean[]{false};
    }

    /**
     * You can call it only after calling method content.
     *
     * @return Name of the file from the multipart.
     */
    public String name() {
        if (!this.contentCalled[0]) {
            throw new RequiredMethodIsNotCalledException(
                "call the method content(), and only after you can get name()"
            );
        }
        return this.fileName[0];
    }

    /**
     * Returns content of the file in the multi part, it also sets file name
     *  by parsing it from the multipart text.
     *
     * @return Content of multi part.
     */
    public Flowable<ByteBuffer> content() {
        return Flowable.fromPublisher(
            FlowAdapters.toPublisher(this.bytes)
        ).map(this::convert);
    }

    /**
     * Works as a converter in map function for Flowable.
     *
     * @param buffer Current chunk as a ByteBuffer.
     * @return Only ByteBuffer with bytes from file in the multipart.
     */
    private ByteBuffer convert(final ByteBuffer buffer) {
        final String chunkAsString = new ByteBufferAsString(buffer).value();
        final List<String> lines = Arrays.asList(chunkAsString.split("\n"));
        final ByteArrayOutputStream content = new ByteArrayOutputStream();
        lines.forEach(
            line -> {
                if (line.trim().isEmpty() && !this.fileStarted[0]) {
                    if (this.isFile[0]) {
                        this.fileStarted[0] = true;
                    }
                } else if (this.fileStarted[0]) {
                    if (this.boundary.contains(line) || this.finalBoundary.contains(line)) {
                        this.fileStarted[0] = false;
                        this.isFile[0] = false;
                    } else {
                        try {
                            content.write(line.concat("\n").getBytes());
                        } catch (final IOException e) {
                            LOGGER.info(e.getMessage());
                        }
                    }
                } else {
                    final List<String> headerValues = Arrays.asList(line.split(";"));
                    final Optional<String> fileHeader = headerValues.stream()
                        .filter(header -> header.contains("filename=")).findFirst();
                    if (fileHeader.isPresent()) {
                        this.fileName[0] = fileHeader
                            .get().split("filename=\"")[1]
                            .split("\"")[0];
                        this.isFile[0] = true;
                    }
                }
            });
        return ByteBuffer.wrap(content.toByteArray());
    }
}
