/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.isis.applib.services.bookmark;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.isis.applib.services.bookmark.idstringifiers.IdStringifierForLong;

import lombok.val;

class IdStringifierForLong_Test {

    public static Stream<Arguments> roundtrip() {
        return Stream.of(
                Arguments.of(Long.MAX_VALUE),
                Arguments.of(Long.MIN_VALUE),
                Arguments.of(0L),
                Arguments.of(12345L),
                Arguments.of(-12345L)
        );
    }

    static class Customer {}

    @ParameterizedTest
    @MethodSource()
    void roundtrip(Long value) {

        val stringifier = new IdStringifierForLong();

        String stringified = stringifier.enstring(value);
        Long parse = stringifier.destring(stringified, Customer.class);

        assertThat(parse).isEqualTo(value);
    }

}
