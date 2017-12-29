package track.servlet.embedded;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import track.servlet.cassandra.CassandraConnection;

/**
 * Запускаем jetty-сервер из java кода (embedded mode)
 * В качестве обработчика http запросов отдаем ему текущий класс, который extends AbstractHandler
 * <p>
 * Можно запустить класс через main()
 * <p>
 * Browser:
 * localhost:8082
 * localhost:8082?x=100&y=200
 */
public class SimpleHttpServer extends AbstractHandler {

    public static final int PORT = 8080;

    public static final String ENV_CASSANDRA_PORT = "CASSANDRA_PORT";
    public static final String ENV_CASSANDRA_SEEDS = "CASSANDRA_SEEDS";

    private Session session;


    public SimpleHttpServer() {
        CassandraConnection client = new CassandraConnection();
        String node = System.getenv(ENV_CASSANDRA_SEEDS);
        String port = System.getenv(ENV_CASSANDRA_PORT);
        System.out.println("Connecting to " + node + ":" + port);
        client.connect(node, Integer.parseInt(port));
        this.session = client.getSession();
        session.execute("USE dev;");
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request,
                       HttpServletResponse response) throws IOException, ServletException {

        try {
            ResultSet rows = session.execute("SELECT * FROM employees;");
            response.getWriter().println("<h1>ROWS:" + rows.getAvailableWithoutFetching() + "</h1>");

            List<String> collect = rows.all().stream()
                    .map(r -> r.getString(1))
                    .collect(Collectors.toList());
            response.getWriter().println("<h1>" + collect.toString() + "</h1>");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String sysPropName = request.getParameter("sys_prop");
        if (StringUtils.isNotEmpty(sysPropName)) {
            try {
                response.getWriter().println("<h1>r=" + System.getProperty(sysPropName) + "</h1>");
            } catch (Exception e) {
                response.getWriter().println("<h1>error</h1>");
            }
        }

        String envPropName = request.getParameter("env_prop");
        if (StringUtils.isNotEmpty(envPropName)) {
            try {
                response.getWriter().println("<h1>r=" + System.getenv(envPropName) + "</h1>");
            } catch (Exception e) {
                response.getWriter().println("<h1>error</h1>");
            }
        }

        response.setContentType("text/html;charset=utf-8");
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.setStatus(HttpServletResponse.SC_OK);

        baseRequest.setHandled(true);

    }

    public static void main(String[] args) throws Exception {

        // Server из библиотеки jetty
        Server server = new Server(PORT);

        // Обработчик соединения - наш класс
        server.setHandler(new SimpleHttpServer());

        server.start();
        System.out.println("Running on:" + PORT);
        server.join();
    }
}