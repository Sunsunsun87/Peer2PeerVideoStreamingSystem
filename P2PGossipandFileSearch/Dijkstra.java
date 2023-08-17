import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Dijkstra {
    public void dijkstra
            (ConcurrentHashMap<String, Integer> totalDistance0,
             ConcurrentHashMap<String, ArrayList<SimpleEntry<Node, UUID>>> map,
             String source){
        //Use priority queue for traversal sequence. Gradually go through and update over the whole graph.
        //Not the same as bfs or dfs: does not use FIFO queue or stack to traverse level-wise or depth-wise,
        //    but priority-wise.
        //pq is for sequence, totalTime array is for comparison and update metric.

        PriorityQueue<SimpleEntry<String, Integer>> pq =
        new PriorityQueue<>(Comparator.comparingInt(SimpleEntry::getValue));

        HashMap<String, Boolean> visited = new HashMap<>();
        ConcurrentHashMap<String, Integer> totalDistance = new ConcurrentHashMap<>();

        //Initialize utility hashmaps.
        for (String s : map.keySet()){
            for (SimpleEntry<Node, UUID> edges : map.get(s)) {
                String neighbor = edges.getKey().getName();
                visited.put(neighbor, false);
                totalDistance.put(neighbor, Integer.MAX_VALUE);
            }
        }

        totalDistance.put(source, 0);

        pq.add(new SimpleEntry<>(source, 0));
        while (!pq.isEmpty()){
            SimpleEntry<String, Integer> node = pq.poll(); //should be string to int. Totally self-created, and utilized based on String name and map string key. Other metrics are later on acquired from string key's node value using node's get methods (get name of neighbor)

            String currNode = node.getKey();
            int currDis = node.getValue();
//            System.out.println(currNode);
//            System.out.println(visited);
            //避免重复 visited.get(currNode)
            if(visited.get(currNode))
                continue;

            //越过陈旧结点 totalDis.get(currNode)
            if (currDis > totalDistance.get(currNode))
                continue;

            if (!map.containsKey(currNode))//越过无邻居结点
                continue;

            visited.put(currNode, true);

            //Broadcast to all neighbors (edges). Refer to totalTime array for time values.
            // This is where Dijkstra is more complex than dfs or bfs. It updates using metric(totalDis) comparison.

            for (SimpleEntry<Node, UUID> edge : map.get(currNode)){
                String neighbor = edge.getKey().getName();
//                int weight = edge.getValue();
                int weight = 0;
                if (totalDistance.get(neighbor) > currDis + weight){ //totalDis.get(neighbor)
                    totalDistance.put(neighbor, currDis + weight); //Update the neighbor's time/distance. Quite 'aggressive'.
                    pq.offer(new SimpleEntry<>(neighbor, totalDistance.get(neighbor)));

                }
            }

        }

        for (String node : totalDistance.keySet()){
            totalDistance0.put(node, totalDistance.get(node));
        }
    }

}
