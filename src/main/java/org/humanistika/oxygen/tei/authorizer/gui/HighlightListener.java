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
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

public class HighlightListener implements DocumentListener {
    private final static Border HIGHLIGHT_WARN_BORDER = BorderFactory.createLineBorder(java.awt.Color.ORANGE);
    private final static Border HIGHLIGHT_ERROR_BORDER = BorderFactory.createLineBorder(java.awt.Color.RED);

    private final JTextComponent watched;
    private final Border defaultBorder;
    private final boolean hasDefaultValue;

    public HighlightListener(final JTextComponent watched, final boolean hasDefaultValue) {
        this.watched = watched;
        this.defaultBorder = watched.getBorder();
        this.hasDefaultValue = hasDefaultValue;

        watched.getDocument().addDocumentListener(this);
        this.maybeHighlight();
    }

    @Override
    public void insertUpdate(final DocumentEvent documentEvent) {
        maybeHighlight();
    }

    @Override
    public void removeUpdate(final DocumentEvent documentEvent) {
        maybeHighlight();
    }

    @Override
    public void changedUpdate(final DocumentEvent documentEvent) {
        maybeHighlight();
    }

    private void maybeHighlight() {
        final int txtLen = watched.getText().trim().length();

        if(hasDefaultValue && txtLen == 0) {
            watched.setBorder(defaultBorder);
        } else {
            final InputVerifier verifier = watched.getInputVerifier();
            if (verifier != null) {
                if (verifier.verify(watched)) {
                    watched.setBorder(defaultBorder);
                } else {
                    watched.setBorder(HIGHLIGHT_ERROR_BORDER);
                }
            } else {
                if (txtLen > 0) {
                    // if a field is non-empty, switch it to default look
                    watched.setBorder(defaultBorder);
                } else {
                    // if a field is empty, highlight it
                    watched.setBorder(HIGHLIGHT_WARN_BORDER);
                }
            }
        }
    }
}
