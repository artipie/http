/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.auth;

import java.util.Arrays;
import java.util.Collection;

/**
 * Permissions combined from other permissions.
 * Combined permissions allow action if any origin permission allows action.
 *
 * @since 0.18
 */
public final class JoinedPermissions implements Permissions {

    /**
     * Origin permissions.
     */
    private final Collection<Permissions> origins;

    /**
     * Ctor.
     *
     * @param origins Origin permissions.
     */
    public JoinedPermissions(final Permissions... origins) {
        this(Arrays.asList(origins));
    }

    /**
     * Ctor.
     *
     * @param origins Origin permissions.
     */
    public JoinedPermissions(final Collection<Permissions> origins) {
        this.origins = origins;
    }

    @Override
    public boolean allowed(final Authentication.User user, final String action) {
        final boolean result;
        if (this.origins.isEmpty()) {
            result = true;
        } else {
            result = this.origins.stream().anyMatch(origin -> origin.allowed(user, action));
        }
        return result;
    }
}
