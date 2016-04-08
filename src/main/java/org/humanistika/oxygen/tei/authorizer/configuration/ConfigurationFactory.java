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
package org.humanistika.oxygen.tei.authorizer.configuration;

import org.humanistika.oxygen.tei.authorizer.configuration.impl.XmlConfiguration;

/**
 * Factory for creating instances of TeiCompleter
 *
 * @author Adam Retter, Evolved Binary Ltd <adam.retter@googlemail.com>
 * @version 1.0
 * @serial 20160405
 */
public class ConfigurationFactory extends org.humanistika.oxygen.tei.completer.configuration.ConfigurationFactory {
    private final static ConfigurationFactory instance = new ConfigurationFactory();
    private final static String CONFIG_FOLDER_NAME = ".bcdh-tei-authorizer";

    private ConfigurationFactory() {
        super();
    }

    public final static ConfigurationFactory getInstance() {
        return instance;
    }

    /**
     * Loads the Configuration
     *
     * @return The loaded Configuration
     */
    @Override
    public Configuration loadConfiguration() {
        return new XmlConfiguration(configDir.resolve(CONFIG_FILE_NAME_PREFIX + ".xml"));
    }

    protected String getConfigFolderName() {
        return CONFIG_FOLDER_NAME;
    }
}
