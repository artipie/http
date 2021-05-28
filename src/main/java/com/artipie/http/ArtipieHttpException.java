/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http;

import com.artipie.ArtipieException;
import com.artipie.http.rs.RsStatus;
import java.util.Collections;
import java.util.Map;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;

/**
 * Base HTTP exception for Artipie endpoints.
 * @since 1.0
 */
@SuppressWarnings("PMD.OnlyOneConstructorShouldDoInitialization")
public final class ArtipieHttpException extends ArtipieException {

    private static final long serialVersionUID = -16695752893817954L;

    /**
     * HTTP error codes reasons map.
     */
    private static final Map<String, String> MEANINGS = Collections.unmodifiableMap(
        new MapOf<String, String>(
            new MapEntry<>("400", "Bad request"),
            new MapEntry<>("401", "Unauthorized"),
            new MapEntry<>("402", "Payment Required"),
            new MapEntry<>("403", "Forbidden"),
            new MapEntry<>("404", "Not Found"),
            new MapEntry<>("405", "Method Not Allowed"),
            new MapEntry<>("406", "Not Acceptable"),
            new MapEntry<>("407", "Proxy Authentication Required"),
            new MapEntry<>("408", "Request Timeout"),
            new MapEntry<>("409", "Conflict"),
            new MapEntry<>("410", "Gone"),
            new MapEntry<>("411", "Length Required"),
            new MapEntry<>("412", "Precondition Failed"),
            new MapEntry<>("413", "Payload Too Large"),
            new MapEntry<>("414", "URI Too Long"),
            new MapEntry<>("415", "Unsupported Media Type"),
            new MapEntry<>("416", "Range Not Satisfiable"),
            new MapEntry<>("417", "Expectation Failed"),
            new MapEntry<>("418", "I'm a teapot"),
            new MapEntry<>("421", "Misdirected Request"),
            new MapEntry<>("422", "Unprocessable Entity (WebDAV)"),
            new MapEntry<>("423", "Locked (WebDAV)"),
            new MapEntry<>("424", "Failed Dependency (WebDAV)"),
            new MapEntry<>("425", "Too Early"),
            new MapEntry<>("426", "Upgrade Required"),
            new MapEntry<>("428", "Precondition Required"),
            new MapEntry<>("429", "Too Many Requests"),
            new MapEntry<>("431", "Request Header Fields Too Large"),
            new MapEntry<>("451", "Unavailable For Legal Reasons"),
            new MapEntry<>("500", "Internal Server Error"),
            new MapEntry<>("501", "Not Implemented")
        )
    );

    /**
     * HTTP status code for error.
     */
    private final RsStatus code;

    /**
     * New HTTP error exception.
     * @param status HTTP status code
     */
    public ArtipieHttpException(final RsStatus status) {
        this(status, ArtipieHttpException.meaning(status));
    }

    /**
     * New HTTP error exception.
     * @param status HTTP status code
     * @param cause Of the error
     */
    public ArtipieHttpException(final RsStatus status, final Throwable cause) {
        this(status, ArtipieHttpException.meaning(status), cause);
    }

    /**
     * New HTTP error exception with custom message.
     * @param status HTTP status code
     * @param message HTTP status meaning
     */
    public ArtipieHttpException(final RsStatus status, final String message) {
        super(message);
        this.code = status;
    }

    /**
     * New HTTP error exception with custom message and cause error.
     * @param status HTTP status code
     * @param message HTTP status meaning
     * @param cause Of the error
     */
    public ArtipieHttpException(final RsStatus status, final String message,
        final Throwable cause) {
        super(message, cause);
        this.code = status;
    }

    /**
     * Status code.
     * @return RsStatus
     */
    public RsStatus status() {
        return this.code;
    }

    /**
     * The meaning of error code.
     * @param status HTTP status code for error
     * @return Meaning string for this code
     */
    private static String meaning(final RsStatus status) {
        return ArtipieHttpException.MEANINGS.getOrDefault(status.code(), "Unknown");
    }
}
