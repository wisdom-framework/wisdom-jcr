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
package org.wisdom.monitor.extensions.jcr;

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

/**
 * Created by Vianney on 27/11/2015.
 */
@Controller
@Path("/monitor/jcr/explorer")
@Authenticated("Monitor-Authenticator")
public class JcrExplorerExtension extends DefaultController implements MonitorExtension {

    @View("monitor/explorer")
    Template explorer;

    @Requires
    JcrRepository jcrRepository;

    @Route(method = HttpMethod.GET, uri = "/{workspace}/{path*}")
    public Result explorer(@Parameter("path") String path,@Parameter("workspace") String workspace) throws Exception {
        path = path.isEmpty() ? "/" : "/"+path;

        Node node = null;
        if (workspace!=null){
            node = jcrRepository.getRepository().login(workspace).getNode(path);
        } else {
            node = jcrRepository.getSession().getNode(path);
        }

        JcrExplorerModel jcrExplorerModel = JcrExplorerModel.build(node);
        return ok(render(explorer, "nodeModel", jcrExplorerModel, "repository", jcrRepository,"currentWorkspace",workspace,"workspaces",jcrRepository.getSession().getWorkspace().getAccessibleWorkspaceNames()));
    }

    @Override
    public String label() {
        return "Explorer";
    }

    @Override
    public String url() {
        String currentWorkspace = jcrRepository.getSession().getWorkspace().getName();
        return "/monitor/jcr/explorer/"+currentWorkspace+"/";
    }

    @Override
    public String category() {
        return "JCR";
    }
}
