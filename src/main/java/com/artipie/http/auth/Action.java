/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.auth;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Artipie permission action.
 * @since 0.16
 */
public interface Action {

    /**
     * Action names collection.
     * @return Names of the action
     */
    Collection<String> names();

    /**
     * Action enumeration.
     * @since 0.16
     */
    enum Standard implements Action {

        /**
         * Read artifact.
         */
        READ {
            @Override
            public Collection<String> names() {
                return Arrays.asList("r", "read", "download", "install");
            }
        },

        /**
         * Write artifact.
         */
        WRITE {
            @Override
            public Collection<String> names() {
                return Arrays.asList("w", "write", "publish", "push", "deploy", "upload");
            }
        },

        /**
         * Delete artifact.
         */
        DELETE {
            @Override
            public Collection<String> names() {
                return Arrays.asList("d", "delete");
            }
        };

    }

    /**
     * Instance of {@link Standard} by string.
     * @since 0.16
     */
    final class ByString {

        /**
         * Action string.
         */
        private final String action;

        /**
         * Ctor.
         * @param action String action
         */
        public ByString(final String action) {
            this.action = action;
        }

        /**
         * Returns instance of {@link Standard} by string synonym.
         * @return Instance of {@link Standard}
         */
        public Action get() {
            return Stream.of(Standard.values()).filter(
                item -> item.names().stream().anyMatch(act -> act.equals(this.action))
            ).findFirst().orElseThrow(
                () -> new IllegalArgumentException(
                    String.format("Unrecognized action %s", this.action)
                )
            );
        }

    }

}
