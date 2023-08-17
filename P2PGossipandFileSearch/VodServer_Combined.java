import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class VodServer_Combined {

    public static void main(String[] args) throws IOException {

//        File file = new ArrayList<>();
//        file = config.getFilesInDirectory("./Nodes/Nodes");

        String configFile = "Nodes/Nodes/node3.config";
        File file = new File(configFile);
        config.getFilesInDirectory("./content", configFile);
        ConcurrentHashMap<UUID, ArrayList<AbstractMap.SimpleEntry<Node, UUID>>> graph = new ConcurrentHashMap<>();
        Node node = config.nodeConfig(graph, file);

//        for (File file : files) {
        UdpServer udpServer = null;
        TcpServer tcpServer = null;

        int udpPort = node.getBackend();
        int tcpPort = node.getFrontend();
        ArrayList<UUID> peerlist = new ArrayList<>();
//-------------------------------out-dated variables for Proj 3----------------------------
            ConcurrentHashMap<String, Integer> totalDistance = new ConcurrentHashMap<>();
            SharedObject sharedObject = new SharedObject();
            HashSet<String> killed = new HashSet<>();
//--------------------------------------------------------------------------------------------


        //UDP thread
            try {
                udpServer = new UdpServer(udpPort, graph, totalDistance, node.getUuid(), sharedObject, killed, peerlist); //(udpPort, so)
                udpServer.start();
            }
            catch (IOException e) {
                System.err.println("UDP could not listen on port: " + udpPort + ".");
                System.exit(1);
            }


            // TCP thread
            tcpServer = new TcpServer(tcpPort, totalDistance, graph, node, sharedObject, killed, peerlist);
            tcpServer.start();


//        }

    }
} 