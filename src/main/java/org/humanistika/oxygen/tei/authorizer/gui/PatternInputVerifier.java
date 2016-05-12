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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Verifies the input to a text box based on a regular expression
 *
 * @author Adam Retter, Evolved Binary Ltd
 */
public class PatternInputVerifier extends InputVerifier {
    private final Matcher matcher;
    private final boolean hasDefaultValue;

    public PatternInputVerifier(final Pattern pattern, final boolean hasDefaultValue) {
        this.matcher = pattern.matcher("");
        this.hasDefaultValue = hasDefaultValue;
    }

    @Override
    public boolean verify(final JComponent input) {
        if(input instanceof JTextComponent) {
            final String txt = ((JTextComponent) input).getText();
            final int txtLen = txt.trim().length();
            if(hasDefaultValue && txtLen == 0) {
                return true;
            } else {
                matcher.reset(txt);
                return matcher.matches();
            }
        } else {
            return false;
        }
    }
}
