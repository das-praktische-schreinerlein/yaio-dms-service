/** 
 * software for document-storage
 * 
 * @FeatureDomain                DMS
 * @author                       Michael Schreiner <michael.schreiner@your-it-fellow.de>
 * @category                     DMS
 * @copyright                    Copyright (c) 2014, Michael Schreiner
 * @license                      http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package de.yaio.services.dms.api.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;

import java.io.IOException;

/** 
 * deserializer for StorageResourceVersionImpl-nodes
 * 
 * @author                       Michael Schreiner <michael.schreiner@your-it-fellow.de>
 */
public class StorageResourceDeserializer extends JsonDeserializer<StorageResource> {

    @Override
    public StorageResource deserialize(final JsonParser jsonParser, final DeserializationContext ctxt) throws IOException {
        ObjectCodec oc = jsonParser.getCodec();

        JsonNode node = oc.readTree(jsonParser);
        
        // create obj from node
        StorageResource result = deserializeJSONNode(node);
        return result;
    }
    
    protected StorageResource deserializeJSONNode(final JsonNode node) throws IOException {
        // configure
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        // etxract data
        StorageResource result = (StorageResource) mapper.treeToValue(node, StorageResourceImpl.class);;
        JsonNode childNodes = node.get("versions");
        
        // create BaseNode
        // create Childnodes
        for (JsonNode childNode : childNodes) {
            StorageResourceVersion version = (StorageResourceVersion) deserializeJSONNode(childNode);
            result.getVersions().put(version.getVersion(), version);
        }
        return result;
    }
}
