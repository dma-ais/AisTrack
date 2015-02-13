/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.ais.track.store;

import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public final class MapDb<K,V> {
    
    static final Logger LOG = LoggerFactory.getLogger(MapDb.class);
    
    private final DB db;
    private final BTreeMap<K,V> map;
    
    private MapDb(DB db, BTreeMap<K,V> map) {
        this.db = db;  
        this.map = map;
    }
    
    public BTreeMap<K, V> getMap() {
        return map;
    }
    
    public void close() {
        db.close();
    }
    
    public DB getDb() {
        return db;
    }

    /**
     * Creates a DB with the given name in the given directory
     * @param backupDir the directory
     * @param dbName the DB name
     * @return the database
     */
    public static <K,V> MapDb<K,V> create(String backupDir, String dbName) {
        try {
            DB db;
            try {
                // First attempt
                // If the database was not shut down properly, it may be corrupted.
                db = createFileDB(backupDir, dbName);
            } catch (Throwable e) {
                LOG.error("Failed to create/load MapDb database " + dbName, e);
                // Delete the DB files
                deleteDBFiles(backupDir, dbName);

                // Second attempt
                db = createFileDB(backupDir, dbName);
            }

            //DB db = DBMaker.newFileDB(new File(backupDir + "/" + dbName)).make();
            BTreeMap<K,V> map = db.getTreeMap(dbName);
            map.size();
            return new MapDb<>(db, map);
        } catch (Throwable e) {
            LOG.error("Failed to create/load MapDb database", e);
        }
        return null;
    }

    /**
     * Creates a DB with the given name in the given directory
     * @param backupDir the directory
     * @param dbName the DB name
     * @return the database
     */
    private static DB createFileDB(String backupDir, String dbName) {
        return DBMaker
                .newFileDB(new File(backupDir + "/" + dbName))
                .transactionDisable()
                .make();
    }

    /**
     * Deletes all files with the given DB name
     * @param backupDir the DB directory
     * @param dbName the database name
     */
    private static void deleteDBFiles(String backupDir, String dbName) {
        try {
            Files.walkFileTree(Paths.get(backupDir), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().startsWith(dbName)) {
                        LOG.warn("Deleting DB file  :" + file);
                        Files.delete(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            LOG.error("Failed cleaning up DB folder: " + e.getMessage());
        }
    }
}
