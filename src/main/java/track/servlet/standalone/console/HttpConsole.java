package track.servlet.standalone.console;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;

/**
 *
 */
public class HttpConsole {
    static final Logger log = LoggerFactory.getLogger(HttpConsole.class);
    private Set<HttpConsoleListener> listeners = new HashSet<>();
    private int port = 8095;

    public static void main(String[] args) throws Exception {
        new HttpConsole().startConsole();
    }

    public void addListener(HttpConsoleListener listener) {
        listeners.add(listener);
    }

    public Map<String, String> queryToMap(String query){
        Map<String, String> result = new HashMap<String, String>();
        for (String param : query.split("&")) {
            String pair[] = param.split("=");
            if (pair.length>1) {
                result.put(pair[0], pair[1]);
            }else{
                result.put(pair[0], "");
            }
        }
        return result;
    }

    public void startConsole() throws Exception {
        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress(port), 0);
        server.createContext("/", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = queryToMap(query);
            String text = params.get("text");


            log.info(String.format("c: %s\n", exchange.getRemoteAddress().getAddress()));
            log.info("input: " + text);
            HttpMessage msg = new HttpMessage();
            msg.text = text;
            listeners.forEach(it -> it.onMessage(msg));

            StringBuilder builder = new StringBuilder();
            builder.append("<h1>URI: ").append(exchange.getRequestURI()).append("</h1>");
            Headers headers = exchange.getRequestHeaders();
            for (String header : headers.keySet()) {
                builder.append("<p>").append(header).append("=").append(headers.getFirst(header)).append("</p>\n");
            }

            byte[] bytes = builder.toString().getBytes();
            exchange.sendResponseHeaders(HttpStatus.OK_200, bytes.length);

            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();

        });
        server.start();
    }
}
