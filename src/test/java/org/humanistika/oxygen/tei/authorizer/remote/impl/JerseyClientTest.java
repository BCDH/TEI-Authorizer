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

import org.apache.commons.codec.binary.Base64;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.test.JerseyTest;
import org.humanistika.ns.tei_authorizer.Suggestion;
import org.humanistika.oxygen.tei.authorizer.configuration.beans.UploadInfo;
import org.humanistika.oxygen.tei.completer.configuration.beans.Authentication;
import org.humanistika.oxygen.tei.completer.remote.ClientFactory.AuthenticationType;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.attribute.UserPrincipal;
import java.security.Principal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for {@link org.humanistika.oxygen.tei.authorizer.remote.impl.JerseyClient}
 *
 * @author Adam Retter, Evolved Binary Ltd <adam.retter@googlemail.com>
 * @version 1.0
 * @serial 20160405
 */
public class JerseyClientTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(MockServer.class)
                .register(RolesAllowedDynamicFeature.class)
                .register(new MockSecurityFilter(), Priorities.AUTHENTICATION)
                .register(JerseyClient.createMoxyJsonResolver())
                .register(GZipEncoder.class)
                .register(EncodingFilter.class);
    }

    //state for holding the received suggestion and description from the server
    static String receivedSuggestion = null;
    static String receivedDescription = null;

    @Before
    public void resetState() {
        receivedSuggestion = null;
        receivedDescription = null;
    }

    @Path("multext")
    @PermitAll
    public static class MockServer {

        @POST
        @Path("upload")
        @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
        public void postUploadBody_Xml_Json(final Suggestion suggestion) {
            receivedSuggestion = suggestion.getValue();
            receivedDescription = suggestion.getDescription();
        }

        @POST
        @Path("upload")
        @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
        public void postUploadBody_FormData(@FormParam("suggestion") final String suggestion, @FormParam("description") final String description) {
            receivedSuggestion = suggestion;
            receivedDescription = description;
        }

        @POST
        @Path("upload-qs")
        public void postUploadQueryString(@QueryParam("suggestion") final String suggestion, @QueryParam("description") final String description) {
            receivedSuggestion = suggestion;
            receivedDescription = description;
        }

        @PUT
        @Path("upload")
        @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
        public void putUploadBody_Xml_Json(final Suggestion suggestion) {
            receivedSuggestion = suggestion.getValue();
            receivedDescription = suggestion.getDescription();
        }

        @PUT
        @Path("upload")
        @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
        public void putUploadBody_FormData(@FormParam("suggestion") final String suggestion, @FormParam("description") final String description) {
            receivedSuggestion = suggestion;
            receivedDescription = description;
        }

        @POST
        @Path("secure/upload")
        @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
        @RolesAllowed(AUTHENTICATED_ROLE)
        public void secure_postUploadBody_Xml_Json(final Suggestion suggestion) {
            receivedSuggestion = suggestion.getValue();
            receivedDescription = suggestion.getDescription();
        }

        @POST
        @Path("secure/upload")
        @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
        @RolesAllowed(AUTHENTICATED_ROLE)
        public void secure_postUploadBody_FormData(@FormParam("suggestion") final String suggestion, @FormParam("description") final String description) {
            receivedSuggestion = suggestion;
            receivedDescription = description;
        }

        @POST
        @Path("secure/upload-qs")
        @RolesAllowed(AUTHENTICATED_ROLE)
        public void secure_postUploadQueryString(@QueryParam("suggestion") final String suggestion, @QueryParam("description") final String description) {
            receivedSuggestion = suggestion;
            receivedDescription = description;
        }

        @PUT
        @Path("secure/upload")
        @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
        @RolesAllowed(AUTHENTICATED_ROLE)
        public void secure_putUploadBody_Xml_Json(final Suggestion suggestion) {
            receivedSuggestion = suggestion.getValue();
            receivedDescription = suggestion.getDescription();
        }

        @PUT
        @Path("secure/upload")
        @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
        @RolesAllowed(AUTHENTICATED_ROLE)
        public void secure_putUploadBody_FormData(@FormParam("suggestion") final String suggestion, @FormParam("description") final String description) {
            receivedSuggestion = suggestion;
            receivedDescription = description;
        }
    }

    @Test
    public void postUploadSuggestion_Xml() {
        final String suggestion = "some-suggestion";
        final String description = null;

        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.POST, UploadInfo.BodyType.XML, UploadInfo.Encoding.NONE, getBaseUri() + "multext/upload", null, null);
        final boolean success = new JerseyClient(AuthenticationType.NONE, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Test
    public void postUploadSuggestionDescription_Xml() {
        final String suggestion = "some-suggestion";
        final String description = "some-description";

        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.POST, UploadInfo.BodyType.XML, UploadInfo.Encoding.NONE, getBaseUri() + "multext/upload", null, null);
        final boolean success = new JerseyClient(AuthenticationType.NONE, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Ignore("Until we figure out how to have Jersey server accept JSON JAXB objects")
    @Test
    public void postUploadSuggestion_Json() {
        final String suggestion = "some-suggestion";
        final String description = null;

        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.POST, UploadInfo.BodyType.JSON, UploadInfo.Encoding.NONE, getBaseUri() + "multext/upload", null, null);
        final boolean success = new JerseyClient(AuthenticationType.NONE, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Ignore("Until we figure out how to have Jersey server accept JSON JAXB objects")
    @Test
    public void postUploadSuggestionDescription_Json() {
        final String suggestion = "some-suggestion";
        final String description = "some-description";

        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.POST, UploadInfo.BodyType.JSON, UploadInfo.Encoding.NONE, getBaseUri() + "multext/upload", null, null);
        final boolean success = new JerseyClient(AuthenticationType.NONE, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Test
    public void postUploadSuggestion_FormData() {
        final String suggestion = "some-suggestion";
        final String description = null;

        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.POST, UploadInfo.BodyType.FORM, UploadInfo.Encoding.NONE, getBaseUri() + "multext/upload", null, null);
        final boolean success = new JerseyClient(AuthenticationType.NONE, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Test
    public void postUploadSuggestionDescription_FormData() {
        final String suggestion = "some-suggestion";
        final String description = "some-description";

        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.POST, UploadInfo.BodyType.FORM, UploadInfo.Encoding.NONE, getBaseUri() + "multext/upload", null, null);
        final boolean success = new JerseyClient(AuthenticationType.NONE, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Test
    public void postUploadSuggestion_queryString() {
        final String suggestion = "some-suggestion";
        final String description = null;

        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.POST, UploadInfo.BodyType.NONE, UploadInfo.Encoding.NONE, getBaseUri() + "multext/upload-qs?" + UploadInfo.UrlVar.SUGGESTION.name().toLowerCase() + "=" + UploadInfo.UrlVar.SUGGESTION.var(), null, null);
        final boolean success = new JerseyClient(AuthenticationType.NONE, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Test
    public void postUploadSuggestionDescription_queryString() {
        final String suggestion = "some-suggestion";
        final String description = "some-description";

        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.POST, UploadInfo.BodyType.NONE, UploadInfo.Encoding.NONE, getBaseUri() + "multext/upload-qs?" + UploadInfo.UrlVar.SUGGESTION.name().toLowerCase() + "=" + UploadInfo.UrlVar.SUGGESTION.var() + "&" + UploadInfo.UrlVar.DESCRIPTION.name().toLowerCase() + "=" + UploadInfo.UrlVar.DESCRIPTION.var(), null, null);
        final boolean success = new JerseyClient(AuthenticationType.NONE, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Test
    public void postUploadSuggestion_Gzip_Xml() {
        final String suggestion = "some-suggestion";
        final String description = null;

        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.POST, UploadInfo.BodyType.XML, UploadInfo.Encoding.GZIP, getBaseUri() + "multext/upload", null, null);
        final boolean success = new JerseyClient(AuthenticationType.NONE, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Test
    public void postUploadSuggestionDescription_Gzip_Xml() {
        final String suggestion = "some-suggestion";
        final String description = "some-description";

        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.POST, UploadInfo.BodyType.XML, UploadInfo.Encoding.GZIP, getBaseUri() + "multext/upload", null, null);
        final boolean success = new JerseyClient(AuthenticationType.NONE, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Ignore("Until we figure out how to have Jersey server accept JSON JAXB objects")
    @Test
    public void postUploadSuggestion_GzipJson() {
        final String suggestion = "some-suggestion";
        final String description = null;

        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.POST, UploadInfo.BodyType.JSON, UploadInfo.Encoding.GZIP, getBaseUri() + "multext/upload", null, null);
        final boolean success = new JerseyClient(AuthenticationType.NONE, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Ignore("Until we figure out how to have Jersey server accept JSON JAXB objects")
    @Test
    public void postUploadSuggestionDescription_Gzip_Json() {
        final String suggestion = "some-suggestion";
        final String description = "some-description";

        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.POST, UploadInfo.BodyType.JSON, UploadInfo.Encoding.GZIP, getBaseUri() + "multext/upload", null, null);
        final boolean success = new JerseyClient(AuthenticationType.NONE, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Test
    public void postUploadSuggestion_Gzip_FormData() {
        final String suggestion = "some-suggestion";
        final String description = null;

        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.POST, UploadInfo.BodyType.FORM, UploadInfo.Encoding.GZIP, getBaseUri() + "multext/upload", null, null);
        final boolean success = new JerseyClient(AuthenticationType.NONE, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Test
    public void postUploadSuggestionDescription_Gzip_FormData() {
        final String suggestion = "some-suggestion";
        final String description = "some-description";

        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.POST, UploadInfo.BodyType.FORM, UploadInfo.Encoding.GZIP, getBaseUri() + "multext/upload", null, null);
        final boolean success = new JerseyClient(AuthenticationType.NONE, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Test
    public void putUploadSuggestion_Xml() {
        final String suggestion = "some-suggestion";
        final String description = null;

        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.PUT, UploadInfo.BodyType.XML, UploadInfo.Encoding.NONE, getBaseUri() + "multext/upload", null, null);
        final boolean success = new JerseyClient(AuthenticationType.NONE, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Test
    public void putUploadSuggestionDescription_Xml() {
        final String suggestion = "some-suggestion";
        final String description = "some-description";

        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.PUT, UploadInfo.BodyType.XML, UploadInfo.Encoding.NONE, getBaseUri() + "multext/upload", null, null);
        final boolean success = new JerseyClient(AuthenticationType.NONE, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Ignore("Until we figure out how to have Jersey server accept JSON JAXB objects")
    @Test
    public void putUploadSuggestion_Json() {
        final String suggestion = "some-suggestion";
        final String description = null;

        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.PUT, UploadInfo.BodyType.JSON, UploadInfo.Encoding.NONE, getBaseUri() + "multext/upload", null, null);
        final boolean success = new JerseyClient(AuthenticationType.NONE, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Ignore("Until we figure out how to have Jersey server accept JSON JAXB objects")
    @Test
    public void putUploadSuggestionDescription_Json() {
        final String suggestion = "some-suggestion";
        final String description = "some-description";

        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.PUT, UploadInfo.BodyType.JSON, UploadInfo.Encoding.NONE, getBaseUri() + "multext/upload", null, null);
        final boolean success = new JerseyClient(AuthenticationType.NONE, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Test
    public void putUploadSuggestion_FormData() {
        final String suggestion = "some-suggestion";
        final String description = null;

        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.PUT, UploadInfo.BodyType.FORM, UploadInfo.Encoding.NONE, getBaseUri() + "multext/upload", null, null);
        final boolean success = new JerseyClient(AuthenticationType.NONE, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Test
    public void putUploadSuggestionDescription_FormData() {
        final String suggestion = "some-suggestion";
        final String description = "some-description";

        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.PUT, UploadInfo.BodyType.FORM, UploadInfo.Encoding.NONE, getBaseUri() + "multext/upload", null, null);
        final boolean success = new JerseyClient(AuthenticationType.NONE, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Test(expected = IllegalStateException.class)
    public void putUploadSuggestion_queryString() {
        final String suggestion = "some-suggestion";
        final String description = null;

        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.PUT, UploadInfo.BodyType.NONE, UploadInfo.Encoding.NONE, getBaseUri() + "multext/upload-qs?" + UploadInfo.UrlVar.SUGGESTION.name().toLowerCase() + "=" + UploadInfo.UrlVar.SUGGESTION.var(), null, null);
        new JerseyClient(AuthenticationType.NONE, client()).uploadSuggestion(uploadInfo, suggestion, description);
    }

    @Test(expected = IllegalStateException.class)
    public void putUploadSuggestionDescription_queryString() {
        final String suggestion = "some-suggestion";
        final String description = "some-description";

        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.PUT, UploadInfo.BodyType.NONE, UploadInfo.Encoding.NONE, getBaseUri() + "multext/upload-qs?" + UploadInfo.UrlVar.SUGGESTION.name().toLowerCase() + "=" + UploadInfo.UrlVar.SUGGESTION.var() + "&" + UploadInfo.UrlVar.DESCRIPTION.name().toLowerCase() + "=" + UploadInfo.UrlVar.DESCRIPTION.var(), null, null);
        new JerseyClient(AuthenticationType.NONE, client()).uploadSuggestion(uploadInfo, suggestion, description);
    }

    @Test
    public void putUploadSuggestion_Gzip_Xml() {
        final String suggestion = "some-suggestion";
        final String description = null;

        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.PUT, UploadInfo.BodyType.XML, UploadInfo.Encoding.GZIP, getBaseUri() + "multext/upload", null, null);
        final boolean success = new JerseyClient(AuthenticationType.NONE, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Test
    public void putUploadSuggestionDescription_Gzip_Xml() {
        final String suggestion = "some-suggestion";
        final String description = "some-description";

        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.PUT, UploadInfo.BodyType.XML, UploadInfo.Encoding.GZIP, getBaseUri() + "multext/upload", null, null);
        final boolean success = new JerseyClient(AuthenticationType.NONE, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Ignore("Until we figure out how to have Jersey server accept JSON JAXB objects")
    @Test
    public void putUploadSuggestion_Gzip_Json() {
        final String suggestion = "some-suggestion";
        final String description = null;

        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.PUT, UploadInfo.BodyType.JSON, UploadInfo.Encoding.GZIP, getBaseUri() + "multext/upload", null, null);
        final boolean success = new JerseyClient(AuthenticationType.NONE, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Ignore("Until we figure out how to have Jersey server accept JSON JAXB objects")
    @Test
    public void putUploadSuggestionDescription_Gzip_Json() {
        final String suggestion = "some-suggestion";
        final String description = "some-description";

        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.PUT, UploadInfo.BodyType.JSON, UploadInfo.Encoding.GZIP, getBaseUri() + "multext/upload", null, null);
        final boolean success = new JerseyClient(AuthenticationType.NONE, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Test
    public void putUploadSuggestion_Gzip_FormData() {
        final String suggestion = "some-suggestion";
        final String description = null;

        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.PUT, UploadInfo.BodyType.FORM, UploadInfo.Encoding.GZIP, getBaseUri() + "multext/upload", null, null);
        final boolean success = new JerseyClient(AuthenticationType.NONE, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Test
    public void putUploadSuggestionDescription_Gzip_FormData() {
        final String suggestion = "some-suggestion";
        final String description = "some-description";

        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.PUT, UploadInfo.BodyType.FORM, UploadInfo.Encoding.GZIP, getBaseUri() + "multext/upload", null, null);
        final boolean success = new JerseyClient(AuthenticationType.NONE, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Test
    public void secure_preemptiveBasic_postUploadSuggestionDescription_Xml() {
        final String suggestion = "some-suggestion";
        final String description = "some-description";

        final Authentication authentication = new Authentication(Authentication.AuthenticationType.PREEMPTIVE_BASIC, TEST_USERNAME, TEST_PASSWORD);
        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.POST, UploadInfo.BodyType.XML, UploadInfo.Encoding.NONE, getBaseUri() + "multext/secure/upload", authentication, null);
        final boolean success = new JerseyClient(AuthenticationType.PREEMPTIVE_BASIC, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Ignore("Until we figure out how to have Jersey server accept JSON JAXB objects")
    @Test
    public void secure_preemptiveBasic_postUploadSuggestionDescription_Json() {
        final String suggestion = "some-suggestion";
        final String description = "some-description";

        final Authentication authentication = new Authentication(Authentication.AuthenticationType.PREEMPTIVE_BASIC, TEST_USERNAME, TEST_PASSWORD);
        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.POST, UploadInfo.BodyType.JSON, UploadInfo.Encoding.NONE, getBaseUri() + "multext/secure/upload", authentication, null);
        final boolean success = new JerseyClient(AuthenticationType.PREEMPTIVE_BASIC, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Test
    public void secure_preemptiveBasic_postUploadSuggestionDescription_FormData() {
        final String suggestion = "some-suggestion";
        final String description = "some-description";

        final Authentication authentication = new Authentication(Authentication.AuthenticationType.PREEMPTIVE_BASIC, TEST_USERNAME, TEST_PASSWORD);
        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.POST, UploadInfo.BodyType.FORM, UploadInfo.Encoding.NONE, getBaseUri() + "multext/secure/upload", authentication, null);
        final boolean success = new JerseyClient(AuthenticationType.PREEMPTIVE_BASIC, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Test
    public void secure_preemptiveBasic_postUploadSuggestionDescription_queryString() {
        final String suggestion = "some-suggestion";
        final String description = "some-description";

        final Authentication authentication = new Authentication(Authentication.AuthenticationType.PREEMPTIVE_BASIC, TEST_USERNAME, TEST_PASSWORD);
        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.POST, UploadInfo.BodyType.NONE, UploadInfo.Encoding.NONE, getBaseUri() + "multext/upload-qs?" + UploadInfo.UrlVar.SUGGESTION.name().toLowerCase() + "=" + UploadInfo.UrlVar.SUGGESTION.var() + "&" + UploadInfo.UrlVar.DESCRIPTION.name().toLowerCase() + "=" + UploadInfo.UrlVar.DESCRIPTION.var(), authentication, null);
        final boolean success = new JerseyClient(AuthenticationType.PREEMPTIVE_BASIC, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    @Test
    public void secure_preemptiveBasic_putUploadSuggestionDescription_Xml() {
        final String suggestion = "some-suggestion";
        final String description = "some-description";

        final Authentication authentication = new Authentication(Authentication.AuthenticationType.PREEMPTIVE_BASIC, TEST_USERNAME, TEST_PASSWORD);
        final UploadInfo uploadInfo = new UploadInfo(UploadInfo.Method.PUT, UploadInfo.BodyType.XML, UploadInfo.Encoding.NONE, getBaseUri() + "multext/secure/upload", authentication, null);
        final boolean success = new JerseyClient(AuthenticationType.PREEMPTIVE_BASIC, client()).uploadSuggestion(uploadInfo, suggestion, description);

        assertTrue(success);
        assertEquals(suggestion, receivedSuggestion);
        assertEquals(description, receivedDescription);
    }

    //TODO(AR) maybe further secure tests for Digest and for PUT

    //TODO(AR) further tests for custom XML and JSON transformations of Upload

    /* supporting classes below for security based tests */
    private final static String AUTHENTICATED_ROLE = "authenticated-user";
    private final static String TEST_USERNAME = "user1";
    private final static String TEST_PASSWORD = "pass1";

    public static class MockSecurityFilter implements ContainerRequestFilter {
        @Override
        public void filter(final ContainerRequestContext requestContext) throws IOException {
            requestContext.setSecurityContext(new MockSecurityContext(requestContext));
        }
    }

    public static class MockSecurityContext implements SecurityContext {
        private final static String BASIC = "Basic";
        private final static String DIGEST = "Digest";
        private final ContainerRequestContext requestContext;

        public MockSecurityContext(final ContainerRequestContext requestContext) {
            this.requestContext = requestContext;
        }

        @Override
        public Principal getUserPrincipal() {
            String auth = requestContext.getHeaderString("Authorization");
            if(auth != null) {
                if(auth.startsWith("Basic ")) {
                    auth = auth.replace("Basic ", "");
                    try {
                        final String userPassDecoded = new String(Base64.decodeBase64(auth), "UTF-8");
                        final String userPass[] = userPassDecoded.split(":");
                        if (userPass.length == 2) {
                            if (userPass[0].equals(TEST_USERNAME) && userPass[1].equals(TEST_PASSWORD)) {
                                return new AuthenticatedPrincipal(userPass[0]);
                            }
                        }
                    } catch (final UnsupportedEncodingException e) {
                        e.printStackTrace();
                        return null;
                    }
                } else if(auth.startsWith("Digest ")) {
                    System.out.println("NEED DIGEST AUTH");
                }
            }

            return null;
        }

        @Override
        public boolean isUserInRole(final String role) {
            final Principal principal = getUserPrincipal();
            return principal != null && role.equals(AUTHENTICATED_ROLE) && principal instanceof AuthenticatedPrincipal;
        }

        @Override
        public boolean isSecure() {
            final String scheme = requestContext.getUriInfo().getBaseUri().getScheme();
            return scheme.equals("https");
        }

        @Override
        public String getAuthenticationScheme() {
            final String auth = requestContext.getHeaderString("Authorization");
            if(auth != null && !auth.isEmpty()) {
                if (auth.startsWith(BASIC)) {
                    return SecurityContext.BASIC_AUTH;
                } else if (auth.startsWith(DIGEST)) {
                    return SecurityContext.DIGEST_AUTH;
                }
            }
            return null;
        }
    }

    public static class AuthenticatedPrincipal implements UserPrincipal {

        private final String username;

        public AuthenticatedPrincipal(final String username) {
            this.username = username;
        }

        @Override
        public String getName() {
            return username;
        }
    }
}
