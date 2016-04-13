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

import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

 /**
 * Configuration details for uploading a suggestion body to a server
 *
 * @author Adam Retter, Evolved Binary Ltd <adam.retter@googlemail.com>
 * @version 1.0
 * @serial 20160126
 */
public class BodyInfo {

     public enum BodyType {
        XML,
        JSON,
        FORM
    }

    public enum Encoding {
        GZIP,
        NONE
    }

    private final BodyType bodyType;
    private final Encoding encoding;
    private final boolean includeSelection;
    private final boolean includeDependent;
    @Nullable private final Path transformation;

    public BodyInfo(final BodyType bodyType, final Encoding encoding, final boolean includeSelection, final boolean includeDependent, final Path transformation) {
        this.bodyType = bodyType;
        this.encoding = encoding;
        this.includeSelection = includeSelection;
        this.includeDependent = includeDependent;
        this.transformation = transformation;
    }

     public BodyType getBodyType() {
         return bodyType;
     }

     public Encoding getEncoding() {
         return encoding;
     }

     public boolean isIncludeSelection() {
         return includeSelection;
     }

     public boolean isIncludeDependent() {
         return includeDependent;
     }

     @Nullable
     public Path getTransformation() {
         return transformation;
     }
 }
