package track.servlet.standalone;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import track.servlet.standalone.console.HttpConsole;

/**
 *
 */
public class Client {

    static final Logger log = LoggerFactory.getLogger(Client.class);

    public static final String ENV_SEED_ADDR = "ENV_SEED_ADDR";
    public static final String ENV_MULTICAST_ADDR = "ENV_MULTICAST_ADDR";
    public static final String ENV_MULTICAST_PORT = "ENV_MULTICAST_PORT";
    public static final String ENV_CONSOLE_PORT = "ENV_CONSOLE_PORT";

    private final InetAddress group;
    private final int port;
    private final boolean isSeed;

    public static void main(String[] args) throws Exception {
        final String addrStr = System.getenv(ENV_MULTICAST_ADDR);
        final String portStr = System.getenv(ENV_MULTICAST_PORT);
        if (addrStr == null || portStr == null) {
            throw new IllegalArgumentException(String.format("Invalid args: %s, %s\n", addrStr, portStr));
        }

        final InetAddress group = InetAddress.getByName(addrStr);
        final int port = Integer.parseInt(portStr);

        Client client = new Client(group, port);
        client.startClient();

    }

    public Client(InetAddress group, int port) {
        this.group = group;
        this.port = port;
        this.isSeed = isSeed(System.getenv(ENV_SEED_ADDR));
    }

    public void startClient() throws Exception {
        MulticastReceiver receiver = new MulticastReceiver(group, port);
        receiver.start();

        if (isSeed) {
            log.info("[SEED]");
            MulticastPublisher publisher = new MulticastPublisher(group, port);
            HttpConsole console = new HttpConsole();
            console.addListener(m -> {
                publisher.multicast(m.text);
            });
            try {
                console.startConsole();
            } catch (Exception e) {
                log.error("Failed to start console", e);
                e.printStackTrace();
            }
        }
    }

    private boolean isSeed(String seed) {
        try {
            InetAddress seedAddress = InetAddress.getByName(seed);
            log.info("Checking seed: " + seed + ", address: " + seedAddress);
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress.equals(seedAddress)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } catch (Exception e) {
            log.error("Failed to check seed", e);
            e.printStackTrace();
            return false;
        }
    }
}
