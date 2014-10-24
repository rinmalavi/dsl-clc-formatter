package com.dslplatform.compiler.client.formatter;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

public class Code implements Serializable {
    public final File file;
    public final String path;

    public final String body;
    private final byte[] digest;

    public Code(final File file, final String body, final byte[] digest) {
        this.file = file;
        this.path = file.getPath();

        this.body = body;
        this.digest = digest;
    }

    public int hashCode() {
        return body.hashCode();
    }

    public boolean equals(final Object o) {
        if (o == null || !(o instanceof Code)) return false;
        final Code other = (Code) o;

        return Arrays.equals(digest, other.digest) && path.equals(other.path);
    }
}
