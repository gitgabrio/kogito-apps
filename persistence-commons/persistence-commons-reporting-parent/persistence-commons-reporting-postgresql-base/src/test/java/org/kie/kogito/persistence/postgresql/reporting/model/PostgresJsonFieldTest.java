/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.kogito.persistence.postgresql.reporting.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PostgresJsonFieldTest {

    static final PostgresJsonField FIELD = new PostgresJsonField("field1", JsonType.STRING);
    static final int FIELD_HASHCODE = FIELD.hashCode();

    @Test
    void testEquality() {
        assertEquals(FIELD,
                FIELD);
        assertNotEquals(FIELD,
                new PostgresJsonField("field1", JsonType.NUMBER));
        assertNotEquals(FIELD,
                new PostgresJsonField("field2", JsonType.STRING));
    }

    @Test
    void testHashCode() {
        assertEquals(FIELD_HASHCODE,
                FIELD.hashCode());
        assertNotEquals(FIELD_HASHCODE,
                new PostgresJsonField("field1", JsonType.NUMBER).hashCode());
        assertNotEquals(FIELD_HASHCODE,
                new PostgresJsonField("field2", JsonType.STRING).hashCode());
    }
}
