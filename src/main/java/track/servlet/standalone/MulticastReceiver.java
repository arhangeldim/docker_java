package track.servlet.standalone;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class MulticastReceiver extends Thread {
    static final Logger log = LoggerFactory.getLogger(MulticastReceiver.class);
    private MulticastSocket socket;
    private InetAddress group;
    private int port;

    public MulticastReceiver(InetAddress group, int port) throws IOException {
        this.group = group;
        this.port = port;
        socket = new MulticastSocket(port);
        socket.joinGroup(group);
        log.info("Running multicast receiver on " + group + ":" + port);
    }

    public void run() {
        try {
            final byte[] buf = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                log.info("Received: " + received);
                if ("end".equals(received)) {
                    break;
                }
            }
            socket.leaveGroup(group);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}