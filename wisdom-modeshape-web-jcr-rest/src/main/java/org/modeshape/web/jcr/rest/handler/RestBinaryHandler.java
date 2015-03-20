/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.modeshape.web.jcr.rest.handler;

import org.wisdom.api.http.Request;
import org.wisdom.api.http.Result;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.io.InputStream;

/**
 * User: Antoine Mischler <antoine@dooapp.com>
 * Date: 19/03/15
 * Time: 11:45
 */
public interface RestBinaryHandler {

    /**
     * The default content disposition prefix, used when serving binary content.
     */
    String DEFAULT_CONTENT_DISPOSITION_PREFIX = "attachment;filename=";

    /**
     * Returns a binary {@link javax.jcr.Property} for the given repository, workspace and path.
     *
     * @param request        a non-null {@link Request} request
     * @param repositoryName a non-null {@link String} representing the name of a repository.
     * @param workspaceName  a non-null {@link String} representing the name of a workspace.
     * @param binaryAbsPath  a non-null {@link String} representing the absolute path to a binary property.
     * @return the {@link javax.jcr.Property} instance which is located at the given path. If such a property is not located, an exception
     * is thrown.
     * @throws javax.jcr.RepositoryException if any JCR related operation fails, including the case when the path to the property isn't valid.
     */
    Property getBinaryProperty(Request request,
                               String repositoryName,
                               String workspaceName,
                               String binaryAbsPath) throws RepositoryException;

    /**
     * Returns a default Content-Disposition {@link String} for a given binary property.
     *
     * @param binaryProperty a non-null {@link javax.jcr.Property}
     * @return a non-null String which represents a valid Content-Disposition.
     * @throws javax.jcr.RepositoryException if any JCR related operation involving the binary property fail.
     */
    String getDefaultContentDisposition(Property binaryProperty) throws RepositoryException;

    /**
     * Returns the default mime-type of a given binary property.
     *
     * @param binaryProperty a non-null {@link javax.jcr.Property}
     * @return a non-null String which represents the mime-type of the binary property.
     * @throws javax.jcr.RepositoryException if any JCR related operation involving the binary property fail.
     */
    String getDefaultMimeType(Property binaryProperty) throws RepositoryException;

    /**
     * Updates the {@link javax.jcr.Property property} at the given path with the content from the given {@link java.io.InputStream}.
     *
     * @param request               a non-null {@link Request} request
     * @param repositoryName        a non-null {@link String} representing the name of a repository.
     * @param workspaceName         a non-null {@link String} representing the name of a workspace.
     * @param binaryPropertyAbsPath a non-null {@link String} representing the absolute path to a binary property.
     * @param binaryStream          an {@link java.io.InputStream} which represents the new content of the binary property.
     * @param allowCreation         a boolean flag which indicates what the behavior should be in case such a property does
     *                              not exist on its parent node: if the flag is {@code true}, the property will be created, otherwise a response code indicating
     *                              the absence is returned.
     * @return a {@link Result} object, which is either OK and contains the rest representation of the binary property, or is
     * NOT_FOUND.
     * @throws javax.jcr.RepositoryException if any JCR related operations fail
     * @throws IllegalArgumentException      if the given input stream is {@code null}
     */
    Result updateBinary(Request request,
                        String repositoryName,
                        String workspaceName,
                        String binaryPropertyAbsPath,
                        InputStream binaryStream,
                        boolean allowCreation) throws RepositoryException;

    /**
     * Uploads a binary value at the given path, creating each missing path segment as an [nt:folder]. The binary is uploaded
     * as an [nt:resource] node of a [nt:file] node, both of which are created.
     *
     * @param request        a {@link Request}, never {@code null}
     * @param repositoryName a {@link String}, the repository name; never {@code null}
     * @param workspaceName  a {@link String}, the workspace name; never {@code null}
     * @param filePath       a {@link String}, file absolute path to the [nt:file] node; never {@code null}
     * @param binaryStream   an {@link java.io.InputStream} from which the binary content will be read.
     * @return a {@link Result} object, never {@code null}
     * @throws javax.jcr.RepositoryException if anything unexpected fails.
     */
    Result uploadBinary(Request request,
                        String repositoryName,
                        String workspaceName,
                        String filePath,
                        InputStream binaryStream) throws RepositoryException;

}
