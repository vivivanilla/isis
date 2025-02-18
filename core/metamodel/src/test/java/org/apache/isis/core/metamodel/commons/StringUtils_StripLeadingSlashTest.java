/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.core.metamodel.commons;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class StringUtils_StripLeadingSlashTest {

    @Test
    public void shouldStripIfThereIsOne() {
        assertThat(StringExtensions.stripLeadingSlash("/foobar"), is("foobar"));
    }

    @Test
    public void shouldLeaveUnchangedIfThereIsNone() {
        assertThat(StringExtensions.stripLeadingSlash("foobar"), is("foobar"));
    }

    @Test
    public void shouldConvertSolitarySlashToEmptyString() {
        assertThat(StringExtensions.stripLeadingSlash("/"), is(""));
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailOnNull() {
        StringExtensions.stripLeadingSlash(null);
    }

}
