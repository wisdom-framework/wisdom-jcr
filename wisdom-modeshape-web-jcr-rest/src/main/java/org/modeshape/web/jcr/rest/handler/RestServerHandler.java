package org.modeshape.web.jcr.rest.handler;

import org.modeshape.web.jcr.rest.model.RestRepositories;
import org.wisdom.api.http.Request;

/**
 * User: Antoine Mischler <antoine@dooapp.com>
 * Date: 20/03/15
 * Time: 11:34
 */
public interface RestServerHandler {

    /**
     * Returns the list of JCR repositories available on this server
     *
     * @param request the servlet request; may not be null
     * @return a list of available JCR repositories, as a {@link org.modeshape.web.jcr.rest.model.RestRepositories} instance.
     */
    RestRepositories getRepositories(Request request);

}
