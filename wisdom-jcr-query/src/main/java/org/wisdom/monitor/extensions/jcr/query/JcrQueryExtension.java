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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Antoine Mischler <antoine@dooapp.com>
 * Date: 18/03/2016
 * Time: 07:26
 */
@Controller
@Path("/monitor/jcr/query")
@Authenticated("Monitor-Authenticator")
public class JcrQueryExtension extends DefaultController implements MonitorExtension {

    class RowValues {

        Value[] values;

        Node[] nodes;

        public Value[] getValues() {
            return values;
        }

        public Node[] getNodes() {
            return nodes;
        }

    }

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

        List<RowValues> rows = null;
        if (result != null) {
            RowIterator rowIterator = result.getRows();
            rows = new ArrayList((int) rowIterator.getSize());
            while (rowIterator.hasNext()) {
                Row row = (Row) rowIterator.next();
                RowValues rowValues = new RowValues();
                rowValues.values = row.getValues();
                int selectorsSize = result.getSelectorNames().length;
                if (selectorsSize > 0) {
                    rowValues.nodes = new Node[selectorsSize];
                    int i = 0;
                    for (String selector : result.getSelectorNames()) {
                        rowValues.nodes[i] = row.getNode(selector);
                        i++;
                    }
                } else {
                    rowValues.nodes = new Node[]{row.getNode()};
                }
                rows.add(rowValues);
            }
        }
        return ok(render(queryTemplate,
                "query", query,
                "language", language,
                "languages", getLanguages(),
                "result", result,
                "rows", rows,
                "exception", exception));
    }

    @Route(method = HttpMethod.POST, uri = "/execute")
    public Result execute(@FormParameter("query") String query, @FormParameter("language") String language) throws Exception {
        try {
            final ClassLoader original = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
                Query createdQuery = jcrRepository.getSession().getWorkspace().getQueryManager().createQuery(query, language);
                QueryResult result = createdQuery.execute();
                return result(query, language, result, null);
            } finally {
                Thread.currentThread().setContextClassLoader(original);
            }
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
