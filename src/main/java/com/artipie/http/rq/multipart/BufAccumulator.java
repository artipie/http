/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/npm-adapter/LICENSE.txt
 */
package com.artipie.http.rq.multipart;

import java.io.Closeable;
import java.nio.ByteBuffer;

/**
 * ByteBuffer accumulator.
 * @since 1.0
 */
final class BufAccumulator implements Closeable {

    /**
     * Buffer.
     */
    private ByteBuffer buffer;

    /**
     * Create buffer with initial capacity.
     * @param cap Initial capacity
     */
    BufAccumulator(final int cap) {
        this.buffer = BufAccumulator.newEmptyBuffer(cap);
    }

    /**
     * Push next chunk of data to accumulator.
     * @param chunk Next buffer
     * @return Self
     */
    public BufAccumulator push(final ByteBuffer chunk) {
        this.check();
        if (this.buffer.capacity() - this.buffer.limit() >= chunk.remaining()) {
            this.buffer.limit(this.buffer.limit() + chunk.remaining());
            this.buffer.put(chunk);
        } else {
            final int cap = Math.max(this.buffer.capacity(), chunk.capacity()) * 2;
            final ByteBuffer resized = ByteBuffer.allocate(cap);
            final int pos = this.buffer.position();
            final int lim = this.buffer.limit();
            this.buffer.flip();
            resized.put(this.buffer);
            resized.limit(lim + chunk.remaining());
            resized.position(pos);
            resized.put(chunk);
            this.buffer = resized;
        }
        return this;
    }

    /**
     * Copy buffer range to another buffer.
     *
     * @param pos Buffer position
     * @param lim Buffer limit
     * @return Readonly copy
     */
    public ByteBuffer copyRange(final int pos, final int lim) {
        final ByteBuffer src = this.duplicate();
        src.limit(lim);
        src.position(pos);
        final ByteBuffer slice = src.slice();
        final ByteBuffer res = ByteBuffer.allocate(slice.remaining());
        res.put(slice);
        res.flip();
        return res.asReadOnlyBuffer();
    }

    /**
     * Drop first n bytes.
     * <p>
     * This operation may change the duplicated buffers or other references to this buffer.
     * </p>
     * @param size How many bytes to drop
     */
    public void drop(final int size) {
        this.buffer.position(size);
        this.buffer.compact();
        this.buffer.limit(this.buffer.position());
    }

    /**
     * Get a duplicate of the buffer.
     * <p>
     * It uses same shared memory as origin buffer but creates new
     * position and limit parameters.
     * </p>
     * @return Duplciated buffer
     */
    public ByteBuffer duplicate() {
        this.check();
        return this.buffer.duplicate();
    }

    /**
     * Get byte array.
     * @return Byte array from accumulator starting from the beginning to limit.
     */
    public byte[] array() {
        final ByteBuffer dup = this.duplicate();
        dup.rewind();
        final byte[] res = new byte[dup.remaining()];
        dup.get(res);
        return res;
    }

    @Override
    @SuppressWarnings("PMD.NullAssignment")
    public void close() {
        this.check();
        // @checkstyle MethodBodyCommentsCheck (1 lines)
        // assign to null means broken state, it's verified by `check` method.
        this.buffer = null;
    }

    /**
     * Sanity check. Works with assertions flag enabled only.
     */
    private void check() {
        assert this.buffer != null : "tokenizer was closed";
    }

    /**
     * Creates new empty buffer with zero position and limit.
     *
     * @param cap Capacity
     * @return New buffer
     */
    private static ByteBuffer newEmptyBuffer(final int cap) {
        final ByteBuffer buf = ByteBuffer.allocate(cap);
        buf.flip();
        return buf;
    }
}
