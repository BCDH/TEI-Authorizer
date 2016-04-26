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

import org.humanistika.oxygen.tei.authorizer.SuggestedAutocomplete;
import org.humanistika.oxygen.tei.authorizer.configuration.beans.UserFieldInfo;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Form for a user to enter the details of a suggested automcomplete
 *
 * @author Adam Retter, Evolved Binary Ltd <adam.retter@googlemail.com>
 * @version 1.0
 * @serial 20160405
 */
public class NewSuggestionForm extends javax.swing.JDialog {

    private final static Logger LOGGER = LoggerFactory.getLogger(NewSuggestionForm.class);

    private static class UserFieldText {
        private final UserFieldInfo userFieldInfo;
        private final JTextComponent text;

        private UserFieldText(final UserFieldInfo userFieldInfo, final JTextComponent text) {
            this.userFieldInfo = userFieldInfo;
            this.text = text;
        }

        public UserFieldInfo getUserFieldInfo() {
            return userFieldInfo;
        }

        public JTextComponent getText() {
            return text;
        }
    }

    @Nullable private final List<UserFieldText> userFieldTexts;
    private SuggestedAutocomplete suggestedAutocomplete = null;

    /**
     * Creates new form NewSuggestionForm
     */
    public NewSuggestionForm(final Frame owner, final java.util.List<UserFieldInfo> userFieldsInfo) {
        super(owner, ModalityType.DOCUMENT_MODAL);
        initComponents();
        this.userFieldTexts = initUserFields(userFieldsInfo);
    }

    private List<UserFieldText> initUserFields(final List<UserFieldInfo> userFieldsInfo) {
        final List<UserFieldText> userFieldTexts;

        if(userFieldsInfo == null) {
            panUserFields.setVisible(false);
            userFieldTexts = null;
        } else {
            userFieldTexts = new ArrayList<>();

            final javax.swing.GroupLayout panUserFieldsLayout = (javax.swing.GroupLayout)panUserFields.getLayout();

            panUserFieldsLayout.setAutoCreateGaps(true);
            panUserFieldsLayout.setAutoCreateContainerGaps(true);

            final GroupLayout.SequentialGroup hGroup = panUserFieldsLayout.createSequentialGroup();
            final GroupLayout.ParallelGroup col1 = panUserFieldsLayout.createParallelGroup();
            final GroupLayout.ParallelGroup col2 = panUserFieldsLayout.createParallelGroup();

            final GroupLayout.SequentialGroup vGroup = panUserFieldsLayout.createSequentialGroup();

            for(final UserFieldInfo userFieldInfo : userFieldsInfo) {
                final JLabel label = new JLabel((userFieldInfo.getLabel() != null ? userFieldInfo.getLabel() : capitalise(userFieldInfo.getName())) + ":");
                col1.addComponent(label);

                final JComponent component;
                final JTextComponent text;
                if(userFieldInfo.isMultiline()) {
                    text = new JTextArea();
                    ((JTextArea)text).setRows(3);
                    final JScrollPane sp = new JScrollPane(text);
                    sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                    component = sp;
                } else {
                    text = new JTextField();
                    component = text;
                }

                if(userFieldInfo.getInitialValue() != null) {
                    text.setText(userFieldInfo.getInitialValue());
                }

                final boolean hasDefaultValue = userFieldInfo.getDefaultValue() != null;

                if(userFieldInfo.isRequired()) {
                    text.setInputVerifier(new RequiredVerifier(hasDefaultValue));
                }

                if(userFieldInfo.getValidateWith() != null) {
                    text.setInputVerifier(new PatternInputVerifier(userFieldInfo.getValidateWith(), hasDefaultValue));
                }

                if(userFieldInfo.isRequired() || userFieldInfo.getValidateWith() != null) {
                    new HighlightListener(text, hasDefaultValue);
                }

                col2.addComponent(component);
                vGroup.addGroup(panUserFieldsLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(label).addComponent(component));

                userFieldTexts.add(new UserFieldText(userFieldInfo, text));
            }

            hGroup.addGroup(col1);
            hGroup.addGroup(col2);
            panUserFieldsLayout.setHorizontalGroup(hGroup);

            panUserFieldsLayout.setVerticalGroup(vGroup);

            pack();
        }

        return userFieldTexts;
    }

