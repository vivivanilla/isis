/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.applib.value;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.inject.Named;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.Value;
import org.apache.isis.applib.jaxb.PrimitiveJaxbAdapters;
import org.apache.isis.applib.util.ZipReader;
import org.apache.isis.applib.util.ZipWriter;
import org.apache.isis.commons.internal.base._Bytes;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.image._Images;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Represents a binary large object.
 *
 * <p>
 * Conceptually you can consider it as a set of bytes (a picture, a video etc),
 * though in fact it wraps three pieces of information:
 * </p>
 * <ul>
 *     <li>
 *         the set of bytes
 *     </li>
 *     <li>
 *         a name
 *     </li>
 *     <li>
 *         a mime type
 *     </li>
 * </ul>
 *
 * @see Clob
 * @since 1.x {@index}
 */
@Named(IsisModuleApplib.NAMESPACE + ".value.Blob")
@Value
@XmlJavaTypeAdapter(Blob.JaxbToStringAdapter.class)   // for JAXB view model support
@Log4j2
public final class Blob implements NamedWithMimeType {

    /**
     * Computed for state:
     *
     * <p>
     * <pre>
     * private final MimeType mimeType;
     * private final byte[] bytes;
     * private final String name;
     * </pre>
     * </p>
     */
    private static final long serialVersionUID = 5659679806709601263L;

    // -- FACTORIES

    /**
     * Returns a new {@link Blob} of given {@code name}, {@code mimeType} and {@code content}.
     * <p>
     * {@code name} may or may not include the desired filename extension, it
     * is guaranteed, that the resulting {@link Blob} has the appropriate extension
     * as constraint by the given {@code mimeType}.
     * <p>
     * For more fine-grained control use one of the {@link Blob} constructors directly.
     * @param name - may or may not include the desired filename extension
     * @param mimeType
     * @param content - bytes
     * @return new {@link Blob}
     */
    public static Blob of(final String name, final CommonMimeType mimeType, final byte[] content) {
        val proposedFileExtension = mimeType.getProposedFileExtensions().getFirst().orElse("");
        val fileName = _Strings.asFileNameWithExtension(name, proposedFileExtension);
        return new Blob(fileName, mimeType.getMimeType(), content);
    }

     // --

    private final MimeType mimeType;
    private final byte[] bytes;
    private final String name;

    public Blob(final String name, final String primaryType, final String subtype, final byte[] bytes) {
        this(name, CommonMimeType.newMimeType(primaryType, subtype), bytes);
    }

    public Blob(final String name, final String mimeTypeBase, final byte[] bytes) {
        this(name, CommonMimeType.newMimeType(mimeTypeBase), bytes);
    }

