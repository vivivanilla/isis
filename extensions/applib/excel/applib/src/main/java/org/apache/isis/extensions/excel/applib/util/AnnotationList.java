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
package org.apache.isis.extensions.excel.applib.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @since 2.0 {@index}
 */
public class AnnotationList {

    AnnotationList(List<AnnotationTriplet> list){
        this.list = list;
    }

    List<AnnotationTriplet> list;

    List<AnnotationTriplet> getByAnnotation_OrderBy_OrderAscending(String annotation){
        List<AnnotationTriplet> result = new ArrayList<>();
        for (AnnotationTriplet a : list){
            if (a.getAnnotation().equals(annotation)){
                result.add(a);
            }
        }
        Collections.sort(result);
        return result;
    }
}
