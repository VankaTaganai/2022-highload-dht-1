package ok.dht.test.kovalenko;

import ok.dht.ServiceConfig;
import one.nio.http.HttpServer;
import one.nio.http.HttpServerConfig;
import one.nio.http.HttpSession;
import one.nio.http.Request;
import one.nio.http.Response;
import one.nio.net.Session;
import one.nio.server.SelectorThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class MyServer extends HttpServer {

    private static final Logger LOG = LoggerFactory.getLogger(MyServer.class);

    public MyServer(HttpServerConfig config, Object... routers) throws IOException {
        super(config, routers);
    }

    public static void main(String[] args) {
        try {
            int port = 19234;
            String url = "http://localhost:" + port;
            ServiceConfig cfg = new ServiceConfig(
                    port,
                    url,
                    Collections.singletonList(url),
                    Path.of("/home/pavel/IntelliJIdeaProjects/tables/data_bigtables/")
            );
            MyService service = new MyService(cfg);
            service.start().get(1, TimeUnit.SECONDS);
            LOG.debug("Socket is ready: {}", url);
        } catch (Exception e) {
            LOG.error("Unexpected error", e);
        }
    }

    @Override
    public void handleDefault(Request request, HttpSession session) throws IOException {
        Response response = new Response(Response.BAD_REQUEST, Response.EMPTY);
        session.sendResponse(response);
    }

    @Override
    public void handleRequest(Request request, HttpSession session) throws IOException {
        try {
            super.handleRequest(request, session);
        } catch (Exception e) {
            LOG.error("Unexpected error", e);
            session.sendError(Response.INTERNAL_ERROR, e.getMessage());
        }
    }

    @Override
    public synchronized void stop() {
        super.stop();
        for (SelectorThread selectorThread : selectors) {
            for (Session session : selectorThread.selector) {
                session.close();
            }
        }
    }
}
