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
package org.humanistika.oxygen.tei.authorizer.remote.impl;

import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.humanistika.ns.tei_authorizer.Suggestion;
import org.humanistika.ns.tei_authorizer.UserValue;
import org.humanistika.ns.tei_authorizer.UserValues;
import org.humanistika.oxygen.tei.authorizer.SuggestedAutocomplete;
import org.humanistika.oxygen.tei.authorizer.configuration.beans.BodyInfo;
import org.humanistika.oxygen.tei.authorizer.configuration.beans.UploadInfo;
import org.humanistika.oxygen.tei.authorizer.remote.Client;
import org.humanistika.oxygen.tei.completer.remote.ClientFactory;
import org.humanistika.oxygen.tei.completer.response.TransformationException;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.sync.net.protocol.http.HttpExceptionWithDetails;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.*;
import javax.xml.bind.JAXBContext;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Server client implemented using Jersey
 *
 * @author Adam Retter, Evolved Binary Ltd
 * @version 1.0
 * @serial 20160405
 */
public class JerseyClient extends org.humanistika.oxygen.tei.completer.remote.impl.JerseyClient implements Client {
    private final static Logger LOGGER = LoggerFactory.getLogger(JerseyClient.class);

    public JerseyClient(final ClientFactory.AuthenticationType authenticationType) {
        super(authenticationType);
    }

    /**
     * Used for injecting a test client
     * in unit tests
     */
    JerseyClient(final ClientFactory.AuthenticationType authenticationType, final javax.ws.rs.client.Client client) {
        super(authenticationType, client);
    }

    @Override
    public SuggestionResponse uploadSuggestion(final UploadInfo uploadInfo, final String suggestion, @Nullable final String description, @Nullable final String selectionValue, @Nullable final String dependentValue, @Nullable final List<SuggestedAutocomplete.UserValue> userValues) {
        try {
            final URL url = getUrl(uploadInfo, suggestion, description, selectionValue, dependentValue);

            Invocation.Builder requestBuilder = client
                    .target(url.toURI())
                    .request();

            if (uploadInfo.getAuthentication() != null) {
                requestBuilder = requestBuilder
                        .property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_USERNAME, uploadInfo.getAuthentication().getUsername())
                        .property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_PASSWORD, uploadInfo.getAuthentication().getPassword());
            }

            //prepare the body for the request
            final BodyInfo bodyInfo = uploadInfo.getBodyInfo();
            final Entity<?> entity;
            if (bodyInfo == null) {
                entity = null;
            } else {
                switch (bodyInfo.getBodyType()) {
                    case XML:
                        final Suggestion xml = getSuggestion(suggestion, description, bodyInfo.isIncludeSelection() ? selectionValue : null, bodyInfo.isIncludeDependent() ? dependentValue : null, userValues);
                        final Path xmlTransformation = bodyInfo.getTransformation();
                        final Variant xmlVariant = new Variant(MediaType.APPLICATION_XML_TYPE, (String) null, bodyInfo.getEncoding() == BodyInfo.Encoding.GZIP ? "gzip" : null);
                        if (xmlTransformation == null) {
                            entity = Entity.entity(xml, xmlVariant);
                        } else {
                            LOGGER.debug("Transforming XML upload to: {} using: {}", url, xmlTransformation);
                            entity = Entity.entity(transformXmlUpload(xml, xmlTransformation), xmlVariant);
                        }
                        break;

                    case JSON:
                        final Suggestion json = getSuggestion(suggestion, description, bodyInfo.isIncludeSelection() ? selectionValue : null, bodyInfo.isIncludeDependent() ? dependentValue : null, userValues);
                        final Path jsonTransformation = bodyInfo.getTransformation();
                        final Variant jsonVariant = new Variant(MediaType.APPLICATION_JSON_TYPE, (String) null, bodyInfo.getEncoding() == BodyInfo.Encoding.GZIP ? "gzip" : null);
                        if (jsonTransformation == null) {
                            entity = Entity.entity(json, jsonVariant);
                        } else {
                            LOGGER.debug("Transforming JSON upload to: {} using: {}", url, jsonTransformation);
                            entity = Entity.entity(transformJsonUpload(json, jsonTransformation), jsonVariant);
                        }
                        break;

                    case FORM:
                        final MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
                        formData.putSingle("suggestion", suggestion);
                        if (description != null) {
                            formData.putSingle("description", description);
                        }
                        if (bodyInfo.isIncludeSelection() && selectionValue != null) {
                            formData.putSingle("selectionValue", selectionValue);
                        }
                        if (bodyInfo.isIncludeDependent() && dependentValue != null) {
                            formData.putSingle("dependentValue", dependentValue);
                        }
                        if (userValues != null) {
                            for (final SuggestedAutocomplete.UserValue userValue : userValues) {
                                formData.putSingle(userValue.getName(), userValue.getValue());
                            }
                        }
                        final Variant formVariant = new Variant(MediaType.APPLICATION_FORM_URLENCODED_TYPE, (String) null, bodyInfo.getEncoding() == BodyInfo.Encoding.GZIP ? "gzip" : null);
                        entity = Entity.entity(new Form(formData), formVariant);
                        break;

                    default:
                        throw new IllegalStateException("Unknown Body Type: " + bodyInfo.getBodyType());
                }
            }

