package track.servlet.cassandra;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

/**
 *
 */
@Ignore
public class CassandraConnectionTest {

    private Session session;

    @Before
    public void connect() {
        CassandraConnection client = new CassandraConnection();
        client.connect("127.0.0.1", 9142);
        this.session = client.getSession();
    }

    @Test
    public void test1() {
        ResultSet result = session.execute("SELECT * FROM system_schema.keyspaces;");

        List<String> matchedKeyspaces = result.all()
                .stream()
                .map(r -> r.getString(0))
                .collect(Collectors.toList());

        System.out.println(matchedKeyspaces);

        session.execute("use dev;");
        ResultSet rows = session.execute("select * from employees;");
        result.all().stream()
                .forEach(System.out::println);
    }

}