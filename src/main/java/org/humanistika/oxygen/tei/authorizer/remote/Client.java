/**
 * TEI Authorizer
 * An Oxygen XML Editor plugin for customizable attribute and value completion and/or creation for TEI P5 documents
 * Copyright (C) 2016 Belgrade Center for Digital Humanities
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.humanistika.oxygen.tei.authorizer.remote;

import org.humanistika.oxygen.tei.authorizer.SuggestedAutocomplete;
import org.humanistika.oxygen.tei.authorizer.configuration.beans.UploadInfo;
import javax.annotation.Nullable;

import java.util.List;

/**
 * TEI Authorizer Client interface extends the TEI Completer Client interface
 * for getting auto-complete suggestions from a remote server
 * with the ability to upload a suggestion to the server
 *
 * @author Adam Retter, Evolved Binary Ltd
 * @version 1.0
 * @serial 20160405
 */
public interface Client extends org.humanistika.oxygen.tei.completer.remote.Client {

    class SuggestionResponse {
        private final boolean success;
        @Nullable private final String message;

        public SuggestionResponse(boolean success, @Nullable String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        @Nullable public String getMessage() {
            return message;
        }
    }

    /**
     * Upload an autocomplete suggestion to the server
     *
     * @param uploadInfo The base details for the upload
     * @param suggestion The suggestion
     * @param description The description of the suggestion or null
     * @param selectionValue The value of the selection or null
     * @param dependentValue The value of the dependent or null
     * @param userValues The user specified values or null
     *
     * @return A response to the suggestion from the server
     */
    SuggestionResponse uploadSuggestion(final UploadInfo uploadInfo, final String suggestion, @Nullable final String description, @Nullable final String selectionValue, @Nullable final String dependentValue, @Nullable final List<SuggestedAutocomplete.UserValue> userValues);
}

