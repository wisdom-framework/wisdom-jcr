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
import org.modeshape.jcr.api.Problems;
import org.modeshape.jcr.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by KEVIN on 08/03/2016.
 */
public class BackupCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModeshapeJcrBackupExtension.class);

    public File create(Session session) throws Exception {
        File file = null;

        file = File.createTempFile("backup" + new Date().getTime(), "");
        file.delete();
        if (!file.mkdir()) {
            throw new IOException("Couldn't not create folder");
        }

        Problems problems = session.getWorkspace().getRepositoryManager().backupRepository(file);

        if (problems.hasProblems()) {
            final String[] problemsMessage = {""};
            problems.forEach(problem -> {
                problemsMessage[0] += problem.getMessage();
                LOGGER.debug(problem.getMessage(), problem.getThrowable());
            });
            throw new Exception(problemsMessage[0]);
        }
        File backupArchive = File.createTempFile("backup" + new Date().getTime(), ".zip");
        zip(file, backupArchive);
        return backupArchive;
    }

    public static void zip(final File folderToZip, final File fileToZipTo) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(fileToZipTo))) {
            processFolder(folderToZip, zipOutputStream, folderToZip.getPath().length() + 1);
        }
    }

    private static void processFolder(final File folder, final ZipOutputStream zipOutputStream, final int prefixLength)
            throws IOException {
        for (final File file : folder.listFiles()) {
            if (file.isFile()) {
                final ZipEntry zipEntry = new ZipEntry(file.getPath().substring(prefixLength));
                zipOutputStream.putNextEntry(zipEntry);
                try (FileInputStream inputStream = new FileInputStream(file)) {
                    IOUtils.copy(inputStream, zipOutputStream);
                }
                zipOutputStream.closeEntry();
            } else if (file.isDirectory()) {
                processFolder(file, zipOutputStream, prefixLength);
            }
        }
    }
}
