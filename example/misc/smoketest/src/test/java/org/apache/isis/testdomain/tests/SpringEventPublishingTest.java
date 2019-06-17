/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.testdomain.tests;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.Getter;
import lombok.Value;
import lombok.val;

class SpringEventPublishingTest {

    @Inject TestPublisher testPublisher;
    @Inject TestListener testListener;
    
    @Test
    void firedEventShouldBeReceivedImmediately() {
        
        val history = testListener.getHistory();
        
        testPublisher.fireHelloWorld();
        
        assertEquals("Hello World!", history.toString());
    }
    
    // -- HELPER
    
    //@Configuration
    static class TestConfig {
    
        //@Bean
//        Event<GenericSpringEvent<String>> createEvent(ApplicationEventPublisher publisher) {
//            return _Spring.event(publisher);
//        }
        
    }
    
    @Singleton
    public static class TestPublisher {
        
        @Inject Event<GenericSpringEvent<String>> genericEvent;
        
        public void fireHelloWorld() {
            genericEvent.fire(GenericSpringEvent.of("Hello World!"));
        }
        
    }
    
    @Singleton
    public static class TestListener {
        
        @Getter private final StringBuilder history = new StringBuilder();

        //@EventListener(GenericSpringEvent.class)
        public void receiveHelloWorld(@Observes GenericSpringEvent<String> event) {
            history.append(event.what);
        }
        
    }
    
    @Value(staticConstructor = "of")
    static class GenericSpringEvent<T> {
        private T what;
    }
        
}
