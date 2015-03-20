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
/*
 * ModeShape (http://www.modeshape.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.modeshape.web.jcr.rest.handler;

import org.apache.felix.ipojo.annotations.Requires;
import org.modeshape.common.util.CheckArg;
import org.modeshape.common.util.StringUtil;
import org.modeshape.jcr.api.JcrConstants;
import org.modeshape.web.jcr.rest.model.RestItem;
import org.modeshape.web.jcr.rest.model.RestProperty;
import org.wisdom.api.annotations.Service;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Request;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Status;
import org.wisdom.jcr.modeshape.RepositoryManager;

import javax.jcr.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Class which handles incoming requests related to {@link javax.jcr.Binary binary values}
 *
 * @author Horia Chiorean
 */
@Service(RestBinaryHandler.class)
public final class RestBinaryHandlerImpl extends AbstractHandler implements RestBinaryHandler {

    private static final String DEFAULT_MIME_TYPE = MimeTypes.BINARY;

    @Requires
    RepositoryManager repositoryManager;

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
    @Override
    public Property getBinaryProperty(Request request,
                                      String repositoryName,
                                      String workspaceName,
                                      String binaryAbsPath) throws RepositoryException {
        Session session = getSession(request, repositoryName, workspaceName);
        return session.getProperty(binaryAbsPath);
    }

    /**
     * Returns a default Content-Disposition {@link String} for a given binary property.
     *
     * @param binaryProperty a non-null {@link javax.jcr.Property}
     * @return a non-null String which represents a valid Content-Disposition.
     * @throws javax.jcr.RepositoryException if any JCR related operation involving the binary property fail.
     */
    @Override
    public String getDefaultContentDisposition(Property binaryProperty) throws RepositoryException {
        Node parentNode = getParentNode(binaryProperty);
        String parentName = parentNode.getName();
        if (StringUtil.isBlank(parentName)) {
            parentName = "binary";
        }
        return DEFAULT_CONTENT_DISPOSITION_PREFIX + parentName;
    }

    /**
     * Returns the default mime-type of a given binary property.
     *
     * @param binaryProperty a non-null {@link javax.jcr.Property}
     * @return a non-null String which represents the mime-type of the binary property.
     * @throws javax.jcr.RepositoryException if any JCR related operation involving the binary property fail.
     */
    @Override
    public String getDefaultMimeType(Property binaryProperty) throws RepositoryException {
        try {
            Binary binary = binaryProperty.getBinary();
            return binary instanceof org.modeshape.jcr.api.Binary ? ((org.modeshape.jcr.api.Binary) binary)
                    .getMimeType() : DEFAULT_MIME_TYPE;
        } catch (IOException e) {
            logger.warn("Cannot determine default mime-type", e);
            return DEFAULT_MIME_TYPE;
        }
    }

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
    @Override
    public Result updateBinary(Request request,
                               String repositoryName,
                               String workspaceName,
                               String binaryPropertyAbsPath,
                               InputStream binaryStream,
                               boolean allowCreation) throws RepositoryException {
        CheckArg.isNotNull(binaryStream, "request body");

        String parentPath = parentPath(binaryPropertyAbsPath);
        Session session = getSession(request, repositoryName, workspaceName);
        Node parent = (Node) itemAtPath(parentPath, session);

        int lastSlashInd = binaryPropertyAbsPath.lastIndexOf('/');
        String propertyName = lastSlashInd == -1 ? binaryPropertyAbsPath : binaryPropertyAbsPath.substring(lastSlashInd + 1);
        boolean createdNewValue = false;
        try {
            Property binaryProperty = null;
            try {
                binaryProperty = parent.getProperty(propertyName);
                //edit an existing property
                Binary binary = session.getValueFactory().createBinary(binaryStream);
                binaryProperty.setValue(binary);
            } catch (PathNotFoundException e) {
                if (!allowCreation) {
                    return new Result(Status.NOT_FOUND);
                }
                // create a new binary property
                Binary binary = session.getValueFactory().createBinary(binaryStream);
                binaryProperty = parent.setProperty(propertyName, binary);
                createdNewValue = true;
            }
            session.save();
            RestProperty restItem = (RestProperty) createRestItem(request, 0, session, binaryProperty);
            Result result = new Result();
            result.status(createdNewValue ? Status.CREATED : Status.OK);
            result.render(restItem);
            return result;
        } finally {
            try {
                binaryStream.close();
            } catch (IOException e) {
                logger.error("Cannot close binary stream", e);
            }
        }
    }

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
    @Override
    public Result uploadBinary(Request request,
                               String repositoryName,
                               String workspaceName,
                               String filePath,
                               InputStream binaryStream) throws RepositoryException {
        CheckArg.isNotNull(binaryStream, "request body");

        String[] segments = filePath.split("\\/");
        List<String> parsedSegments = new ArrayList<>();
        for (String segment : segments) {
            if (!StringUtil.isBlank(segment)) {
                parsedSegments.add(segment);
            }
        }
        if (parsedSegments.isEmpty()) {
            return exceptionResponse("The path '" + filePath + "' should contain at least one segment");
        }

        Session session = getSession(request, repositoryName, workspaceName);
        Node fileNode;
        try {
            fileNode = session.getNode(filePath);
        } catch (PathNotFoundException e) {
            fileNode = null;
        }

        try {
            Node content;
            int responseStatus;
            if (fileNode == null) {
                String filename = parsedSegments.get(parsedSegments.size() - 1);
                String parentPath = "/";

                Node parent = session.getNode(parentPath);
                for (int i = 0; i < parsedSegments.size() - 1; i++) {
                    String childName = parsedSegments.get(i);
                    try {
                        parent = parent.getNode(childName);
                    } catch (PathNotFoundException e) {
                        parent = parent.addNode(childName, JcrConstants.NT_FOLDER);
                    }
                }
                fileNode = parent.addNode(filename, JcrConstants.NT_FILE);
                content = fileNode.addNode(JcrConstants.JCR_CONTENT, JcrConstants.NT_RESOURCE);
                responseStatus = Status.CREATED;
            } else {
                if (!JcrConstants.NT_FILE.equalsIgnoreCase(fileNode.getPrimaryNodeType().getName())) {
                    return exceptionResponse("The node at '" + filePath + "' does not have the [nt:file] primary type");
                }
                content = fileNode.getNode(JcrConstants.JCR_CONTENT);
                responseStatus = Status.OK;
            }

            Binary binary = session.getValueFactory().createBinary(binaryStream);
            Property binaryProperty = content.setProperty(JcrConstants.JCR_DATA, binary);
            session.save();

            RestItem restItem = createRestItem(request, 0, session, binaryProperty);
            return new Result(responseStatus).render(restItem);
        } finally {
            try {
                binaryStream.close();
            } catch (IOException ioe) {
                logger.error("Cannot close binary stream", ioe);
            }
        }
    }

    @Override
    protected RepositoryManager getRepositoryManager() {
        return repositoryManager;
    }
}
