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
package org.humanistika.oxygen.tei.authorizer.gui;

import javax.swing.*;
import javax.swing.text.JTextComponent;

/**
 * Verifies that the user has entered something in a text box
 *
 * @author Adam Retter, Evolved Binary Ltd
 */
public class RequiredVerifier extends InputVerifier {
    private final boolean hasDefaultValue;

    public RequiredVerifier(final boolean hasDefaultValue) {
        this.hasDefaultValue = hasDefaultValue;
    }

    @Override
    public boolean verify(final JComponent input) {
        if(input instanceof JTextComponent) {
            if (hasDefaultValue) {
                return true;
            } else {
                return ((JTextComponent) input).getText().trim().length() > 0;
            }
        } else {
            return false;
        }
    }
}
