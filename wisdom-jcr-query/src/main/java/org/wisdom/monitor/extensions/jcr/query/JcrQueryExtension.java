/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2016 Wisdom Framework
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
package org.wisdom.monitor.extensions.jcr.query;

import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.security.Authenticated;
import org.wisdom.api.templates.Template;
import org.wisdom.jcrom.runtime.JcrRepository;
import org.wisdom.monitor.service.MonitorExtension;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;

/**
 * User: Antoine Mischler <antoine@dooapp.com>
 * Date: 18/03/2016
 * Time: 07:26
 */
@Controller
@Path("/monitor/jcr/query")
@Authenticated("Monitor-Authenticator")
public class JcrQueryExtension extends DefaultController implements MonitorExtension {

    @View("query/query")
    Template queryTemplate;

    @Requires
    JcrRepository jcrRepository;

    @Override
    public String label() {
        return "Query";
    }

    @Route(method = HttpMethod.GET, uri = "")
    public Result index() throws Exception {
        return result("", "", null, null);
    }

    private String[] getLanguages() throws RepositoryException {
        return jcrRepository.getSession().getWorkspace().getQueryManager().getSupportedQueryLanguages();
    }

    private Result result(String query, String language, QueryResult result, Exception exception) throws RepositoryException {
        return ok(render(queryTemplate,
                "query", query,
                "language", language,
                "languages", getLanguages(),
                "result", result,
                // row iterator needs to be wrapped in an iterable to be used in th:each directives
                "rows", (Iterable<Row>) () -> {
                    try {
                        return result.getRows();
                    } catch (RepositoryException e) {
                        logger().error(e.getMessage(), e);
                    }
                    return null;
                }, "exception", exception));
    }

    @Route(method = HttpMethod.POST, uri = "/execute")
    public Result execute(@FormParameter("query") String query, @FormParameter("language") String language) throws Exception {
        try {
            Query createdQuery = jcrRepository.getSession().getWorkspace().getQueryManager().createQuery(query, language);
            QueryResult result = createdQuery.execute();
            return result(query, language, result, null);
        } catch (Exception e) {
            return result(query, language, null, e);
        }
    }

    @Override
    public String url() {
        return "/monitor/jcr/query";
    }

    @Override
    public String category() {
        return "JCR";
    }

}
