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
package org.wisdom.monitor.extensions.jcr.modeshape.monitoring;


import org.apache.felix.ipojo.annotations.Requires;
import org.modeshape.jcr.api.monitor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.security.Authenticated;
import org.wisdom.api.templates.Template;
import org.wisdom.jcrom.runtime.JcrRepository;
import org.wisdom.monitor.service.MonitorExtension;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by KEVIN on 22/01/2016.
 */
@Controller
@Path("/monitor/jcr/monitoring")
@Authenticated("Monitor-Authenticator")
public class JcrModeshapeMonitoringExtension extends DefaultController implements MonitorExtension {

    /**
     * The famous {@link org.slf4j.Logger}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JcrModeshapeMonitoringExtension.class);

    @View("modeshape/monitoring/index")
    Template indexTemplate;

    @Requires
    JcrRepository jcrRepository;

    public static ValueMetric valueMetric = ValueMetric.QUERY_COUNT;
    public static DurationMetric durationMetric = DurationMetric.SEQUENCER_EXECUTION_TIME;
    public static Window window = Window.PREVIOUS_60_MINUTES;

    @Route(method = HttpMethod.GET, uri = "")
    public Result index() throws Exception {
        org.modeshape.jcr.api.Workspace workspace = (org.modeshape.jcr.api.Workspace) jcrRepository.getSession().getWorkspace();
        RepositoryMonitor monitor = workspace.getRepositoryManager().getRepositoryMonitor();
        Set<ValueMetric> availableValueMetrics = monitor.getAvailableValueMetrics();
        Set<DurationMetric> availableDurationMetrics = monitor.getAvailableDurationMetrics();
        Set<Window> availableWindows = monitor.getAvailableWindows();

        History durationHistory = workspace.getRepositoryManager().getRepositoryMonitor().getHistory(durationMetric, window);
        History valueHistory = workspace.getRepositoryManager().getRepositoryMonitor().getHistory(valueMetric, window);
        DurationActivity[] longestRunning = workspace.getRepositoryManager().getRepositoryMonitor().getLongestRunning(durationMetric);

        return ok(render(indexTemplate,
                "availableValueMetrics", availableValueMetrics,
                "valueMetric", valueMetric,
                "availableDurationMetrics", availableDurationMetrics,
                "durationMetric", durationMetric,
                "availableWindows", availableWindows,
                "window", window,
                "longestRunning", longestRunning,
                "valueHistory", valueHistory,
                "durationHistory", durationHistory,
                "timeUnit", TimeUnit.SECONDS,
                "query", "query"));
    }

    @Route(method = HttpMethod.POST, uri = "/parameters")
    public Result updateParams(@Body MetricsParameters metricsParameters) throws Exception {
        LOGGER.debug(metricsParameters.getValueMetric() + " " + metricsParameters.getDurationMetric() + " " + metricsParameters.getWindow());
        valueMetric = metricsParameters.getValueMetric();
        durationMetric = metricsParameters.getDurationMetric();
        window = metricsParameters.getWindow();
        return redirect("/monitor/jcr/monitoring");
    }


    @Override
    public String label() {
        return "Monitoring";
    }

    @Override
    public String url() {
        return "/monitor/jcr/monitoring";
    }

    @Override
    public String category() {
        return "JCR";
    }

}
