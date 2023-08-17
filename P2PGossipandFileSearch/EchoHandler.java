import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class EchoHandler extends Thread {

    public String File_Path = "./content/text.txt";
    Socket clientSocket;
    long[] fileRange;
    //time fields
    ZoneId zoneId = ZoneId.of("GMT");
    ZonedDateTime zonedDateTime = LocalDateTime.now().atZone(zoneId);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
    String formattedDate = zonedDateTime.format(formatter);
    private int rate;

    private ConcurrentHashMap<String, Integer> totalDistance;
    private ConcurrentHashMap<UUID, ArrayList<AbstractMap.SimpleEntry<Node, UUID>>> map;
    private Node node;

    private SharedObject so;
    private HashSet<String> killed;
    private ArrayList<UUID> peerlist;
    private HashMap<String, ArrayList<UUID>> peermap;

    EchoHandler(Socket clientSocket,
                Node node,
                ConcurrentHashMap<UUID, ArrayList<AbstractMap.SimpleEntry<Node, UUID>>> map,
                ConcurrentHashMap<String, Integer> totalDistance,
                SharedObject so,
                HashSet<String> killed,
                ArrayList<UUID> peerlist) {
        this.clientSocket = clientSocket;
        this.map = map;
        this.totalDistance = totalDistance;
        this.node = node;
        this.so = so;
        this.killed = killed;
        this.peerlist = peerlist;
        this.peermap = new HashMap<>();
    }

    @Override
    public void run(){
        try {
//            System.out.println("Connection successful");
//            System.out.println("Waiting for input.....");

            DataOutputStream writer = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            //Handling client request
            String inputLine = "";
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
                if (inputLine.startsWith("GET")) {
                    requestProcessing(inputLine, writer);
                    break;
                }
            }
            writer.close();
            in.close();
//            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (Exception e) {
                // 500 internal server error
                try {
                    DataOutputStream writer = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
                    writer.writeBytes(response("500 Internal Server Error"));
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }

    //Process the request line. Distinguish GET line and Range: line.
    public void requestProcessing(String inputLine, DataOutputStream writer) throws IOException {
        if (!inputLine.startsWith("GET")) {
            writer.writeBytes(response("404 Not Found"));
            return;
        }
        String uri = inputLine.split(" ")[1];
        if (uri.startsWith("/peer/search")){
            content_search(uri, writer);
        }
        else if (uri.startsWith("/peer/uuid")) {
            UUID_Process(writer);
        } else if (uri.startsWith("/peer/neighbors")) {
            neighbors_Process(writer);
        } else if (uri.startsWith("/peer/addneighbor")) {
            addProcess(uri, writer);
        } else if (uri.equals("/peer/map")) {
//            Map_Process(writer);
        } else if (uri.equals("/peer/rank")) {
            Rank_Process(writer);
        }else if (uri.equals("/peer/kill")) {
            KILL_Process(writer);
        }else {
            writer.writeBytes(response("404 Not Found"));
        }
    }

    private void content_search(String uri, DataOutputStream writer) throws IOException {
        String[] split = uri.split("/");
        String file = ".\\content\\" + split[split.length - 1];

//        so.setFile(file);

        search(map, file);


        writer.writeBytes(response("200 OK", "Success"));
//        writer.writeBytes(peerlist.toString());//换成peermap response.
        writer.writeBytes(peermap.toString());

    }
    public void search(ConcurrentHashMap<UUID, ArrayList<AbstractMap.SimpleEntry<Node, UUID>>> neighborMap,
                        String target){
        peerlist = new ArrayList<>();

        for (UUID id : neighborMap.keySet()){
            ArrayList<AbstractMap.SimpleEntry<Node, UUID>> neighbors = neighborMap.get(id);
            for (AbstractMap.SimpleEntry<Node, UUID> member : neighbors){
                if (id.equals(member.getValue())){//读id的node
                    Node idNode = member.getKey();
                    String[] files = idNode.getContent_dir().split(",");//id的内容

                    for (String file : files){
//                        if (file.equals(target))//换成partial match
                        if (file.contains(target))
//                            peerlist.add(id);//换成peermap.computeIfAbsent
                            peermap.computeIfAbsent(file, val -> new ArrayList<UUID>()).add(id);
                    }

                }
            }
        }
    }

    private void UUID_Process(DataOutputStream writer) throws IOException {
        UUID uuid = node.getUuid();
        writer.writeBytes(response("200 OK", "Success"));
        JsonObject object = new JsonObject();
        object.addProperty("uuid", uuid.toString());

        writer.writeBytes("<html><head><title>UUID</title></head>" +
                "<body><h1>" +
                "the uuid of the current node is" + "\r\n" +
//                "uuid: " + uuid +
                object +
                "</h1></body></html>");

    }

    private void neighbors_Process(DataOutputStream writer) throws IOException {
        ArrayList<AbstractMap.SimpleEntry<Node, UUID>> neighbors = map.get(node.getName());

        JsonArray array = new JsonArray();
        for(AbstractMap.SimpleEntry<Node, UUID> neighbor : neighbors){
            JsonObject object = new JsonObject();
            Node node = neighbor.getKey();
//            int weight = neighbor.getValue();
            int weight = 0;
            object.addProperty("uuid", node.getUuid().toString());
            object.addProperty("name", node.getName());
            object.addProperty("host", node.getHost());
            object.addProperty("frontend", node.getFrontend());
            object.addProperty("backend", node.getBackend());
            object.addProperty("metric", weight);

            array.add(object);
        }


        writer.writeBytes(response("200 OK", "Success"));
        writer.writeBytes("<html><head><title>Neighbors</title></head>" +
                "<body><h1>" +
                "a list of objects representing all active neighbors" +
                array +
                "</h1></body></html>");

    }

//    private void Map_Process(DataOutputStream writer) throws IOException {
//        writer.writeBytes(response("200 OK", "Success"));
//        JsonObject OBJ = new JsonObject();
//        for(String node : map.keySet()){
//            JsonObject obj = new JsonObject();
//            ArrayList<AbstractMap.SimpleEntry<Node, UUID>> list = map.get(node);
//            for (AbstractMap.SimpleEntry<Node, UUID> neighbor : list){
//                Node n = neighbor.getKey();
////                Integer weight = neighbor.getValue();
//                int weight = 0;
//                obj.addProperty(n.getName(), weight);
//            }
//            OBJ.addProperty(node , obj.toString());
//        }
//
//        writer.writeBytes("<html><head><title>Map</title></head>" +
//                "<body><h1>" +
//                "a list of objects representing all active neighbors" +
////                map +
//                OBJ +
//                "</h1></body></html>");
//
//    }



    private void addProcess(String uri, DataOutputStream writer) throws IOException {
        Map<String, String> paramMap = getRequestParam(uri);
        String data = paramMap.get("path") + " " + paramMap.get("host") + " " + paramMap.get("port") + " " + paramMap.get("rate");

        writer.writeBytes(response("200 OK", "SUCCESS"));
    }

    private void Rank_Process(DataOutputStream writer) throws IOException {
        writer.writeBytes(response("200 OK", "Success"));

        JsonArray array = new JsonArray();
        for(String node : totalDistance.keySet()){
            JsonObject object = new JsonObject();
            int weight = totalDistance.get(node);
            object.addProperty(node, weight);
            array.add(object.toString());
        }

        writer.writeBytes("<html><head><title>Map</title></head>" +
                "<body><h1>" +
                "a list of objects representing all active neighbors" +
//                totalDistance +
                array +
                "</h1></body></html>");

    }

    private void KILL_Process(DataOutputStream writer) throws IOException {
        // Kill
        so.setFlag(true);
        System.out.printf(node.getName() + " is killed");
        killed.add(node.getName());
        System.out.println(killed);
        //record this killed node into Killed HashSet.
        writer.writeBytes(response("200 OK", node.getName() + " killed"));
//        System.exit(1);


    }

    public String extension(String path) {
        String extension = "";
        for (int i = path.length() - 1; i >= 0; i--) {
            if (path.charAt(i) == '.') {
                extension = path.substring(i);
                break;
            }
        }
        System.out.println(extension);
        return extension;
    }

    public String response(String status_code) {
        return response(status_code, get_media_Type(extension(File_Path)), null);
    }

    public String response(String status_code, String data) {
        return response(status_code, "text/html", data);
    }

    //extension is replaced?
    public String response(String status_code, String contentType, String data) {
        String CRLF = "\r\n";
        StringBuilder response1 = new StringBuilder("HTTP/1.1 " + status_code + CRLF);
        if (this.fileRange != null && !status_code.equals("404 Not Found")) {
            response1.append("Accept-Ranges: Bytes").append(CRLF);
            response1.append("Content-Length: ").append(this.fileRange[1] - this.fileRange[0] + 1).append(CRLF);
            response1.append("Content-Range: ").append(this.fileRange[0]).append('-').append(this.fileRange[1])
                    .append('/').append(new File(File_Path).length()).append(CRLF);
        }

        response1.append("Content-Type: ").append(contentType).append(CRLF);
        response1.append("Connection: ").append("keep-alive").append(CRLF);
        response1.append("Date: ").append(formattedDate).append(CRLF);
        response1.append(CRLF);


        if (data != null) {
            response1.append(data);
        }
        //System.out.println(response1.toString());
        return response1.toString();
    }

    public static String get_media_Type(String extension) {
        Map<String, String> media_Types = new HashMap<>();
        media_Types.put(".txt", "text/plain");
        media_Types.put(".css", "text/css");
        media_Types.put(".html", "text/html");
        media_Types.put(".htm", "text/html");
        media_Types.put(".gif", "image/gif");
        media_Types.put(".jpg", "image/jpeg");
        media_Types.put(".jpeg", "image/jpeg");
        media_Types.put(".png", "image/png");
        media_Types.put(".js", "application/javascript");
        media_Types.put(".webm", "video/webm");
        media_Types.put(".mp4", "video/webm");
        media_Types.put(".ogg", "video/webm");
        String My_Type = media_Types.get(extension);
        if (My_Type != null) {
            return My_Type;
        } else {
            return "application/octet-stream";
        }

    }

    // For 206 partial content transfer.
    public void serveFile(DataOutputStream writer, File file, long start, long end) {
        try {
            FileInputStream fis = new FileInputStream(file);
            fis.skip(start);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                if (start + bytesRead > end) {
                    bytesRead = (int) (end - start + 1);
                }
                writer.write(buffer, 0, bytesRead);
                start += bytesRead;
                if (start > end) {
                    break;
                }
            }
            fis.close();
        } catch (IOException e) {
            System.out.println("IOException occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Get a map of params: path, ip, port, rate. Store in one map. Used in addProcess
    private Map<String, String> getRequestParam(String uri) {
        String params = uri.split("\\?")[1];
//        System.out.println(uri);
        Map<String, String> map = new HashMap<>();
        for (String key_value : params.split("&")) {
            String key = key_value.split("=")[0];
            String value = key_value.split("=")[1];
            map.put(key, value);
        }
        System.out.println(map);
        return map;

    }

}