    public Blob(final String name, final MimeType mimeType, final byte[] bytes) {
        if(name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        if(mimeType == null) {
            throw new IllegalArgumentException("MimeType cannot be null");
        }
        if(name.contains(":")) {
            throw new IllegalArgumentException("Name cannot contain ':'");
        }
        if(bytes == null) {
            throw new IllegalArgumentException("Bytes cannot be null");
        }
        this.name = name;
        this.mimeType = mimeType;
        this.bytes = bytes;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public MimeType getMimeType() {
        return mimeType;
    }

    public byte[] getBytes() {
        return bytes;
    }

    // -- UTILITIES

    public Clob toClob(final @NonNull Charset charset) {
        return new Clob(getName(), getMimeType(), _Strings.ofBytes(getBytes(), charset));
    }

    /**
     * Does not close the OutputStream.
     * @param os
     * @throws IOException
     */
    public void writeBytesTo(final OutputStream os) throws IOException {
        if(os==null) {
            return;
        }
        if(bytes!=null) {
            os.write(bytes);
        }
    }

    /**
     * The {@link InputStream} involved is closed after consumption.
     * @param consumer
     * @throws IOException
     */
    public void consume(final Consumer<InputStream> consumer) throws IOException {
     // null to empty
        val bytes = Optional.ofNullable(getBytes())
                .orElse(new byte[0]);
        try(val bis = new ByteArrayInputStream(bytes)) {
            consumer.accept(bis);
        }
    }

    /**
     * The {@link InputStream} involved is closed after digestion.
     * @param <R>
     * @param digester
     * @throws IOException
     */
    public <R> R digest(final @NonNull Function<InputStream, R> digester) throws IOException {
        // null to empty
        val bytes = Optional.ofNullable(getBytes())
                .orElse(new byte[0]);
        try(val bis = new ByteArrayInputStream(bytes)) {
            return digester.apply(bis);
        }
    }

    public Blob zip() {
        val zipWriter = ZipWriter.newInstance();
        zipWriter.nextEntry(getName(), outputStream->outputStream.writeBytes(getBytes()));
        return Blob.of(getName()+".zip", CommonMimeType.ZIP, zipWriter.toBytes());
    }

    @SneakyThrows
    public Blob unZip(final @NonNull CommonMimeType resultingMimeType) {

        return digest(is->
            ZipReader.digest(is, (zipEntry, zipInputStream)->{
                if(zipEntry.isDirectory()) {
                    return (Blob)null; // continue
                }
                final byte[] unzippedBytes;
                try {
                    unzippedBytes = _Bytes.of(zipInputStream);
                } catch (IOException e) {
                    throw _Exceptions
                        .unrecoverable(e, "failed to read zip entry %s", zipEntry.getName());
                }
                return Blob.of(zipEntry.getName(), resultingMimeType, unzippedBytes);
            })

        )
        .orElse(Blob.of("blob_unzip_failed", resultingMimeType, new byte[0]));
    }

    // -- OBJECT CONTRACT

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        final Blob blob = (Blob) o;
        return Objects.equals(mimeType.toString(), blob.mimeType.toString()) &&
                Arrays.equals(bytes, blob.bytes) &&
                Objects.equals(name, blob.name);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(mimeType.toString(), name);
        result = 31 * result + Arrays.hashCode(bytes);
        return result;
    }

    @Override
    public String toString() {
        return getName() + " [" + getMimeType().getBaseType() + "]: " + getBytes().length + " bytes";
    }

    /**
     * (thread-safe)
     * @implNote see also BlobValueSemanticsProvider
     */
    public static final class JaxbToStringAdapter extends XmlAdapter<String, Blob> {

        private final PrimitiveJaxbAdapters.BytesAdapter bytesAdapter = new PrimitiveJaxbAdapters.BytesAdapter(); // thread-safe

        @Override
        public Blob unmarshal(final String data) throws Exception {
            if(data==null) {
                return null;
            }
            final int colonIdx = data.indexOf(':');
            final String name  = data.substring(0, colonIdx);
            final int colon2Idx  = data.indexOf(":", colonIdx+1);
            final String mimeTypeBase = data.substring(colonIdx+1, colon2Idx);
            final String payload = data.substring(colon2Idx+1);
            final byte[] bytes = bytesAdapter.unmarshal(payload);
            try {
                return new Blob(name, new MimeType(mimeTypeBase), bytes);
            } catch (MimeTypeParseException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String marshal(final Blob blob) throws Exception {
            if(blob==null) {
                return null;
            }
            String s = blob.getName() +
                    ':' +
                    blob.getMimeType().getBaseType() +
                    ':' +
                    bytesAdapter.marshal(blob.getBytes());
            return s;
        }

    }

    /**
     * @return optionally the payload as a {@link BufferedImage} based on whether
     * this Blob's MIME type identifies as image and whether the payload is not empty
     */
    public Optional<BufferedImage> asImage() {

        val bytes = getBytes();
        if(bytes == null) {
            return Optional.empty();
        }

        val mimeType = getMimeType();
        if(mimeType == null || !mimeType.getPrimaryType().equals("image")) {
            return Optional.empty();
        }

        try {
            val img = _Images.fromBytes(getBytes());
            return Optional.ofNullable(img);
        } catch (Exception e) {
            log.error("failed to read image data", e);
            return Optional.empty();
        }

    }

}
