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
package org.wisdom.monitor.extensions.jcr.backup;


import org.apache.commons.io.IOUtils;
import org.apache.felix.ipojo.annotations.Requires;
import org.modeshape.jcr.api.Problems;
import org.modeshape.jcr.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.annotations.scheduler.Async;
import org.wisdom.api.http.FileItem;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.security.Authenticated;
import org.wisdom.api.templates.Template;
import org.wisdom.jcrom.runtime.JcrRepository;
import org.wisdom.monitor.service.MonitorExtension;

import javax.jcr.RepositoryException;
import java.io.*;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by KEVIN on 22/01/2016.
 */
@Controller
@Path("/monitor/jcr/backup")
@Authenticated("Monitor-Authenticator")
public class ModeshapeJcrBackupExtension extends DefaultController implements MonitorExtension {

    /**
     * The famous {@link org.slf4j.Logger}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ModeshapeJcrBackupExtension.class);

    @View("backup/index")
    Template indexTemplate;

    @Requires
    JcrRepository jcrRepository;

    @Route(method = HttpMethod.GET, uri = "")
    public Result index() throws Exception {
        return ok(render(indexTemplate));
    }

    @Route(method = HttpMethod.POST, uri = "/create")
    @Async
    public Result backup() throws IOException {
        BackupCreator backupCreator = new BackupCreator();
        try {
            return ok(backupCreator.create((Session) jcrRepository.getSession()), true);
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e);
            return internalServerError(e.getMessage());
        }
    }

    @Route(method = HttpMethod.POST, uri = "/restore")
    @Async
    public Result restore(@FormParameter("upload") FileItem fileToRestore) throws IOException, RepositoryException {
        File backupToRestoreFile = File.createTempFile("restore-" + new Date().getTime(), ".zip");
        InputStream inputStream = fileToRestore.stream();
        FileOutputStream outputStream = new FileOutputStream(backupToRestoreFile);
        IOUtils.copyLarge(inputStream, outputStream);
        inputStream.close();
        outputStream.close();
        File backupToRestoreFolder = File.createTempFile("restore-" + new Date().getTime(), "");
        backupToRestoreFolder.delete();
        backupToRestoreFolder.mkdir();
        unzip(backupToRestoreFile, backupToRestoreFolder);
        Problems problems = null;
        try {
            problems = ((Session) jcrRepository.getSession()).getWorkspace().getRepositoryManager().restoreRepository(backupToRestoreFolder);
        } catch (RepositoryException e) {
            return internalServerError("1-" + e.getMessage());
        }
        return ok(render(indexTemplate));
    }


    public void unzip(File fileToUnzip, File folderToUnzipInto) {

        byte[] buffer = new byte[1024];

        try {
            //get the zip file content
            ZipInputStream zis =
                    new ZipInputStream(new FileInputStream(fileToUnzip));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {

                String fileName = ze.getName();
                File newFile = new File(folderToUnzipInto + File.separator + fileName);

                System.out.println("file unzip : " + newFile.getAbsoluteFile());

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public String label() {
        return "Backup Tool";
    }

    @Override
    public String url() {
        return "/monitor/jcr/backup";
    }

    @Override
    public String category() {
        return "JCR";
    }

}
