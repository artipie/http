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
package com.artipie.http.servlet;

import com.artipie.http.Headers;
import com.artipie.http.Slice;
import com.artipie.http.misc.RandomFreePort;
import com.artipie.http.rq.RqHeaders;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithBody;
import com.artipie.http.rs.RsWithHeaders;
import com.artipie.http.rs.RsWithStatus;
import com.artipie.http.rs.StandardRs;
import com.artipie.http.rs.common.RsText;
import com.artipie.http.slice.SliceSimple;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import javax.servlet.GenericServlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

/**
 * Integration test for servlet slice wrapper.
 * @since 0.19
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@EnabledForJreRange(min = JRE.JAVA_11, disabledReason = "HTTP client is not supported prior JRE_11")
final class ServletWrapITCase {

    /**
     * Jetty server.
     */
    private Server server;

    /**
     * Request builder.
     */
    private HttpRequest.Builder req;

    @BeforeEach
    void setUp() throws Exception {
        final int port = new RandomFreePort().get();
        this.server = new Server(port);
        this.req = HttpRequest.newBuilder(URI.create(String.format("http://localhost:%d", port)));
    }

    @AfterEach
    void tearDown() throws Exception {
        this.server.stop();
    }

    @Test
    void simpleSliceTest() throws Exception {
        final String text = "Hello servlet";
        this.start(new SliceSimple(new RsText(text)));
        final String body = HttpClient.newHttpClient().send(
            this.req.copy().GET().build(),
            HttpResponse.BodyHandlers.ofString()
        ).body();
        MatcherAssert.assertThat(body, new IsEqual<>(text));
    }

    @Test
    void echoSliceTest() throws Exception {
        this.start((line, headers, body) -> new RsWithBody(body));
        final String test = "Ping";
        final String body = HttpClient.newHttpClient().send(
            this.req.copy().PUT(HttpRequest.BodyPublishers.ofString(test)).build(),
            HttpResponse.BodyHandlers.ofString()
        ).body();
        MatcherAssert.assertThat(body, new IsEqual<>(test));
    }

    @Test
    void parsesHeaders() throws Exception {
        this.start(
            (line, headers, body) -> new RsWithHeaders(
                StandardRs.OK,
                new Headers.From("RsHeader", new RqHeaders(headers, "RqHeader").get(0))
            )
        );
        final String value = "some-header";
        final List<String> rsh = HttpClient.newHttpClient().send(
            this.req.copy().GET().header("RqHeader", value).build(),
            HttpResponse.BodyHandlers.discarding()
        ).headers().allValues("RsHeader");
        MatcherAssert.assertThat(
            rsh, Matchers.contains(value)
        );
    }

    @Test
    void returnsStatusCode() throws Exception {
        this.start(new SliceSimple(new RsWithStatus(RsStatus.NO_CONTENT)));
        final int status = HttpClient.newHttpClient().send(
            this.req.copy().GET().build(), HttpResponse.BodyHandlers.discarding()
        ).statusCode();
        // @checkstyle MagicNumberCheck (1 line)
        MatcherAssert.assertThat(status, new IsEqual<>(204));
    }

    /**
     * Start Jetty server with slice back-end.
     * @param slice Back-end
     * @throws Exception on server error
     */
    private void start(final Slice slice) throws Exception {
        final ServletContextHandler context = new ServletContextHandler();
        final ServletHolder holder = new ServletHolder(new SliceServlet(slice));
        holder.setAsyncSupported(true);
        context.addServlet(holder, "/");
        this.server.setHandler(context);
        this.server.start();
    }

    /**
     * Servlet implementation with slice back-end.
     * @since 0.19
     */
    private static final class SliceServlet extends GenericServlet {

        static final long serialVersionUID = 0L;

        /**
         * Slice back-end.
         */
        private final Slice target;

        /**
         * New servlet for slice.
         * @param target Slice
         */
        SliceServlet(final Slice target) {
            this.target = target;
        }

        @Override
        public void service(final ServletRequest req,  final ServletResponse rsp) {
            new ServletSliceWrap(this.target).handle(req.startAsync());
        }
    }
}