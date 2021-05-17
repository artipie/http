/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.hm;

import com.artipie.http.Headers;
import com.artipie.http.Response;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithBody;
import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for {@link RsHasBody}.
 *
 * @since 0.4
 */
class RsHasBodyTest {

    @Test
    void shouldMatchEqualBody() {
        final Response response = connection -> connection.accept(
            RsStatus.OK,
            Headers.EMPTY,
            Flowable.fromArray(
                ByteBuffer.wrap("he".getBytes()),
                ByteBuffer.wrap("ll".getBytes()),
                ByteBuffer.wrap("o".getBytes())
            )
        );
        MatcherAssert.assertThat(
            "Matcher is expected to match response with equal body",
            new RsHasBody("hello".getBytes()).matches(response),
            new IsEqual<>(true)
        );
    }

    @Test
    void shouldNotMatchNotEqualBody() {
        final Response response = connection -> connection.accept(
            RsStatus.OK,
            Headers.EMPTY,
            Flowable.fromArray(ByteBuffer.wrap("1".getBytes()))
        );
        MatcherAssert.assertThat(
            "Matcher is expected not to match response with not equal body",
            new RsHasBody("2".getBytes()).matches(response),
            new IsEqual<>(false)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"data", "chunk1,chunk2"})
    void shouldMatchResponseTwice(final String chunks) {
        final String[] elements = chunks.split(",");
        final byte[] data = String.join("", elements).getBytes();
        final Response response = new RsWithBody(
            Flowable.fromIterable(
                Stream.of(elements)
                    .map(String::getBytes)
                    .map(ByteBuffer::wrap)
                    .collect(Collectors.toList())
            )
        );
        new RsHasBody(data).matches(response);
        MatcherAssert.assertThat(
            new RsHasBody(data).matches(response),
            new IsEqual<>(true)
        );
    }
}
