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
package org.apache.isis.core.metamodel.valuesemantics.temporal;

import java.util.function.Supplier;

/**
 * Provides html rendering for temporals with time-zone or time-offset information.
 *
 * @since 2.0
 */
public interface BadgeRenderer {

    String render(String text, Supplier<String> tooltipProvider);

    // -- FACTORIES

    /**
     * non-html, text only; ignoring tooltip
     */
    public static BadgeRenderer textual() {
        return (text, tooltipProvider)->text;
    }

    /**
     * Depends on presence of <i>Bootstrap</i>.
     */
    public static BadgeRenderer bootstrapBadgeWithTooltip() {
        return (text, tooltipProvider)->String.format("<span "
                + "class=\"badge bg-secondary\" "
                + "data-bs-container=\"body\" "
                + "data-bs-toggle=\"tooltip\" "
                + "title=\"%s\">"
                + "%s"
                + "</span>", tooltipProvider.get(), text);
    }

}
