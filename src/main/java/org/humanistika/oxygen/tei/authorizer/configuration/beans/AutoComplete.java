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

import org.humanistika.oxygen.tei.completer.configuration.beans.Dependent;
import org.humanistika.oxygen.tei.completer.configuration.beans.RequestInfo;
import org.humanistika.oxygen.tei.completer.configuration.beans.ResponseAction;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Created by aretter on 07/04/2016.
 */
public class AutoComplete extends org.humanistika.oxygen.tei.completer.configuration.beans.AutoComplete {

    @Nullable
    private final UploadInfo uploadInfo;

    public AutoComplete(final Map<String, String> namespaceBindings, final String context, final String attribute, final Dependent dependent, final String selection, final RequestInfo requestInfo, final ResponseAction responseAction, final UploadInfo uploadInfo) {
        super(namespaceBindings, context, attribute, dependent, selection, requestInfo, responseAction);
        this.uploadInfo = uploadInfo;
    }

    @Nullable
    public UploadInfo getUploadInfo() {
        return uploadInfo;
    }
}
