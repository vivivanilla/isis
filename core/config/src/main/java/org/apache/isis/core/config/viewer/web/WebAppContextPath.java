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
package org.apache.isis.core.config.viewer.web;

import java.io.Serializable;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.annotation.Priority;
import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.resources._Resources;
import org.apache.isis.core.config.IsisModuleCoreConfig;

import lombok.Getter;
import lombok.val;

/**
 * This class is {@link Serializable} so that it can be injected into Wicket components.
 */
@Service
@Singleton
@Named(IsisModuleCoreConfig.NAMESPACE + "..WebAppContextPath")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class WebAppContextPath implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Either "" or "/xxx".
     */
    @Getter
    private String contextPath = "";

//    public Optional<String> getContextPath() {
//        return hasContextPath()
//                ? Optional.of(contextPath)
//                : Optional.empty();
//    }

    /**
     * @param contextPath - any form allowed: leading or trailing '/',
     * no matter what, gets normalized
     */
    public void setContextPath(final @Nullable String contextPath) {
        this.contextPath = normalizeContextPath(contextPath);
    }

    /**
     * @return whether a context-path is in use
     */
    public boolean hasContextPath() {
        return _Strings.isNotEmpty(contextPath);
    }

    /**
     * @param localPath - last part of an URL to be prefixed (nullable)
     * @return (non-null)
     */
    public String prependContextPath(final @Nullable String localPath) {
        if(localPath==null) {
            return getContextPath();
        }
        if(!hasContextPath()) {
            return localPath;
        }
        return getContextPath() + _Strings.prefix(localPath, "/");
    }

    public String appendContextPath(final @Nullable String path) {
        if(path==null) {
            return getContextPath();
        }
        if(!hasContextPath()) {
            return path;
        }
        return _Strings.suffix(path, "/") +
                (getContextPath().startsWith("/")
                    ? getContextPath().substring(1)
                    : path);
    }

    /**
     * @param urlOrLocalPath - when detected to be a localPath prepends the context-path if any,
     * identity operator otherwise
     */
    @Nullable
    public String prependContextPathIfLocal(final @Nullable String urlOrLocalPath) {
        if(urlOrLocalPath==null) {
            return null;
        }
        if(_Resources.isLocalResource(urlOrLocalPath)) {
            return _Strings.prefix(this.prependContextPath(urlOrLocalPath), "/");
        }
        return urlOrLocalPath;
    }

    public Optional<String> prependContextPathIfLocal(final Optional<String> urlOrLocalPath) {
        return urlOrLocalPath.map(this::prependContextPathIfLocal);
    }

    // -- HELPER

    private final Pattern pattern = Pattern.compile("^[/]*(.+?)[/]*$");

    /**
     * make sure result is either empty or has a leading slash followed by a non-empty string
     */
    private String normalizeContextPath(final @Nullable String contextPath) {
        if(contextPath == null) {
            return "";
        }
        val matcher = pattern.matcher(contextPath);
        val path = matcher.matches()
                ? matcher.group(1)
                        : "";

        if("".equals(path) || "/".equals(path)) {
            return "";
        }

        return ensureLeadingSlash(path);
    }

    @Nullable
    private String ensureLeadingSlash(final @Nullable String url) {
        if(url==null || url.length()<2) {
            return url;
        }
        return !url.startsWith("/")
                ? "/" + url
                        : url;
    }


}
