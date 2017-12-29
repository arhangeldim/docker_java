package track.servlet.standalone;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 *
 */
public class MulticastPublisher {
    private final InetAddress group;
    private final int port;
    private DatagramSocket socket;

    public MulticastPublisher(InetAddress group, int port) throws SocketException {
        this.group = group;
        this.port = port;
        socket = new DatagramSocket();
    }

    public void multicast(final String multicastMessage) {
        try {
            socket = new DatagramSocket();
            byte[] buf = multicastMessage.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, port);
            System.out.println("Publish: " + multicastMessage);
            socket.send(packet);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}