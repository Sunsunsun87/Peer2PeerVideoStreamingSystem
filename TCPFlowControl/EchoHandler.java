import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

class EchoHandler extends Thread {
    private static final String DATA_FILE = "data.dat";

    private static final String LOAD_VOD_PATH = "load/";
    private static final Map<String, Map<String, String>> PATH_MAP = new HashMap<>();
    public static final Map<String, Integer> VOD_LOAD_PERCENTAGE_MAP = new LinkedHashMap<>();
    public static final Map<String, Double> VOD_LOAD_RATE_MAP = new LinkedHashMap<>();

    Socket clientSocket;
    //file fields
    long[] fileRange;
    //time fields
    ZoneId zoneId = ZoneId.of("GMT");
    ZonedDateTime zonedDateTime = LocalDateTime.now().atZone(zoneId);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
    String formattedDate = zonedDateTime.format(formatter);
    private int rate;

    private int udpPort;

    private SharedObject so;


    static {
        loadData();
    }

    EchoHandler(Socket clientSocket, SharedObject so) {
        this.clientSocket = clientSocket;
        this.so = so;
    }

    private static void loadData() {
        List<String> lines = FileUtil.readFile(DATA_FILE);
        for (String line : lines) {
            String[] infos = line.split(" ");
            Map<String, String> map = new HashMap<>();
            map.put("path", infos[0]);
            map.put("host", infos[1]);
            map.put("port", infos[2]);
            map.put("rate", infos[3]);
            PATH_MAP.put(infos[0], map);
        }
    }


    public void run() {
        try {
            System.out.println("Connection successful");
            System.out.println("Waiting for input.....");

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

        if (uri.startsWith("/peer/add?")) {
            addProcess(uri, writer);
        } else if (uri.startsWith("/peer/view/")) {
            viewProcess(uri, writer);
        } else if (uri.startsWith("/peer/config?")) {
            configProcess(uri, writer);
        } else if (uri.equals("/peer/status")) {
            statusProcess(uri, writer);
        } else {
            writer.writeBytes(response("404 Not Found"));
        }
    }


    private void statusProcess(String uri, DataOutputStream writer) throws IOException {
        String message = "";
        List<String> pathList = new ArrayList<>(VOD_LOAD_PERCENTAGE_MAP.keySet());
        Collections.reverse(pathList); //reverse path list
        for (String path : VOD_LOAD_PERCENTAGE_MAP.keySet()) {
            message += String.format("%s: percentage=%d%%, rate=%.1f%n", path, VOD_LOAD_PERCENTAGE_MAP.get(path), VOD_LOAD_RATE_MAP.get(path));
        }
        writer.writeBytes(response("200 OK", message));
    }

    private void configProcess(String uri, DataOutputStream writer) throws IOException {
        Map<String, String> paramMap = getRequestParam(uri);
        String rateStr = paramMap.get("rate");
        try {
            int rateInt = Integer.parseInt(rateStr);
            if (rateInt >= 0) {
                rate = rateInt;
            }
            udpServer.setRate(rate);

        } catch (Exception e) {
            e.printStackTrace();
        }
        writer.writeBytes(response("200 OK", "Success: rate=" + rate));
    }

    private void viewProcess(String uri, DataOutputStream writer) throws IOException {
//        String path = uri.substring(11);//
        String path = Get_path();

//        udpClient udpClient = new udpClient(udpPort);//UDP client creation. Using designated port
//        System.out.println("Client created");
//        udpClient.receive(Get_port(), Get_IP(), path.split("/")[1], Get_rate());

        //udpClient receives file.
        writer.writeBytes(response("200 OK"));

        FileUtil.writeFile(writer, Get_path());

    }

   // public static void receive(int port, String fileName) throws IOException
    private String getLoadFilePath(String path) {
        return "load/" + path;
    }

//    private boolean loadVod(String path, String host, int port, int rate) {
//        String file = this.getLoadFilePath(path);
//        try {
//            byte[] vodByte = udpClient.receive(port, host, path, rate);
//            if (vodByte == null) {
//                return false;
//            } else {
//                FileUtil.writeFile(file, vodByte);
//                return true;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//
//    }


    private void addProcess(String uri, DataOutputStream writer) throws IOException {
        Map<String, String> paramMap = this.getRequestParam(uri);
        String var10000 = paramMap.get("path");
        String data = var10000 + " " + paramMap.get("host") + " " + paramMap.get("port") + " " + paramMap.get("rate");
        PATH_MAP.put(paramMap.get("path"), paramMap);
        FileUtil.writeFile("data.dat", data, true);

        writer.writeBytes(this.response("200 OK", "Success"));
    }


    public String Get_path() {
        List<String> contents = FileUtil.readFile(DATA_FILE);
        String lastRow = contents.get(contents.size() - 1);
        return lastRow.split(" ")[0];
    }

    public String Get_IP() {
        List<String> contents = FileUtil.readFile(DATA_FILE);
        String lastRow = contents.get(contents.size() - 1);
        return lastRow.split(" ")[1];

    }public int Get_port() {
        List<String> contents = FileUtil.readFile(DATA_FILE);
        String lastRow = contents.get(contents.size() - 1);
        return Integer.parseInt(lastRow.split(" ")[2]);
    }
    public int Get_rate() {
        List<String> contents = FileUtil.readFile(DATA_FILE);
        String lastRow = contents.get(contents.size() - 1);
        return Integer.parseInt(lastRow.split(" ")[3]);
    }

    public String extension(String path) {
        System.out.println(PATH_MAP);
        System.out.println(Get_path());
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
        return response(status_code, get_media_Type(extension(Get_path())), null);
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
                    .append('/').append(new File(Get_path()).length()).append(CRLF);
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


    // Get a map of params: path, ip, port, rate. Store in one map. Used in addProcess
    private Map<String, String> getRequestParam(String uri) {
        String params = uri.split("\\?")[1];
        //System.out.println(uri);
        Map<String, String> map = new HashMap<>();
        for (String key_value : params.split("&")) {
            String key = key_value.split("=")[0];
            String value = key_value.split("=")[1];
            map.put(key, value);
        }
        //System.out.println(map);
        return map;

    }

}
















