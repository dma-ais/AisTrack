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
package dk.dma.ais.track;

import java.io.FileInputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Properties;

import javax.inject.Singleton;

import org.aeonbits.owner.ConfigCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;

import dk.dma.ais.bus.AisBus;
import dk.dma.ais.bus.consumer.DistributerConsumer;
import dk.dma.ais.configuration.bus.AisBusConfiguration;
import dk.dma.ais.track.model.VesselTarget;
import dk.dma.ais.track.resource.VesselResource;
import dk.dma.ais.track.store.MaxSpeedStore;
import dk.dma.ais.track.store.PastTrackStore;
import dk.dma.ais.track.store.SimpleMaxSpeedStore;
import dk.dma.ais.track.store.TargetStore;
import dk.dma.commons.app.AbstractDaemon;

public class AisTrackDaemon extends AbstractDaemon {

    static final Logger LOG = LoggerFactory.getLogger(AisTrackDaemon.class);

    @Parameter(names = "-port", description = "The port to run AisTrach HTTP server at")
    Integer port;

    @Parameter(names = "-backup", description = "The backup directory")
    String backup;

    @Parameter(names = "-bus", description = "AisBus configuration file")
    String aisbusConfFile;
    
    @Parameter(names = "-conf", description = "Optional configuration file")
    String confFile;

    private WebServer webServer;
    private AisBus aisBus;
    private AisTrackHandler handler;
    private PastTrackStore pastTrackStore;

    @Override
    protected void runDaemon(Injector injector) throws Exception {
        LOG.info("Starting AisTrackDaemon");
        
        // Create configuration
        Properties argProps = new Properties();
        Properties confProps = new Properties();
        if (port != null) {
            argProps.setProperty("port", Integer.toString(port));
        }
        if (backup != null) {
            argProps.setProperty("backup", backup);
        }
        if (aisbusConfFile != null) {
            argProps.setProperty("aisbusConfFile", aisbusConfFile);
        }
        if (confFile != null) {
            confProps.load(new FileInputStream(confFile));
        }        
        AisTrackConfiguration cfg = ConfigCache.getOrCreate(AisTrackConfiguration.class, confProps, argProps);
        
        // Get target store class
        @SuppressWarnings("unchecked")
        Class<TargetStore<VesselTarget>> targetStoreClazz = (Class<TargetStore<VesselTarget>>) cfg.targetStoreClass();
        LOG.info("Using " + targetStoreClazz + " target store");
        
        @SuppressWarnings("unchecked")
        Class<PastTrackStore> trackStoreClazz = (Class<PastTrackStore>) cfg.pastTrackStoreClass();
        LOG.info("Using " + trackStoreClazz + " track store");

        // Make web server
        webServer = new WebServer(cfg.port());

        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(VesselResource.class);
                bind(new TypeLiteral<TargetStore<VesselTarget>>() {}).to(targetStoreClazz).in(Singleton.class);
                bind(PastTrackStore.class).to(trackStoreClazz).in(Singleton.class);
                bind(AisTrackHandler.class).in(Singleton.class);
                bind(AisTrackConfiguration.class).toInstance(cfg);
                bind(MaxSpeedStore.class).to(SimpleMaxSpeedStore.class).in(Singleton.class);
            }
        };
        
        injector = Guice.createInjector(module);
        handler = injector.getInstance(AisTrackHandler.class);
        pastTrackStore = injector.getInstance(PastTrackStore.class);
        
        // Load AisBus configuration
        AisBusConfiguration aisBusConf = AisBusConfiguration.load(cfg.aisbusConfFile());
        aisBus = aisBusConf.getInstance();
        // Create distributor consumer and add to aisBus
        DistributerConsumer distributer = new DistributerConsumer();
        distributer.getConsumers().add(handler);
        distributer.init();
        aisBus.registerConsumer(distributer);
        aisBus.start();
        aisBus.startConsumers();
        aisBus.startProviders();
        
        webServer.start(injector);
        LOG.info("AisTrack started");
        webServer.join();
    }

    @Override
    public void shutdown() {
        LOG.info("Shutting down");
        if (webServer != null) {
            try {
                webServer.stop();
            } catch (Exception e) {
                LOG.info("Failed to stop web server", e);
            }
        }
        if (aisBus != null) {
            aisBus.cancel();
        }
        pastTrackStore.close();
        super.shutdown();
    }

    public static void main(String[] args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                LOG.error("Uncaught exception in thread " + t.getClass().getCanonicalName() + ": " + e.getMessage(), e);
                System.exit(-1);
            }
        });
        new AisTrackDaemon().execute(args);
    }

}
