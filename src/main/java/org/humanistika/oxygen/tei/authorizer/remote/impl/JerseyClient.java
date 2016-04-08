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
import org.humanistika.oxygen.tei.authorizer.configuration.beans.UploadInfo;
import org.humanistika.oxygen.tei.authorizer.remote.Client;
import org.humanistika.oxygen.tei.completer.remote.ClientFactory;
import org.humanistika.oxygen.tei.completer.response.TransformationException;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.Map;

/**
 * Server client implemented using Jersey
 *
 * @author Adam Retter, Evolved Binary Ltd <adam.retter@googlemail.com>
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
    public boolean uploadSuggestion(final UploadInfo uploadInfo, final String suggestion, @Nullable final String description) {
        try {

            final URL url = getUrl(uploadInfo, suggestion, description);

            Invocation.Builder requestBuilder = client
                    .target(url.toURI())
                    .request();

            if(uploadInfo.getAuthentication() != null) {
                requestBuilder = requestBuilder
                        .property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_USERNAME, uploadInfo.getAuthentication().getUsername())
                        .property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_PASSWORD, uploadInfo.getAuthentication().getPassword());
            }

            //prepare the body for the request
            final Entity<?> entity;
            switch(uploadInfo.getBodyType()) {
                case XML:
                    final Suggestion xml = getSuggestion(suggestion, description);
                    final Path xmlTransformation = uploadInfo.getTransformation();
                    final Variant xmlVariant = new Variant(MediaType.APPLICATION_XML_TYPE, (String)null, uploadInfo.getEncoding() == UploadInfo.Encoding.GZIP ? "gzip" : null);
                    if(xmlTransformation == null) {
                        entity = Entity.entity(xml, xmlVariant);
                    } else {
                        LOGGER.debug("Transforming XML upload to: {} using: {}", url, xmlTransformation);
                        entity = Entity.entity(transformXmlUpload(xml, xmlTransformation), xmlVariant);
                    }
                    break;

                case JSON:
                    final Suggestion json = getSuggestion(suggestion, description);
                    final Path jsonTransformation = uploadInfo.getTransformation();
                    final Variant jsonVariant = new Variant(MediaType.APPLICATION_JSON_TYPE, (String)null, uploadInfo.getEncoding() == UploadInfo.Encoding.GZIP ? "gzip" : null);
                    if(jsonTransformation == null) {
                        entity = Entity.entity(json, jsonVariant);
                    } else {
                        LOGGER.debug("Transforming JSON upload to: {} using: {}", url, jsonTransformation);
                        entity = Entity.entity(transformJsonUpload(json, jsonTransformation), jsonVariant);
                    }
                    break;

                case FORM:
                    final MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
                    formData.putSingle("suggestion", suggestion);
                    if(description != null) {
                        formData.putSingle("description", description);
                    }
                    final Variant formVariant = new Variant(MediaType.APPLICATION_FORM_URLENCODED_TYPE, (String)null, uploadInfo.getEncoding() == UploadInfo.Encoding.GZIP ? "gzip" : null);
                    entity = Entity.entity(new Form(formData), formVariant);
                    break;

                case NONE:
                default:
                    entity = null;
                    break;

            }

            final Response response;
            switch(uploadInfo.getMethod()) {
                case PUT:
                    response = requestBuilder.put(entity);
                    break;

                case POST:
                default:
                    response = requestBuilder.post(entity);
                    break;
            }

            final Response.StatusType statusInfo = response.getStatusInfo();
            if(statusInfo.getFamily() == Response.Status.Family.SUCCESSFUL) {
                return true;
            } else {
                LOGGER.error("Unable to upload suggestion to server: {}", statusInfo.getReasonPhrase());
                return false;
            }
        } catch(final URISyntaxException | IOException | TransformationException e) {
            LOGGER.error(e.getMessage(), e); //TODO(AR) maybe something more visible to the user
            return false;
        }
    }

    private Suggestion getSuggestion(final String value, @Nullable final String description) {
        final Suggestion suggestion = new Suggestion();
        suggestion.setValue(value);
        suggestion.setDescription(description);
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
     *
     * @return The URL for connecting to the server
     */
    protected URL getUrl(final UploadInfo uploadInfo, final String suggestion, final @Nullable String description) throws MalformedURLException {
        final Map<UploadInfo.UrlVar, String> substitutions = new HashMap<>();
        substitutions.put(UploadInfo.UrlVar.SUGGESTION, suggestion);
        if(description != null) {
            substitutions.put(UploadInfo.UrlVar.DESCRIPTION, description);
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
