import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UdpServer extends Thread {
    private DatagramSocket dsock = null;
    private boolean flag;

    private static int rate = 0;
//    private String node;
    private UUID node;
    private ConcurrentHashMap<String, Integer> totalDistance;
    private ConcurrentHashMap<UUID, ArrayList<SimpleEntry<Node, UUID>>> map;
    public static void setRate(int rate) {
        UdpServer.rate = rate;
    }
    private static final int BUFFER_SIZE = 32 * 1024;
    private SharedObject so;
    ConcurrentHashMap<String, Integer> timerMap;
    private HashSet<String> killed;
    private ArrayList<UUID> peerlist;
    private int interval;

//    private String targetFile;

    public UdpServer(int udpPort,
                     ConcurrentHashMap<UUID, ArrayList<SimpleEntry<Node, UUID>>> map,
                     ConcurrentHashMap<String, Integer> totalDistance,
                     UUID node,
                     SharedObject so,
                     HashSet<String> killed,
                     ArrayList<UUID> peerlist) throws IOException {
        dsock = new DatagramSocket(udpPort);
        this.map = map;
        this.node = node;
        this.totalDistance = totalDistance;
        flag = false;
        this.so = so;
        //Sets timermap
        timerMap = new ConcurrentHashMap<>();
        ArrayList<SimpleEntry<Node, UUID>> neighbors = map.get(node);
//        for (SimpleEntry<Node, UUID> neighbor : neighbors){
//            Node neighborNode = neighbor.getKey();
//            timerMap.put(neighborNode.getName(), 0);
//        }
        this.killed = killed;
        this.peerlist = peerlist;//Not needed really
    }


    @Override
    public void run()  {

        Timer timer = new Timer();
        //Finding self node from map using node uuid.
        Node selfNode = new Node();
        ArrayList<SimpleEntry<Node, UUID>> simpleEntries = map.get(node);
        for (SimpleEntry<Node, UUID> entry : simpleEntries){
            if (entry.getValue() == node){
                selfNode = entry.getKey();
            }
        }

        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (so.getFlag()){
                    timer.cancel();//
                }
                try {
                    send();
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                System.out.println(node + "'s neighbors:");

//                for (String node1 : timerMap.keySet()){
//                    timerMap.put(node1, timerMap.get(node1) + 10);
////                    System.out.println(node + node1 + ": " + timerMap.get(node1) + " seconds");
//                    if (timerMap.get(node1) > 30){
//                        killed.add(node1);
//                        deleteNeighbor(node1);
//                        System.out.println("deleteNeighbor " + node1);
//                        timerMap.remove(node1);
//                    }
//                }
//                System.out.println();
            }
        },0, selfNode.getInterval());//          0.2 second

        byte[] arr1 = new byte[BUFFER_SIZE];
        DatagramPacket dpack = new DatagramPacket(arr1, arr1.length);

        while(!so.getFlag()) {

            try {
                dsock.receive(dpack);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Receiving packet and updating timer.

//            int neighborPort = dpack.getPort();
//            String neighborName = "";
//            System.out.println(neighborPort);
            // Parsing incoming neighbor packet.
//            for (UUID node : map.keySet()){
//                ArrayList<SimpleEntry<Node, UUID>> neighbors = map.get(node);//为什么会空？？？？？？
//                for (SimpleEntry<Node, UUID> neighbor : neighbors){
//                    Node node1 = neighbor.getKey();
//                    if (node1.getBackend() == neighborPort){
//                        neighborName = node1.getName();
//                        timerMap.put(neighborName, 0);//Resetting neighborNode timer to 0 second.
//
////                        System.out.println("resetting");
////                        System.out.println(timerMap);
//                    }
//                }
//            }


            //Parsing neighbor packet map
            byte[] arr2 = dpack.getData();
            int packSize = dpack.getLength();

            ByteArrayInputStream bis = new ByteArrayInputStream(arr2);
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(bis);
                ConcurrentHashMap<UUID, ArrayList<SimpleEntry<Node, UUID>>> neighborGraph =
                        (ConcurrentHashMap<UUID, ArrayList<SimpleEntry<Node, UUID>>>) ois.readObject();

                updateMap(neighborGraph);


            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void send() throws IOException {//send map to neighbor (getting neighbor by retrieving node's neighbors and iterate to get respective backend ports)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(map);
        oos.flush();
        byte[] data = bos.toByteArray();

        for(SimpleEntry<Node, UUID> edge : map.get(node)) {//when to fill in? at the beginning.
            Node neighbor = edge.getKey();
//            int weight = edge.getValue();
            UUID uuid = edge.getValue();
            int port = neighbor.getBackend();
            InetAddress add = InetAddress.getByName("localhost");

            DatagramPacket dpack = new DatagramPacket(data, data.length, add, port);
            dsock.send(dpack);
        }
    }

    void printGraph(ConcurrentHashMap<UUID, ArrayList<SimpleEntry<Node, UUID>>> map){
        for (UUID node : map.keySet()){
            ArrayList<SimpleEntry<Node, UUID>> edges = map.get(node);
            System.out.print(node + "'s neighbors: ");
            for (SimpleEntry<Node, UUID> edge : edges){
                Node neighbor = edge.getKey();
//                int distance = edge.getValue();
                UUID uuid = edge.getValue();
                System.out.print(uuid + ", ");
            }
            System.out.println();
        }
    }

    void printDistance(){

        System.out.println(this.node + "'s distances to other nodes: ");

        for (String node : totalDistance.keySet()){
            System.out.println(node + ": " + totalDistance.get(node));
        }
    }


    public void updateMap(ConcurrentHashMap<UUID, ArrayList<SimpleEntry<Node, UUID>>> neighborMap){
        for (UUID node : neighborMap.keySet()){
//            if (!map.containsKey(node) && !killed.contains(node)){// and not killed.
            if (!map.containsKey(node)){
                map.put(node, neighborMap.get(node));
                flag = true; //也不需要了
            }
        }

        if (flag){
//            System.out.println((node + "'s map:"));

            flag = false;

            System.out.println("map of node " + node + " modified");

            printGraph(map);

//            new Dijkstra().dijkstra(totalDistance, map, node);

//            printDistance();
//            System.out.println(totalDistance);
        }

    }

    private void deleteNeighbor(String deleteNeighbor){
        System.out.println("Deleting");

        //Delete deleteNeighbor's entry
        System.out.println("Deleting " + deleteNeighbor + " " + map.get(deleteNeighbor) +
                "from " + node + " 's map");
        map.remove(deleteNeighbor);

        //Delete deleteNeighbor's entry in other nodes' neighborList.
        for (UUID n : map.keySet()){
            ArrayList<SimpleEntry<Node, UUID>> links = map.get(n);
            for (int i = 0; i < links.size(); i++){
                SimpleEntry<Node, UUID> link = links.get(i);
                Node node = link.getKey();
                if (node.getName().equals(deleteNeighbor)){
                    System.out.println("Deleting " + deleteNeighbor + " in node " + n);
                    links.remove(i);//Read-write error??????????
                }
            }
            map.put(n, links);
        }

//        System.out.println(map);
//        printGraph(map);
        //Deleting totalDistance to deleteNeighbor.
        totalDistance.remove(deleteNeighbor);

//        printGraph(map);
//        new Dijkstra().dijkstra(totalDistance, map, node);

    }

    public void search(ConcurrentHashMap<UUID, ArrayList<SimpleEntry<Node, UUID>>> neighborMap){
        for (UUID id : neighborMap.keySet()){
            ArrayList<SimpleEntry<Node, UUID>> neighbors = neighborMap.get(id);
            for (SimpleEntry<Node, UUID> member : neighbors){
                if (id.equals(member.getValue())){
                    Node idNode = member.getKey();
                    String[] files = idNode.getContent_dir().split(",");

                    for (String file : files){
                        if (file.equals(so.getFile()))
                            peerlist.add(id);
                    }

                }
            }
        }
    }


}