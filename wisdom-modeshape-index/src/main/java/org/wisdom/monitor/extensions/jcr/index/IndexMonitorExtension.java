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
package org.wisdom.monitor.extensions.jcr.index;

import org.apache.felix.ipojo.annotations.Requires;
import org.modeshape.jcr.api.index.IndexDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.security.Authenticated;
import org.wisdom.api.templates.Template;
import org.wisdom.jcrom.runtime.JcrRepository;
import org.wisdom.monitor.service.MonitorExtension;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by KEVIN on 22/01/2016.
 */
@Controller
@Path("/monitor/jcr/index")
@Authenticated("Monitor-Authenticator")
public class IndexMonitorExtension extends DefaultController implements MonitorExtension {

    /**
     * The famous {@link org.slf4j.Logger}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexMonitorExtension.class);

    @View("index/index")
    Template indexTemplate;

    @Requires
    JcrRepository jcrRepository;

    @Route(method = HttpMethod.GET, uri = "")
    public Result index() throws Exception {
        org.modeshape.jcr.api.Session session = (org.modeshape.jcr.api.Session) jcrRepository.getSession();
        Map<String, String> indexes = new HashMap();
        for (IndexDefinition index : session.getWorkspace().getIndexManager().getIndexDefinitions().values()) {
            indexes.put(index.getName(), session.getWorkspace().getIndexManager().getIndexStatus(index.getProviderName(), index.getName(), session.getWorkspace().getName()).name());
        }
        return ok(render(indexTemplate, "indexes", indexes.entrySet()));
    }

    @Route(method = HttpMethod.POST, uri = "/index/rebuild")
    public Result rebuildIndex() throws Exception {
        org.modeshape.jcr.api.Session session = (org.modeshape.jcr.api.Session) jcrRepository.getSession();
        session.getWorkspace().reindexAsync();
        return index();
    }

    @Override
    public String label() {
        return "Index Monitor";
    }

    @Override
    public String url() {
        return "/monitor/jcr/index";
    }

    @Override
    public String category() {
        return "JCR";
    }

}
