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
package org.humanistika.oxygen.tei.authorizer.configuration.beans;

import org.humanistika.oxygen.tei.completer.configuration.beans.Authentication;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Configuration details for uploading a suggestion to a server
 *
 * @author Adam Retter, Evolved Binary Ltd <adam.retter@googlemail.com>
 * @version 1.0
 * @serial 20160405
 */
public class UploadInfo {
    public enum UrlVar {
        USERNAME,
        SUGGESTION,
        DESCRIPTION,
        SELECTION_VALUE,
        DEPENDENT_VALUE;

        public String var() {
            return "$" + camelName();
        }

        public String camelName() {
            return underscoreToCamel(name().toLowerCase());
        }

        private String underscoreToCamel(String str) {
            int idx = -1;
            while((idx = str.indexOf('_')) > -1) {
                str = str.substring(0, idx) + str.substring(idx + 1, idx + 2).toUpperCase() +  str.substring(idx + 2);
            }
            return str;
        }
    }

    public enum Method {
        POST,
        PUT
    }

    private final Method method;
    private final String url;
    @Nullable private final Authentication authentication;
    @Nullable private final BodyInfo bodyInfo;

    public UploadInfo(final Method method, final String url, final Authentication authentication, final BodyInfo bodyInfo) {
        this.method = method;
        this.url = url;
        this.authentication = authentication;
        this.bodyInfo = bodyInfo;
    }

    public URL getUrl(final Map<UrlVar, String> substitutions) throws MalformedURLException {
        String expandedUrl = url;
        if(substitutions != null) {
            for (final Map.Entry<UrlVar, String> substitution : substitutions.entrySet()) {
                expandedUrl = expandedUrl.replace(substitution.getKey().var(), substitution.getValue());
            }
        }
        return new URL(expandedUrl);
    }

    public Method getMethod() {
        return method;
    }

    @Nullable
    public Authentication getAuthentication() {
        return authentication;
    }

    @Nullable
    public BodyInfo getBodyInfo() {
        return bodyInfo;
    }
}
