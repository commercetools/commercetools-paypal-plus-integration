package com.commercetools.http.converter.json;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Optional;

/**
 * Custom Gson response message converter to allow JSON pretty print, if requested.
 * <p>
 * The class extends default Spring {@link GsonHttpMessageConverter} adding {@link #prettyGson} mapper and processing
 * {@link PrettyFormattedBody} instances.
 */
public class PrettyGsonMessageConverter extends GsonHttpMessageConverter {

    /**
     * JSON message converter with configured pretty print options, which is used when a response is expected to be
     * pretty printed.
     */
    private final Gson prettyGson;

    /**
     * @see GsonHttpMessageConverter#jsonPrefix
     */
    private String jsonPrefix;

    /**
     * @param gson       default (minified) JSON mapper. This value is set to {@code super.gson} property.
     * @param prettyGson pretty configure JSON mapper, which is used if the body expected to be pretty printed
     */
    public PrettyGsonMessageConverter(final Gson gson, final Gson prettyGson) {
        super();
        this.setGson(gson);
        this.prettyGson = prettyGson;
    }

    /**
     * Because base {@link GsonHttpMessageConverter#jsonPrefix} is private, but is used in overloaded
     * {@link #writeInternal(Object, Type, HttpOutputMessage)} - we should copy this value.
     *
     * @see GsonHttpMessageConverter#setJsonPrefix(String)
     */
    @Override
    public void setJsonPrefix(String jsonPrefix) {
        super.setJsonPrefix(jsonPrefix);
        this.jsonPrefix = jsonPrefix;
    }

    /**
     * Because base {@link GsonHttpMessageConverter#jsonPrefix} is private, but is used in overloaded
     * {@link #writeInternal(Object, Type, HttpOutputMessage)} - we should copy this value.
     *
     * @see GsonHttpMessageConverter#setPrefixJson(boolean)
     */
    @Override
    public void setPrefixJson(boolean prefixJson) {
        super.setPrefixJson(prefixJson);
        this.jsonPrefix = (prefixJson ? ")]}', " : null);
    }

    /**
     * Allow response JSON pretty print if {@code objectToWrite} is a {@link PrettyFormattedBody} instance with
     * <code>{@link PrettyFormattedBody#isPretty() isPretty} == true</code>.
     *
     * @param objectToWrite if the value is {@link PrettyFormattedBody} instance with
     *                      <code>{@link PrettyFormattedBody#isPretty() isPretty} == true</code> - use
     *                      {@link #prettyGson} for output writing. Otherwise use base
     *                      {@link GsonHttpMessageConverter#writeInternal(Object, Type, HttpOutputMessage)}
     * @param type          the type of object to write (may be {@code null})
     * @param outputMessage the HTTP output message to write to
     * @throws IOException                     in case of I/O errors
     * @throws HttpMessageNotWritableException in case of conversion errors
     */
    @Override
    protected void writeInternal(@Nullable final Object objectToWrite,
                                 @Nullable final Type type,
                                 @Nonnull final HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {

        // based on: if objectToWrite is PrettyFormattedBody && isPretty == true => use custom formatter
        // otherwise - use the default base GsonHttpMessageConverter#writeInternal(Object, Type, HttpOutputMessage)

        Optional<PrettyFormattedBody> prettyFormatted = Optional.ofNullable(objectToWrite)
                .filter(o -> o instanceof PrettyFormattedBody)
                .map(o -> (PrettyFormattedBody) objectToWrite);

        boolean pretty = prettyFormatted.map(PrettyFormattedBody::isPretty).orElse(false);
        Object realObject = prettyFormatted.map(PrettyFormattedBody::getBody).orElse(objectToWrite);

        if (pretty) {
            // this is basically full copy of super.writeInternal(), but with custom (pretty) gson mapper
            Charset charset = getCharset(outputMessage.getHeaders());
            OutputStreamWriter writer = new OutputStreamWriter(outputMessage.getBody(), charset);
            try {
                if (this.jsonPrefix != null) {
                    writer.append(this.jsonPrefix);
                }
                if (type != null) {
                    this.prettyGson.toJson(realObject, type, writer);
                } else {
                    this.prettyGson.toJson(realObject, writer);
                }
                writer.close();
            } catch (JsonIOException ex) {
                throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getMessage(), ex);
            }
        } else {
            // use default writer if isPretty property is not specified
            super.writeInternal(realObject, type, outputMessage);
        }
    }

    /**
     * To ensure the message converter supports {@link PrettyFormattedBody} instances
     *
     * @param clazz response body class
     * @return <b>true</b> if the {@code clazz} is {@link PrettyFormattedBody} or {@code super.supports(clazz) == true}
     */
    @Override
    protected boolean supports(Class<?> clazz) {
        return PrettyFormattedBody.class.equals(clazz) || super.supports(clazz);
    }

    /**
     * Just a copy-paste of {@link GsonHttpMessageConverter#getCharset(HttpHeaders)} because it is private, but used in
     * {@link #writeInternal(Object, Type, HttpOutputMessage)}
     *
     * @param headers output message HTTP headers
     * @return a charset from the {@code headers} content type or {@link GsonHttpMessageConverter#DEFAULT_CHARSET}
     * otherwise.
     */
    private Charset getCharset(HttpHeaders headers) {
        if (headers == null || headers.getContentType() == null || headers.getContentType().getCharset() == null) {
            return DEFAULT_CHARSET;
        }
        return headers.getContentType().getCharset();
    }
}
