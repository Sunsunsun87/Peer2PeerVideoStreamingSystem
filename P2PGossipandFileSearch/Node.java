import java.io.Serializable;
import java.util.UUID;

public class Node implements Serializable {
    private UUID uuid;
    private String name;
    private String host;
    private int frontend;
    private int backend;
    private String content_dir;
    private int peer_count;
    private int metric;
    private int interval;

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public Node() {
    }

    public Node(String name, int port){
        this.name = name;
        backend = port;
    }


    public String getContent_dir() {
        return content_dir;
    }

    public void setContent_dir(String content_dir) {
        this.content_dir = content_dir;
    }

    public int getPeer_count() {
        return peer_count;
    }

    public void setPeer_count(int peer_count) {
        this.peer_count = peer_count;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public int getFrontend() {
        return frontend;
    }

    public int getBackend() {
        return backend;
    }

    public int getMetric() {
        return metric;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setFrontend(int frontend) {
        this.frontend = frontend;
    }

    public void setBackend(int backend) {
        this.backend = backend;
    }

    public void setMetric(int metric) {
        this.metric = metric;
    }

    public Node(UUID uuid, String name, String host, int frontend, int backend, String content_dir, int peer_count, int metric) {
        this.uuid = uuid;
        this.name = name;
        this.host = host;
        this.frontend = frontend;
        this.backend = backend;
        this.content_dir = content_dir;
        this.peer_count = peer_count;
        this.metric = metric;
    }


}
