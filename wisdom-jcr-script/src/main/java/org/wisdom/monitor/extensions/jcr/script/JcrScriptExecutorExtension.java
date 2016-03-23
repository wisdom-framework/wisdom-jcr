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
package org.wisdom.monitor.extensions.jcr.script;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.felix.ipojo.annotations.Requires;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.security.Authenticated;
import org.wisdom.api.templates.Template;
import org.wisdom.jcrom.runtime.JcrRepository;
import org.wisdom.monitor.extensions.jcr.script.json.JcrEventDeserializer;
import org.wisdom.monitor.extensions.jcr.script.json.JcrEventSerializer;
import org.wisdom.monitor.extensions.jcr.script.util.EventFormatter;
import org.wisdom.monitor.service.MonitorExtension;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventJournal;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by KEVIN on 22/01/2016.
 */
@Controller
@Path("/monitor/jcr/script")
@Authenticated("Monitor-Authenticator")
public class JcrScriptExecutorExtension extends DefaultController implements MonitorExtension {

    /**
     * The famous {@link org.slf4j.Logger}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JcrScriptExecutorExtension.class);

    public static final String JCR_MIGRATION_PREFIX = "jcr-migration-";
    private static final EventFormatter EVENT_FORMATTER = new EventFormatter();

    @View("script/script")
    Template scriptTemplate;

    @Requires
    JcrRepository jcrRepository;

    @Route(method = HttpMethod.GET, uri = "")
    public Result index() throws Exception {
        Session session = jcrRepository.getSession();
        Optional<String> currentMigrationWorkspaceOptional = Arrays.asList(session.getWorkspace().getAccessibleWorkspaceNames()).stream().filter(name -> name.startsWith(JCR_MIGRATION_PREFIX)).findFirst();
        String workspace = "";
        String script = "";
        List<Event> events = new ArrayList<>();
        if (currentMigrationWorkspaceOptional.isPresent()) {
            workspace = currentMigrationWorkspaceOptional.get();
            Node rootNode = session.getRepository().login(workspace).getRootNode();
            if (rootNode.hasNode("jcr:migrations")) {
                Node migrationNode = rootNode.getNode("jcr:migrations").getNode(workspace);
                script = migrationNode.getProperty("script").getString();
                ObjectMapper mapper = new ObjectMapper();
                SimpleModule module = new SimpleModule();
                module.addDeserializer(Event.class, new JcrEventDeserializer());
                mapper.registerModule(module);
                events = mapper.readValue(migrationNode.getProperty("events").getString(), mapper.getTypeFactory().constructCollectionType(List.class, Event.class));
            }
        }
        return ok(render(scriptTemplate, "script", script, "workspace", workspace, "events", events, "eventFormatter", EVENT_FORMATTER));
    }

    @Route(method = HttpMethod.POST, uri = "/execute")
    public Result execute(@FormParameter("script") String script, @FormParameter("workspace") String workspace, @FormParameter("executeDirectly") boolean executeDirectly) throws Exception {
        if (workspace == null || workspace.isEmpty()) {
            if (executeDirectly) {
                return executeDirectly(script);
            } else {
                return preExecuteScript(script);
            }
        } else {
            return executeScript(script, workspace);
        }
    }

    private Result executeDirectly(String script) {
        long migrationTimestamp = System.currentTimeMillis();
        Timestamp timestamp = new Timestamp(migrationTimestamp);
        String workspace = JCR_MIGRATION_PREFIX + new SimpleDateFormat("yyyyMMddHHmmss").format(timestamp);
        Session originalSession = jcrRepository.getSession();
        final ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

            EventJournal journal = originalSession.getWorkspace().getObservationManager().getEventJournal();

            ImportCustomizer importCustomizer = new ImportCustomizer();
            importCustomizer.addStarImports("javax.jcr");
            importCustomizer.addStarImports("javax.jcr.query");

            CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
            compilerConfiguration.addCompilationCustomizers(importCustomizer);

            Binding binding = new Binding();
            binding.setProperty("session", originalSession);
            GroovyShell shell = new GroovyShell(binding, compilerConfiguration);

            long startMigrationTimeMillis = System.currentTimeMillis();
            shell.evaluate(script);
            originalSession = (Session) binding.getProperty("session");
            originalSession.save();
            List<Event> events = new ArrayList<>();
            if (journal != null) {
                journal.skipTo(startMigrationTimeMillis);
                journal.forEachRemaining(event -> events.add((Event) event));
            }
            Node systemNode = originalSession.getRootNode();
            Node migrationsNode = null;
            if (systemNode.hasNode("jcr:migrations")) {
                migrationsNode = systemNode.getNode("jcr:migrations");
            } else {
                migrationsNode = systemNode.addNode("jcr:migrations");
            }
            Node migration = migrationsNode.addNode(workspace);
            migration.setProperty("executeDate", migrationTimestamp);
            migration.setProperty("script", script);
            migration.setProperty("events", listToJsonString(events));
            originalSession.save();

            if (events.isEmpty()) {
                throw new Exception("Your script doesn't affect any node.");
            }
            return ok(render(scriptTemplate, "workspace", "", "events", new ArrayList<>(), "script", "", "info", "Executed successfully!"));
        } catch (Exception e) {
            String[] ss = ExceptionUtils.getRootCauseStackTrace(e);
            return internalServerError(render(scriptTemplate, "exception", e, "stackTrace", StringUtils.join(ss, "\n"), "workspace", "", "events", new ArrayList(), "script", script));
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    @Route(method = HttpMethod.GET, uri = "/abort")
    public Result abort() throws Exception {
        final ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            Session session = jcrRepository.getSession();
            Optional<String> currentMigrationWorkspaceOptional = Arrays.asList(session.getWorkspace().getAccessibleWorkspaceNames()).stream().filter(name -> name.startsWith(JCR_MIGRATION_PREFIX)).findFirst();
            String workspace = "";
            if (currentMigrationWorkspaceOptional.isPresent()) {
                workspace = currentMigrationWorkspaceOptional.get();
                session.getWorkspace().deleteWorkspace(workspace);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
        return index();
    }

    private Result executeScript(String script, String workspace) throws RepositoryException {
        final ClassLoader original = Thread.currentThread().getContextClassLoader();
        Session originalSession = jcrRepository.getSession();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            originalSession.save();
            Session migrationSession = jcrRepository.getRepository().login(workspace);
            Node migrationNode = migrationSession.getRootNode().getNode("jcr:migrations").getNode(workspace);
            migrationNode.setProperty("applied", true);
            originalSession.getWorkspace().clone(workspace, "/", "/", true);
            originalSession.getWorkspace().deleteWorkspace(workspace);
            return ok(render(scriptTemplate, "workspace", "", "events", new ArrayList(), "script", "", "info", "Executed successfully!"));
        } catch (Exception e) {
            originalSession.getWorkspace().deleteWorkspace(workspace);
            return internalServerError(render(scriptTemplate, "exception", e, "stackTrace", StringUtils.join(ExceptionUtils.getRootCauseStackTrace(e), "\n"), "workspace", workspace, "events", new ArrayList(), "script", script));
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    private Result preExecuteScript(String script) throws RepositoryException {
        long migrationTimestamp = System.currentTimeMillis();
        Timestamp timestamp = new Timestamp(migrationTimestamp);
        String workspace = JCR_MIGRATION_PREFIX + new SimpleDateFormat("yyyyMMddHHmmss").format(timestamp);
        Session originalSession = jcrRepository.getSession();

        final ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            originalSession.getWorkspace().createWorkspace(workspace, originalSession.getWorkspace().getName());
            Session session = jcrRepository.getRepository().login(workspace);
            EventJournal journal = session.getWorkspace().getObservationManager().getEventJournal();

            ImportCustomizer importCustomizer = new ImportCustomizer();
            importCustomizer.addStarImports("javax.jcr");
            importCustomizer.addStarImports("javax.jcr.query");

            CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
            compilerConfiguration.addCompilationCustomizers(importCustomizer);

            Binding binding = new Binding();
            binding.setProperty("session", session);
            GroovyShell shell = new GroovyShell(binding, compilerConfiguration);

            long startMigrationTimeMillis = System.currentTimeMillis();
            shell.evaluate(script);
            session = (Session) binding.getProperty("session");
            session.save();
            List<Event> events = new ArrayList<>();
            if (journal != null) {
                journal.skipTo(startMigrationTimeMillis);
                journal.forEachRemaining(event -> events.add((Event) event));
            }
            Node systemNode = session.getRootNode();
            Node migrationsNode = null;
            if (systemNode.hasNode("jcr:migrations")) {
                migrationsNode = systemNode.getNode("jcr:migrations");
            } else {
                migrationsNode = systemNode.addNode("jcr:migrations");
            }
            Node migration = migrationsNode.addNode(workspace);
            migration.setProperty("executeDate", migrationTimestamp);
            migration.setProperty("script", script);
            migration.setProperty("events", listToJsonString(events));
            session.save();

            if (events.isEmpty()) {
                throw new Exception("Your script doesn't affect any node.");
            }
            return ok(render(scriptTemplate, "events", events, "workspace", workspace, "eventFormatter", EVENT_FORMATTER, "script", script));
        } catch (Exception e) {
            originalSession.getWorkspace().deleteWorkspace(workspace);
            String[] ss = ExceptionUtils.getRootCauseStackTrace(e);
            return internalServerError(render(scriptTemplate, "exception", e, "stackTrace", StringUtils.join(ss, "\n"), "workspace", "", "events", new ArrayList(), "script", script));
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    @Override
    public String label() {
        return "Script Executor";
    }

    @Override
    public String url() {
        return "/monitor/jcr/script";
    }

    @Override
    public String category() {
        return "JCR";
    }

    public String listToJsonString(List list) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Event.class, new JcrEventSerializer()); // assuming serializer declares correct class to bind to
        mapper.registerModule(module);
        mapper.writeValue(out, list);
        final byte[] data = out.toByteArray();
        return new String(data);
    }

}
