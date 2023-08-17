import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GossipProtocol {
    private List<Node> nodes;

    public GossipProtocol(int numNodes) {
        nodes = new ArrayList<>(numNodes);
        for (int i = 0; i < numNodes; i++) {
            nodes.add(new Node(i));
        }
    }

    public void run() {
        Random random = new Random();
        for (Node node : nodes) {
            Node neighbor = nodes.get(random.nextInt(nodes.size()));
            node.gossip(neighbor);
        }
    }

    private static class Node {
        private int id;
        private List<Node> neighbors;
        private String information;

        public Node(int id) {
            this.id = id;
            neighbors = new ArrayList<>();
            information = "Hello from Node " + id;
        }

        public void addNeighbor(Node neighbor) {
            neighbors.add(neighbor);
        }

        public void gossip(Node neighbor) {
            neighbor.receive(information);
        }

        public void receive(String information) {
            // Do something with the received information
            System.out.println("Node " + id + " received: " + information);
            // Gossip to a random neighbor
            Random random = new Random();
            Node neighbor = neighbors.get(random.nextInt(neighbors.size()));
            neighbor.gossip(this);
        }
    }

    public static void main(String[] args) {
        GossipProtocol protocol = new GossipProtocol(5);
        protocol.run();
    }
}
