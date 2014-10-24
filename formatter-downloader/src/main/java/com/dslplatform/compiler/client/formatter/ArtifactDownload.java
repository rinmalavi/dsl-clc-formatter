package com.dslplatform.compiler.client.formatter;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.Callable;

public class ArtifactDownload {
    final int length;
    final String hexSha1;

    final byte[] sha1;
    final File jarFile;

    final URL url;

    public ArtifactDownload(
            final String baseUrl,
            final String groupId,
            final String artifactId,
            final String version,
            final int length,
            final String hexSha1) {

        this.length = length;
        this.hexSha1 = hexSha1.toLowerCase(Locale.ENGLISH);

        this.sha1 = DatatypeConverter.parseHexBinary(this.hexSha1);

        final String jarName = String.format(
                "%s-%s-[%s].jar",
                artifactId,
                version,
                this.hexSha1);

        this.jarFile = new File(TEMP_DIR, jarName);

        try {
            this.url = new URL(String.format(
                    "%s%s/%s/%s/%3$s-%4$s.jar",
                    baseUrl,
                    groupId.replace('.', '/'),
                    artifactId,
                    version));
        }
        catch (final MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static final File TEMP_DIR = new File(System.getProperty("java.io.tmpdir"), "dsl-clc-formatter");

    public Callable<File> getDownload() {
        return new Downloader();
    }

    private class Downloader implements Callable<File> {
        @Override
        public File call() throws IOException {
            if (!checkCache()) {
                final byte[] body = download();
                persist(jarFile, body);
            }

            return jarFile;
        }

        private boolean checkCache() throws IOException {
            if (!jarFile.isFile()) return false;

            if (length != jarFile.length()) {
                System.out.println("SIZE WRONG");
                jarFile.delete();
                return false;
            }

            final FileInputStream fis = new FileInputStream(jarFile);
            final byte[] body = new byte[length];

            final MessageDigest md = getDigest();

            int offset = 0;
            while (offset < length) {
                final int read = fis.read(body, offset, length - offset);
                if (read == -1) break;
                System.out.println("READ CACHE:" + read);
                md.update(body, offset, read);
                offset += read;
            }

            final byte[] digest = md.digest();
            if (!Arrays.equals(digest, sha1)) {
                System.out.println("CACHED DIGEST DIFFERS for " + url);
                jarFile.delete();
                return false;
            }

            System.out.println("CACHE OK!");
            return true;
        }

        private byte[] download() throws IOException {
            final InputStream is = url.openStream();
            try {
                final byte[] body = new byte[length];
                int offset = 0;
                while(offset < length) {
                    final int read = is.read(body, offset, length - offset);
                    System.out.println(read);
                    if (read == -1) break;
                    offset += read;
                }
                System.out.println("OFFSET IS : " + offset);
                if (offset != length) {
                    throw new IOException(String.format(
                            "Artifact was too small (got %d/%d bytes)",
                            offset,
                            length));
                }

                verifyDownload(body);

                return body;
            } finally {
                is.close();
            }
        }

        private MessageDigest getDigest() {
            try {
                return MessageDigest.getInstance("SHA-1");
            } catch (final NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        private void verifyDownload(final byte[] body) throws IOException {
            final byte[] digest = getDigest().digest(body);

            if (!Arrays.equals(sha1, digest)) {
                throw new IOException(String.format(
                        "Digest mismatch; expected \"%s\" but got \"%s\"",
                        hexSha1,
                        DatatypeConverter.printHexBinary(digest).toLowerCase(Locale.ENGLISH)));
            }

            System.out.println(DatatypeConverter.printHexBinary(digest).toLowerCase(Locale.ENGLISH));
            System.out.println(DatatypeConverter.printHexBinary(sha1).toLowerCase(Locale.ENGLISH));
        }

        private void persist(final File jarFile, final byte[] body) throws IOException {
            if (!TEMP_DIR.isDirectory()) {
                TEMP_DIR.mkdirs();
            }

            final FileOutputStream fos = new FileOutputStream(jarFile);
            try {
                fos.write(body);
            }
            finally {
                fos.close();
            }
        }
    }
}
