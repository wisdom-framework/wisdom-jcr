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

    Property getBinaryProperty(Request request,
                               String repositoryName,
                               String workspaceName,
                               String binaryAbsPath) throws RepositoryException;

    String getDefaultContentDisposition(Property binaryProperty) throws RepositoryException;

    String getDefaultMimeType(Property binaryProperty) throws RepositoryException;

    Result updateBinary(Request request,
                        String repositoryName,
                        String workspaceName,
                        String binaryPropertyAbsPath,
                        InputStream binaryStream,
                        boolean allowCreation) throws RepositoryException;

    Result uploadBinary(Request request,
                        String repositoryName,
                        String workspaceName,
                        String filePath,
                        InputStream binaryStream) throws RepositoryException;

}
