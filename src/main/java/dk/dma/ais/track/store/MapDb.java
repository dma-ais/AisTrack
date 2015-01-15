package dk.dma.ais.track.store;

import java.io.File;

import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapDb<K,V> {
    
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
    
    public static <K,V> MapDb<K,V> create(String backupDir, String dbName) {
        try {
            DB db = DBMaker.newFileDB(new File(backupDir + "/" + dbName)).transactionDisable().make();
            //DB db = DBMaker.newFileDB(new File(backupDir + "/" + dbName)).make();
            BTreeMap<K,V> map = db.getTreeMap(dbName);
            map.size();
            return new MapDb<K,V>(db, map);
        } catch (Exception e) {
            LOG.error("Failed to create/load MapDb database", e);
            new File(backupDir + "/" + dbName).delete();
        }
        return null;
    }

}