    private final String capitalise(final String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblSuggestion = new javax.swing.JLabel();
        lblDescription = new javax.swing.JLabel();
        txtSuggestion = new javax.swing.JTextField();
        txtSuggestion.setInputVerifier(new RequiredVerifier(false));
        new HighlightListener(txtSuggestion, false);
        spDescription = new javax.swing.JScrollPane();
        txtDescription = new javax.swing.JTextArea();
        btnOk = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        panUserFields = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("New Suggestion");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                windowClosedHandler(evt);
            }
        });

        lblSuggestion.setText("Suggestion:");

        lblDescription.setText("Description:");

        txtDescription.setColumns(20);
        txtDescription.setRows(5);
        spDescription.setViewportView(txtDescription);

        btnOk.setText("Ok");
        btnOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOkActionPerformed(evt);
            }
        });

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        panUserFields.setBorder(javax.swing.BorderFactory.createTitledBorder("Metadata"));

        javax.swing.GroupLayout panUserFieldsLayout = new javax.swing.GroupLayout(panUserFields);
        panUserFields.setLayout(panUserFieldsLayout);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(lblSuggestion)
                        .addGap(18, 18, 18)
                        .addComponent(txtSuggestion, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnOk))
                    .addComponent(panUserFields, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblDescription)
                        .addGap(18, 18, 18)
                        .addComponent(spDescription, javax.swing.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)))
                .addContainerGap(14, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSuggestion)
                    .addComponent(txtSuggestion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblDescription)
                    .addComponent(spDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(panUserFields, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnOk)
                    .addComponent(btnCancel))
                .addGap(20, 20, 20))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        this.suggestedAutocomplete = null;
        dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void windowClosedHandler(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_windowClosedHandler

    }//GEN-LAST:event_windowClosedHandler

    private void btnOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOkActionPerformed
        final String suggestion = txtSuggestion.getText();
        final String description = txtDescription.getText();

        final boolean validUserValues;
        final List<SuggestedAutocomplete.UserValue> userValues;
        if(userFieldTexts == null) {
            userValues = null;
            validUserValues = true;
        } else {
            userValues = new ArrayList<>();
            boolean valid = true;
            for(final UserFieldText userFieldText : userFieldTexts) {

                // get the user entered text or the default value (if present)
                final JTextComponent jText = userFieldText.getText();
                final boolean userEnteredValue = jText.getText() != null && !jText.getText().isEmpty();
                final String value = (!userEnteredValue && userFieldText.getUserFieldInfo().getDefaultValue() != null ? userFieldText.getUserFieldInfo().getDefaultValue() : jText.getText());

                userValues.add(new SuggestedAutocomplete.UserValue(userFieldText.getUserFieldInfo().getName(), value));

                final InputVerifier verifier = jText.getInputVerifier();
                if(verifier != null) {
                    if(!verifier.verify(jText)) {
                        valid = false;
                    }
                }
            }
            validUserValues = valid;
        }

        //TODO(AR) only close if validation passes
        final boolean validSuggestion = txtSuggestion.getInputVerifier().verify(txtSuggestion);
        if(validSuggestion && validUserValues) {
            this.suggestedAutocomplete = new SuggestedAutocomplete(suggestion, description == null || description.length() == 0 ? null : description, userValues);
            dispose();
        } else {
            LOGGER.debug("Not OK'ing form, validSuggestion={}, validUserValues={}", validSuggestion, validUserValues);
        }
    }//GEN-LAST:event_btnOkActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOk;
    private javax.swing.JLabel lblDescription;
    private javax.swing.JLabel lblSuggestion;
    private javax.swing.JPanel panUserFields;
    private javax.swing.JScrollPane spDescription;
    private javax.swing.JTextArea txtDescription;
    private javax.swing.JTextField txtSuggestion;
    // End of variables declaration//GEN-END:variables


    @Nullable public SuggestedAutocomplete getSuggestedAutocomplete() {
        return suggestedAutocomplete;
    }
}
