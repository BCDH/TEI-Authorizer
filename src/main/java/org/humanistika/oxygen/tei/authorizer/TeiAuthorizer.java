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
package org.humanistika.oxygen.tei.authorizer;

import org.humanistika.oxygen.tei.authorizer.configuration.ConfigurationFactory;
import org.humanistika.oxygen.tei.authorizer.configuration.beans.AutoComplete;
import org.humanistika.oxygen.tei.authorizer.configuration.beans.UploadInfo;
import org.humanistika.oxygen.tei.authorizer.remote.Client;
import org.humanistika.oxygen.tei.authorizer.remote.impl.JerseyClientFactory;
import org.humanistika.oxygen.tei.completer.TeiCompleter;
import org.humanistika.oxygen.tei.completer.configuration.Configuration;
import org.humanistika.oxygen.tei.completer.configuration.beans.Authentication;
import org.humanistika.oxygen.tei.completer.remote.ClientFactory;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.sync.contentcompletion.xml.CIValue;
import ro.sync.contentcompletion.xml.WhatPossibleValuesHasAttributeContext;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TEI-Authorizer
 *
 * Oxygen XML Editor plugin for customizable attribute and value completion
 * and/or creation for TEI P5 documents
 *
 * Works in conjunction with TEI P5 documents, by suggesting content values
 * for various attributes. If no content value exists, or none is suitable
 * it allows you to create new content values.
 * Content values are retrieved from and stored to a remote server.
 *
 * @author Adam Retter, Evolved Binary Ltd <adam.retter@googlemail.com>
 * @version 1.0
 * @serial 20160405
 */
public class TeiAuthorizer extends TeiCompleter {
    private final static Logger LOGGER = LoggerFactory.getLogger(TeiAuthorizer.class);

    @Override
    public String getDescription() {
        return "BCDH TEI-Authorizer for TEI P5";
    }

    /**
     * Performs content completion for the TEI P5 configured attributes
     * by resolving possible attribute values from a server
     */
    @Override
    public List<CIValue> filterAttributeValues(List<CIValue> list, final WhatPossibleValuesHasAttributeContext context) {
        if (context != null) {
            final AutoCompleteSuggestions<org.humanistika.oxygen.tei.completer.configuration.beans.AutoComplete> autoCompleteSuggestions = getAutoCompleteSuggestions(context);

            if(autoCompleteSuggestions != null) {
                if (list == null) {
                    list = new ArrayList<>();
                }

                list.addAll(autoCompleteSuggestions.getSuggestions());


                final AutoComplete autoComplete = ((AutoComplete)autoCompleteSuggestions.getAutoComplete());
                final UploadInfo uploadInfo = autoComplete.getUploadInfo();

                //only show the "Add New..." if there is an auto-complete with upload config for the
                if(uploadInfo != null) {
                    final AutoCompleteContext autoCompleteContext = autoCompleteSuggestions.getAutoCompleteContext();

                    //Add an "Add New..." option to the list
                    list.add(new AddNewSuggestionCIValue(uploadInfo, autoCompleteContext.getSelectedValue(), autoCompleteContext.getDependentValue()));
                }
            }
        }
        return list;
    }

    @Override
    protected Configuration getConfiguration() {
        if(configuration == null) {
            synchronized(this) {
                if(configuration == null) {
                    this.configuration = ConfigurationFactory.getInstance().loadConfiguration();
                }
            }
        }
        return configuration;
    }

    @Override
    protected ClientFactory getClientFactory() {
        return JerseyClientFactory.getInstance();
    }

    /**
     * A CIValue labelled "Add New..." which
     * prompts the user to enter a new suggestion
     * via a dialog box
     */
    public class AddNewSuggestionCIValue extends CIValue {

        private final UploadInfo uploadInfo;
        private String suggestion = null;
        private String description = null;
        @Nullable private final String selectionValue;
        @Nullable private final String dependentValue;

        public AddNewSuggestionCIValue(final UploadInfo uploadInfo, final String selectionValue, final String dependentValue) {
            super("Add New...", "Add a new suggestion");
            this.uploadInfo = uploadInfo;
            this.selectionValue = selectionValue;
            this.dependentValue = dependentValue;
        }

        @Override
        public String getInsertString() {
            if(suggestion == null) {

                //Ask the user for an autocomplete suggestion
                final SuggestedAutocomplete suggestedAutocomplete = promptUserForNewSuggestion();

                if(suggestedAutocomplete != null) {
                    //TODO(AR) upload to the server, on error alert the user, on success replace in document
                    uploadSuggestion(suggestedAutocomplete);
                }

                //process the result from the dialog
                final String suggestion = suggestedAutocomplete.getSuggestion();
                this.suggestion =  (suggestion == null ? "" : suggestion);
                this.description =  suggestedAutocomplete.getDescription();
            }

            return suggestion;
        }

        private boolean uploadSuggestion(final SuggestedAutocomplete suggestedAutocomplete) {
            final Authentication.AuthenticationType authenticationType = uploadInfo.getAuthentication() == null ? null : uploadInfo.getAuthentication().getAuthenticationType();
            final Client client = (Client)getClient(authenticationType);
            return client.uploadSuggestion(uploadInfo, suggestedAutocomplete.getSuggestion(), suggestedAutocomplete.getDescription(), selectionValue, dependentValue);
        }

        /**
         * Shows a dialog which allows the user to enter a new
         * suggestion
         *
         * @return The suggestion entered by the user or null
         * if the user closed or cancelled the dialog
         */
        @Nullable
        private SuggestedAutocomplete promptUserForNewSuggestion() {
            final KeyboardFocusManager keyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            final Component comp = keyboardFocusManager.getFocusOwner();
            final Frame frame = getParentFrame(comp);
            final NewSuggestionForm newSuggestionForm = new NewSuggestionForm(frame);

            //set location of the dialog
            if(comp instanceof JTextArea) {
                final Point caretPosition = ((JTextArea)comp).getCaret().getMagicCaretPosition();
                if(caretPosition != null) {
                    newSuggestionForm.setLocation(caretPosition);
                } else {
                    newSuggestionForm.setLocationRelativeTo(comp);
                }
            } else {
                newSuggestionForm.setLocationRelativeTo(comp);
            }

            //display the dialog
            newSuggestionForm.setVisible(true);
            final SuggestedAutocomplete suggestedAutocomplete = newSuggestionForm.getSuggestedAutocomplete();
            newSuggestionForm.dispose();
            return suggestedAutocomplete;
        }

        private Frame getParentFrame(final Component component) {
            if(component == null) {
                return null;
            }

            final Component parent = component.getParent();
            if(parent == null) {
                return null;
            }

            if(parent instanceof Frame) {
                return (Frame)parent;
            }

            return getParentFrame(parent);
        }
    }
}
