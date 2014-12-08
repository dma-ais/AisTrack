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

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import dk.dma.ais.track.resource.VesselResource;

public class WebServer {

    static final Logger LOG = LoggerFactory.getLogger(WebServer.class);

    final ServletContextHandler context;

    final Server server;

    public WebServer(int port) {
        server = new Server(port);
        this.context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    }

    /**
     * @return the context
     */
    public ServletContextHandler getContext() {
        return context;
    }

    public void join() throws InterruptedException {
        server.join();
    }

    public void stop() throws Exception {
        server.stop();
    }

    public void start(Injector injector) throws Exception {
        final ResourceConfig rc = new ResourceConfig();
        rc.register(injector.getInstance(VesselResource.class));         
        
        ((ServerConnector) server.getConnectors()[0]).setReuseAddress(true);
        context.setContextPath("/");
        ServletHolder sho = new ServletHolder(new ServletContainer(rc));
        context.addServlet(sho, "/*");
        
//        GzipHandler gzipHandler = new GzipHandler();
//        gzipHandler.setHandler(context);
//        server.setHandler(gzipHandler);
        
        server.setHandler(context);
        server.start();
    }

}
