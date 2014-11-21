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
import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public void start() throws Exception {
        ((ServerConnector) server.getConnectors()[0]).setReuseAddress(true);

        context.setContextPath("/");

        ServletHolder sho = new ServletHolder(new ServletContainer());
        sho.setClassName("org.glassfish.jersey.servlet.ServletContainer");
        sho.setInitParameter("jersey.config.server.provider.packages", "dk.dma.ais.track.resource");
        // This flag is set to disable internal buffering in jersey.
        // this is mainly done to avoid delays from when people request something. To the first output is delivered
        sho.setInitParameter(CommonProperties.OUTBOUND_CONTENT_LENGTH_BUFFER, "-1");

        context.addServlet(sho, "/*");

        // HandlerWrapper hw = new HandlerWrapper() {
        //
        // /** {@inheritDoc} */
        // @Override
        // public void handle(String target, Request baseRequest, HttpServletRequest request,
        // HttpServletResponse response) throws IOException, ServletException {
        // long start = System.nanoTime();
        // String queryString = request.getQueryString() == null ? "" : "?" + request.getQueryString();
        // LOG.info("Received connection from " + request.getRemoteHost() + " (" + request.getRemoteAddr() + ":"
        // + request.getRemotePort() + ") request = " + request.getRequestURI() + queryString);
        // super.handle(target, baseRequest, request, response);
        // LOG.info("Connection closed from " + request.getRemoteHost() + " (" + request.getRemoteAddr() + ":"
        // + request.getRemotePort() + ") request = " + request.getRequestURI() + queryString
        // + ", Duration = " + (System.nanoTime() - start) / 1000000 + " ms");
        // }
        // };
        // hw.setHandler(context);
        // server.setHandler(hw);
        server.start();
    }
    
}
