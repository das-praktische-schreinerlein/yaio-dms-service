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
package de.yaio.services.dms.storage;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.yaio.services.dms.storage.file.FileStorageResourceVersion;

/** 
 * deserializer for FileStorageResourceVersion-nodes
 * 
 * @FeatureDomain                document-storage
 * @package                      de.yaio.services.dms.storage.file
 * @author                       Michael Schreiner <michael.schreiner@your-it-fellow.de>
 * @category                     document-storage
 * @copyright                    Copyright (c) 2014, Michael Schreiner
 * @license                      http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0
 */
public class StorageResourceVersionDeserializer extends JsonDeserializer<StorageResourceVersion> {

    @Override
    public StorageResourceVersion deserialize(final JsonParser jsonParser, final DeserializationContext ctxt) throws IOException {
        ObjectCodec oc = jsonParser.getCodec();

        JsonNode node = oc.readTree(jsonParser);
        
        // create obj from node
        StorageResourceVersion result = deserializeJSONNode(node);
        return result;
    }
    
    protected StorageResourceVersion deserializeJSONNode(final JsonNode node) throws IOException {
        // configure
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        
        // create obj
        StorageResourceVersion result = (StorageResourceVersion) mapper.treeToValue(node, FileStorageResourceVersion.class);
        return result;
    }
}
