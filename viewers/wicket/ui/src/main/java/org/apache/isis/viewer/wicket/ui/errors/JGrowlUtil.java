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
package org.apache.isis.viewer.wicket.ui.errors;

import org.apache.wicket.util.string.Strings;

import org.apache.isis.commons.internal.base._Text;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.interaction.session.MessageBroker;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JGrowlUtil {

    static enum MessageSeverity {
        INFO {
            @Override long delayMillis(final IsisConfiguration.Viewer.Wicket.MessagePopups messagePopups) {
                return messagePopups.getInfoDelay().toMillis();
            }
        },
        WARNING {
            @Override long delayMillis(final IsisConfiguration.Viewer.Wicket.MessagePopups messagePopups) {
                return messagePopups.getWarningDelay().toMillis();
            }
        }, // sticky
        DANGER{
            @Override long delayMillis(final IsisConfiguration.Viewer.Wicket.MessagePopups messagePopups) {
                return messagePopups.getErrorDelay().toMillis();
            }
        } // sticky
        ;

        public String cssClassSuffix() {
            return name().toLowerCase();
        }

        abstract long delayMillis(IsisConfiguration.Viewer.Wicket.MessagePopups messagePopups);
    }

    public String asJGrowlCalls(final MessageBroker messageBroker, final IsisConfiguration configuration) {
        val buf = new StringBuilder();

        val messagePopupConfig = configuration.getViewer().getWicket().getMessagePopups();
        for (String info : messageBroker.drainMessages()) {
            addJGrowlCall(info, JGrowlUtil.MessageSeverity.INFO, messagePopupConfig, buf);
        }

        for (String warning : messageBroker.drainWarnings()) {
            addJGrowlCall(warning, JGrowlUtil.MessageSeverity.WARNING, messagePopupConfig, buf);
        }

        messageBroker.drainApplicationError()
        .ifPresent(error->
            addJGrowlCall(error, MessageSeverity.DANGER, messagePopupConfig, buf));

        return buf.toString();
    }

    // -- HELPER

    private void addJGrowlCall(
            final String origMsg,
            final MessageSeverity severity,
            final IsisConfiguration.Viewer.Wicket.MessagePopups messagePopups,
            final StringBuilder buf) {

        final CharSequence escapedMsg = escape(origMsg);
        buf.append("$.growl(\"")
        .append(escapedMsg)
        .append('"');
        buf.append(", {");
        buf.append("type: \"").append(severity.cssClassSuffix()).append('"');
        buf.append(String.format(", delay: %d", severity.delayMillis(messagePopups)));
        buf.append(String.format(", placement: { from: '%s', align: '%s' }", messagePopups.getPlacement().getVertical().name().toLowerCase(), messagePopups.getPlacement().getHorizontal().name().toLowerCase()));
        buf.append(String.format(", offset: %d", messagePopups.getOffset()));
        buf.append('}');
        buf.append(");\n");
    }

    String escape(final String origMsg) {

        _Text.normalize(origMsg);

        final String escaped = Strings.escapeMarkup(origMsg).toString();

        // convert (what would originally have been either) ' or " to '
        return escaped
                .replace("&quot;", "'")
                .replace("&#039;", "'")
                .replace("\r", "")
                .replace("\t", " ")
                .replace("\n", "<br/>");
    }
}
