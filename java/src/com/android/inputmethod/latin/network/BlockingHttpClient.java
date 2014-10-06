/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.inputmethod.latin.network;

import com.android.inputmethod.annotations.UsedForTesting;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A client for executing HTTP requests synchronously.
 * This must never be called from the main thread.
 *
 * TODO: Remove @UsedForTesting after this is actually used.
 */
@UsedForTesting
public class BlockingHttpClient {
    private final HttpURLConnection mConnection;

    /**
     * Interface that handles processing the response for a request.
     */
    public interface ResponseProcessor {
        /**
         * Called when the HTTP request fails with an error.
         *
         * @param httpStatusCode The status code of the HTTP response.
         * @param message The HTTP response message, if any, or null.
         */
        void onError(int httpStatusCode, @Nullable String message);

        /**
         * Called when the HTTP request finishes successfully.
         * The {@link InputStream} is closed by the client after the method finishes,
         * so any processing must be done in this method itself.
         *
         * @param response An input stream that can be used to read the HTTP response.
         */
        void onSuccess(InputStream response);
    }

    /**
     * TODO: Remove @UsedForTesting after this is actually used.
     */
    @UsedForTesting
    public BlockingHttpClient(HttpURLConnection connection) {
        mConnection = connection;
    }

    /**
     * Executes the request on the underlying {@link HttpURLConnection}.
     *
     * TODO: Remove @UsedForTesting after this is actually used.
     *
     * @param request The request payload, if any, or null.
     * @param responeProcessor A processor for the HTTP response.
     */
    @UsedForTesting
    public void execute(@Nullable byte[] request, @Nonnull ResponseProcessor responseProcessor)
            throws IOException {
        try {
            if (request != null) {
                OutputStream out = new BufferedOutputStream(mConnection.getOutputStream());
                out.write(request);
                out.flush();
                out.close();
            }

            final int responseCode = mConnection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                responseProcessor.onError(responseCode, mConnection.getResponseMessage());
            } else {
                responseProcessor.onSuccess(mConnection.getInputStream());
            }
        } finally {
            mConnection.disconnect();
        }
    }
}