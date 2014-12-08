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

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;

import javax.inject.Singleton;

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
import dk.dma.ais.track.store.MapDbTargetStore;
import dk.dma.ais.track.store.TargetStore;
import dk.dma.commons.app.AbstractDaemon;

public class AisTrackDaemon extends AbstractDaemon {

    static final Logger LOG = LoggerFactory.getLogger(AisTrackDaemon.class);

    @Parameter(names = "-port", description = "The port to run AisTrach HTTP server at")
    int port = 8080;

    @Parameter(names = "-backup", description = "The backup directory")
    File backup;

    @Parameter(names = "-bus", description = "AisBus configuration file")
    String aisBusConfFile = "aisbus.xml";

    private WebServer webServer;
    private AisBus aisBus;
    private AisTrackHandler handler;
    private TargetStore<VesselTarget> targetStore;

    @Override
    protected void runDaemon(Injector injector) throws Exception {
        LOG.info("Starting AisTrackDaemon");

        // Target store
        // TODO base on settings
        targetStore = new MapDbTargetStore<VesselTarget>();
        targetStore.init();

        // Make web server
        webServer = new WebServer(port);

        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(VesselResource.class);
                bind(new TypeLiteral<TargetStore<VesselTarget>>() {}).toInstance(targetStore);
                bind(AisTrackHandler.class).in(Singleton.class);                
            }
        };
        
        injector = Guice.createInjector(module);
        
        handler = injector.getInstance(AisTrackHandler.class);

        // Load AisBus configuration
        AisBusConfiguration aisBusConf = AisBusConfiguration.load(aisBusConfFile);
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
        targetStore.close();
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
