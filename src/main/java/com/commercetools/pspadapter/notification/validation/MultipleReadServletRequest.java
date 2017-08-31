package com.commercetools.pspadapter.notification.validation;

import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.annotation.Nonnull;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * This request wrapper enables multiple reads from request body. This is especially handy
 * in cases when you need to first read the body in filter or interceptor and then map
 * the body to Java object in the controller. This wrapper reads the stream once and then
 * returns its cached version on every next reads.
 * NOTICE: because the whole request body is read and cached, it takes space in RAM.
 * Consider using it only when you have reasonable request body.
 *
 * @see <a href="https://stackoverflow.com/questions/34804205/how-can-i-read-request-body-multiple-times-in-spring-handlermethodargumentresol/34806876#34806876">
 *     Stackoverflow answer
 *     </a>
 */
public class MultipleReadServletRequest extends ContentCachingRequestWrapper {

    public MultipleReadServletRequest(@Nonnull HttpServletRequest request) {
        super(request);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        byte[] contentByteArray = getContentAsByteArray();
        if (contentByteArray == null || contentByteArray.length == 0) {
            // if there's no cached request body yet, call parent method
            // which will read and cache the request body
            return super.getInputStream();
        } else {
            // if there's a cached request body, return a new input stream that
            // will read from the cached request body
            return new ServletInputStream() {
                // How to create servlet input stream:
                // https://stackoverflow.com/questions/30484388/inputstream-to-servletinputstream

                private int lastIndexRetrieved = -1;
                private ReadListener readListener = null;

                @Override
                public boolean isFinished() {
                    return (lastIndexRetrieved == contentByteArray.length - 1);
                }

                @Override
                public boolean isReady() {
                    // This implementation will never block
                    // We also never need to call the readListener from this method, as this method will never return false
                    return isFinished();
                }

                @Override
                public int available() throws IOException {
                    return (contentByteArray.length - lastIndexRetrieved - 1);
                }

                @Override
                public void close() throws IOException {
                    lastIndexRetrieved = contentByteArray.length - 1;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    this.readListener = readListener;
                    if (!isFinished()) {
                        try {
                            readListener.onDataAvailable();
                        } catch (IOException e) {
                            readListener.onError(e);
                        }
                    } else {
                        try {
                            readListener.onAllDataRead();
                        } catch (IOException e) {
                            readListener.onError(e);
                        }
                    }
                }

                @Override
                public int read() throws IOException {
                    int i;
                    if (!isFinished()) {
                        i = contentByteArray[lastIndexRetrieved + 1];
                        lastIndexRetrieved++;
                        if (isFinished() && (readListener != null)) {
                            try {
                                readListener.onAllDataRead();
                            } catch (IOException ex) {
                                readListener.onError(ex);
                                throw ex;
                            }
                        }
                        return i;
                    } else {
                        return -1;
                    }
                }
            };
        }
    }
}
