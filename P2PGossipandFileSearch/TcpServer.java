import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TcpServer extends Thread{
    private int tcpPort;
    private ConcurrentHashMap<String, Integer> totalDistance;
    private ConcurrentHashMap<UUID, ArrayList<AbstractMap.SimpleEntry<Node, UUID>>> map;
    private Node node;
    private SharedObject so;
    private HashSet<String> killed;
    private ArrayList<UUID> peerlist;
    public TcpServer(int tcpPort,
                     ConcurrentHashMap<String, Integer> totalDistance,
                     ConcurrentHashMap<UUID, ArrayList<AbstractMap.SimpleEntry<Node, UUID>>> map,
                     Node node,
                     SharedObject so,
                     HashSet<String> killed,
                     ArrayList<UUID> peerlist){
        this.tcpPort = tcpPort;
        this.totalDistance = totalDistance;
        this.map = map;
        this.node = node;
        this.so = so;
        this.killed = killed;
        this.peerlist = peerlist;
    }

    @Override
    public void run(){
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(tcpPort);
        } catch (IOException e) {
            System.err.println("TCP could not listen on port: " + tcpPort + ".");
            System.exit(1);
        }

        try {
            while (!so.getFlag()) {
                System.out.println("Waiting for tcp connection.....");
                Socket clientSocket = serverSocket.accept();
                EchoHandler echoHandler = new EchoHandler(clientSocket, node, map, totalDistance, so, killed, peerlist);
                echoHandler.start();
            }
            System.out.printf("TCP closed");
        } catch (IOException e) {
            System.err.println("Accept failed.");
            System.exit(1);
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
