import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;


public class VodServer {
    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = null;
        udpServer udpServer = null;

        int tcpPort = 8345;
        int udpPort = 8346;

        DatagramSocket datagramSocket = new DatagramSocket(udpPort);

        SharedObject sharedObject = new SharedObject();

        // UDP sender thread
        try {
            udpServer = new udpServer(datagramSocket, sharedObject);
            udpServer.start();
        } catch (IOException e) {
            System.err.println("UDP could not listen on port: " + udpPort + ".");
            System.exit(1);
        }

        // TCP thread
        try {
            serverSocket = new ServerSocket(tcpPort);
        } catch (IOException e) {
            System.err.println("TCP could not listen on port: " + tcpPort + ".");
            System.exit(1);
        }

        try {
            while (true) {
                System.out.println("Waiting for tcp connection.....");
                Socket clientSocket = serverSocket.accept();
                EchoHandler handler = new EchoHandler(clientSocket, sharedObject);
                handler.start();
            }

        } catch (IOException e) {
            System.err.println("Accept failed.");
            System.exit(1);
        }

        udpServer.close();
        serverSocket.close();
    }
} 