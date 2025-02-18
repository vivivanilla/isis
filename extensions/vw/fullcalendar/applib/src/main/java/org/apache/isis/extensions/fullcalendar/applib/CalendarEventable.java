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
package org.apache.isis.extensions.fullcalendar.applib;

import org.apache.isis.extensions.fullcalendar.applib.value.CalendarEvent;

/**
 * @since 2.0 {@index}
 */
public interface CalendarEventable {

    /**
     * The name of the calendar to which this event belongs.
     * <p>
     * For example, an <code>Employee</code> might have a <code>employedOn</code> property,
     * so the calendar name would be 'employedOn'.
     * <p>
     * If there is possibly more than one date associated with the entity, then use
     * {@link Calendarable} instead.
     */
    String getCalendarName();

    CalendarEvent toCalendarEvent();

}