            final Response response;
            switch (uploadInfo.getMethod()) {
                case PUT:
                    response = requestBuilder.put(entity);
                    break;

                case POST:
                default:
                    response = requestBuilder.post(entity);
                    break;
            }

            final Response.StatusType statusInfo = response.getStatusInfo();
            if (statusInfo.getFamily() == Response.Status.Family.SUCCESSFUL) {
                return new SuggestionResponse(true, null);
            } else {
                LOGGER.error("Unable to upload suggestion to server: {}", statusInfo.getReasonPhrase());
                return new SuggestionResponse(false, statusInfo.getReasonPhrase());
            }
        } catch (final ProcessingException e) {
            LOGGER.error(e.getMessage(), e);
            if(e.getCause() instanceof HttpExceptionWithDetails) {
                final HttpExceptionWithDetails httpEx = ((HttpExceptionWithDetails)e.getCause());
                return new SuggestionResponse(false, "HTTP " + httpEx.getReasonCode() + " " + httpEx.getReason());
            } else {
                return new SuggestionResponse(false, e.getMessage());
            }
        } catch (final URISyntaxException | IOException | TransformationException e) {
            LOGGER.error(e.getMessage(), e);
            return new SuggestionResponse(false, e.getMessage());
        }
    }

    private Suggestion getSuggestion(final String value, @Nullable final String description, @Nullable final String selectionValue, @Nullable final String dependentValue, @Nullable final List<SuggestedAutocomplete.UserValue> userValues) {
        final Suggestion suggestion = new Suggestion();
        suggestion.setValue(value);
        suggestion.setDescription(description);
        suggestion.setSelectionValue(selectionValue);
        suggestion.setDependentValue(dependentValue);

        if(userValues != null) {
            for(final SuggestedAutocomplete.UserValue userValue : userValues) {
                final UserValue uv = new UserValue();
                uv.setName(userValue.getName());
                uv.setValue(userValue.getValue());
                if(suggestion.getUserValues() == null) {
                    suggestion.setUserValues(new UserValues());
                }
                suggestion.getUserValues().getUserValue().add(uv);
            }
        }

        return suggestion;
    }

    /**
     * Get the URL for connecting to the server
     *
     * Completes the URL from autoComplete with the suggestion and description
     *
     * @param uploadInfo The base request info
     * @param suggestion The suggestion
     * @param description The description or null
     * @param selectionValue the value of the selection or null
     * @param dependentValue the value of the dependent or null
     *
     * @return The URL for connecting to the server
     *
     * @throws MalformedURLException if the configured URL to use is invalid
     */
    protected URL getUrl(final UploadInfo uploadInfo, final String suggestion, @Nullable final String description, @Nullable final String selectionValue, @Nullable final String dependentValue) throws MalformedURLException {
        final Map<UploadInfo.UrlVar, String> substitutions = new HashMap<>();
        if(uploadInfo.getAuthentication() != null) {
            substitutions.put(UploadInfo.UrlVar.USERNAME, uploadInfo.getAuthentication().getUsername());
        }

        substitutions.put(UploadInfo.UrlVar.SUGGESTION, suggestion);

        if(description != null) {
            substitutions.put(UploadInfo.UrlVar.DESCRIPTION, description);
        }

        if(selectionValue != null) {
            substitutions.put(UploadInfo.UrlVar.SELECTION_VALUE, selectionValue);
        }

        if(dependentValue != null) {
            substitutions.put(UploadInfo.UrlVar.DEPENDENT_VALUE, dependentValue);
        }

        return uploadInfo.getUrl(substitutions);
    }

    private String transformXmlUpload(final Suggestion suggestion, final Path transformation) throws IOException, TransformationException {
        try(final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            try {
                final JAXBContext context = JAXBContext.newInstance(Suggestion.class);
                final Marshaller marshaller = context.createMarshaller();
                marshaller.marshal(suggestion, os);

                try(final ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray())) {
                    os.reset();
                    xmlTransformer.transform(is, transformation, os);
                    return new String(os.toByteArray(), StandardCharsets.UTF_8);
                }

            } catch (final JAXBException e) {
                throw new TransformationException(e);
            }
        }
    }

    private String transformJsonUpload(final Suggestion suggestion, final Path transformation) throws IOException, TransformationException {
        try(final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            try {
                final JAXBContext context = org.eclipse.persistence.jaxb.JAXBContextFactory.createContext(new Class[]{Suggestion.class}, null);
                final Marshaller marshaller = context.createMarshaller();
                marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, org.eclipse.persistence.oxm.MediaType.APPLICATION_JSON);
                marshaller.setProperty(MarshallerProperties.JSON_ATTRIBUTE_PREFIX, null);
                marshaller.setProperty(MarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME, false);
                marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false);
                marshaller.setProperty(MarshallerProperties.NAMESPACE_PREFIX_MAPPER, namespacePrefixMapper);
                marshaller.setProperty(MarshallerProperties.JSON_NAMESPACE_SEPARATOR, ':');
                marshaller.marshal(suggestion, os);

                try(final ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray())) {
                    os.reset();
                    jsonTransformer.transform(is, transformation, os);
                    return new String(os.toByteArray(), StandardCharsets.UTF_8);
                }
            } catch (final JAXBException e) {
                throw new TransformationException(e);
            }
        }
    }
}
