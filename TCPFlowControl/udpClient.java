import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.sql.Time;
import java.util.*;

public class udpClient extends Thread{
    public static HashMap<Integer, byte[]> map = new HashMap<>();

    private static DatagramSocket dsock = null;
    private static final int BUFFER_SIZE = 32 * 1024;
//    private static String filePath;
    private int rate;
    private SharedObject so;


    public static void main(String args[]) throws Exception {
        InetAddress add = InetAddress.getByName("localhost");

        DatagramSocket dsock = new DatagramSocket();
        String message1 = "This is client calling";
        byte arr[] = message1.getBytes();
        DatagramPacket dpack = new DatagramPacket(arr, arr.length, add, 8345);
        dsock.send(dpack);                                   // send the packet
        Date sendTime = new Date();                          // note the time of sending the message

        dsock.receive(dpack);                                // receive the packet
        String message2 = new String(dpack.getData());
        Date receiveTime = new Date();   // note the time of receiving the message
        System.out.println((receiveTime.getTime() - sendTime.getTime()) + " milliseconds echo time for " + message2);
    }


    public udpClient(DatagramSocket dsock, SharedObject so) throws IOException {
        this.dsock = dsock;//constructor port
        this.so = so;
//        filePath = path;
    }

    public void changeRate(int newRate){
        this.rate = newRate;
    }
    public int getRate(){
        return this.rate;
    }

    //Three way handshake: sends out one packet, waits for response to calculate Round Trip Time,
    //then calculate window size and sends it to udpServer in third packet. Window size is 16 bit.
    // Finish.
    public static void establishCon(int port, String host, int rate, String fileName) throws IOException {
//        byte[] packet = new byte[1024];
        byte[] packet = fileName.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(packet, packet.length, InetAddress.getByName(host), port);
        dsock.send(datagramPacket);
//        long startTime = System.currentTimeMillis();


//        byte[] receiveData = new byte[1024];
//        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
//        dsock.receive(receivePacket);
//        long receiveTime = System.currentTimeMillis();
//        double RTT = (receiveTime - startTime) * 1.0 / 1000;
//        System.out.println("RTT is: " + RTT);
//        int window_size = (int) (rate * RTT );
//        System.out.println("Window size is: " + window_size);
//
//        byte[] thirdData = new byte[1024];
//        thirdData[0] = (byte) (window_size >> 8);
//        thirdData[1] = (byte) (window_size);
//        DatagramPacket thirdPacket = new DatagramPacket(thirdData, thirdData.length, InetAddress.getByName(host), port);
//        dsock.send(thirdPacket);

//        return RTT;
    }

    @Override
    public void run(){


        receive(Get_port(), Get_IP(), path.split("/")[1], Get_rate());

    }

    public void receive(int port, String host, String fileName, int rate) throws IOException {
        //对方udpport


        // Create the socket, set the address and create the file to be sent
//        DatagramSocket socket = new DatagramSocket(port);
//
        while(!so.getFlag()){
            //blocks.
        }

        establishCon(port, host, rate, fileName);

        receiveAndCreate(port, fileName);

//
//        InetAddress address;
//
//        File file = new File(fileName);
//        FileOutputStream outToFile = new FileOutputStream(file);
//
//        // Create a flag to indicate the last message
//        boolean lastMessageFlag = false;
//        boolean lastMessage = false;
//
//        // Store sequence number
//        int sequenceNumber = 0;
//        int lastSequenceNumber = 0;
//
//        // For each message we will receive
//        while (!lastMessage) {
//            // Create byte array for full message and another for file data without header
//            byte[] message = new byte[1024*32];
//            byte[] fileByteArray = new byte[1024*32];
//
//            // Receive packet and retreive message
//            DatagramPacket receivedPacket = new DatagramPacket(message, message.length);
//            socket.setSoTimeout(0);
//            socket.receive(receivedPacket);
//            message = receivedPacket.getData();
//
//            // Get port and address for sending ack
//            address = receivedPacket.getAddress();
//            port = receivedPacket.getPort();
//
//            // Retrieve sequence number
//            sequenceNumber = ((message[0] & 0xff) << 8) + (message[1] & 0xff);
//
//            // Retrieve the last message flag
//            if ((message[2] & 0xff) == 1) {
//                lastMessageFlag = true;
//            } else {
//                lastMessageFlag = false;
//            }
//
//            if (sequenceNumber == (lastSequenceNumber + 1)) {
//                lastSequenceNumber = sequenceNumber;
//
//                // Retrieve data from message
//                for (int i=3; i < 32*1024 ; i++) {
//                    fileByteArray[i-3] = message[i];
//                }
//
//                // Write the message to the file and print received message
//                outToFile.write(fileByteArray);
//                System.out.println("Received: Sequence number = " + lastSequenceNumber +", Flag = " + lastMessageFlag);
//
//                // Send acknowledgement
//                sendAck(lastSequenceNumber, socket, address, port);
//
//            } else {
//                System.out.println("Expected sequence number: " + (lastSequenceNumber + 1) + " but received " + sequenceNumber + ". DISCARDING");
//
//                //Resend the acknowledgement
//                sendAck(lastSequenceNumber, socket, address, port);
//            }
//
//            // Check for last message
//            if (lastMessageFlag) {
//                outToFile.close();
//                lastMessage = false;
//                break;
//            }
//        }
//        socket.close();
//        System.out.println("File " + fileName + " has been received.");
//        return new byte[0];
    }
//    public static void receiveAndCreate(int port, String fileName) throws IOException {
//        // Create the socket, set the address and create the file to be sent
//        DatagramSocket socket = new DatagramSocket(port);
//        InetAddress address;
//        File file = new File(fileName);
//        FileOutputStream outToFile = new FileOutputStream(file);
//
//        // Create a flag to indicate the last message
//        boolean lastMessageFlag = false;
//        boolean lastMessage = false;
//
//        // Store sequence number
//        int sequenceNumber = 0;
//        int lastSequenceNumber = 0;
//
//        // For each message we will receive
//        while (!lastMessage) {
//            // Create byte array for full message and another for file data without header
//            byte[] message = new byte[BUFFER_SIZE];
//            byte[] fileByteArray = new byte[BUFFER_SIZE - 3];
//
//            // Receive packet and retreive message
//            DatagramPacket receivedPacket = new DatagramPacket(message, message.length);
//            socket.setSoTimeout(0);
//            socket.receive(receivedPacket);
//            message = receivedPacket.getData();
//
//            // Get port and address for sending ack
//            address = receivedPacket.getAddress();
//            port = receivedPacket.getPort();
//
//            // Retrieve sequence number
//            sequenceNumber = ((message[0] & 0xff) << 8) + (message[1] & 0xff);
//
//            // Retrieve the last message flag
//            if ((message[2] & 0xff) == 1) {
//                lastMessageFlag = true;
//            } else {
//                lastMessageFlag = false;
//            }
//
//            if (sequenceNumber == (lastSequenceNumber + 1)) {
//                lastSequenceNumber = sequenceNumber;
//
//                // Retrieve data from message
//                for (int i=3; i < BUFFER_SIZE ; i++) {
//                    fileByteArray[i-3] = message[i];
//                }
//
//                // Write the message to the file and print received message
//                outToFile.write(fileByteArray);
//                System.out.println("Received: Sequence number = " + lastSequenceNumber +", Flag = " + lastMessageFlag);
//
//                // Send acknowledgement
//                sendAck(lastSequenceNumber, socket, address, port);
//
//            } else {
//                System.out.println("Expected sequence number: " + (lastSequenceNumber + 1) + " but received " + sequenceNumber + ". DISCARDING");
//
//                //Resend the acknowledgement
//                sendAck(lastSequenceNumber, socket, address, port);
//            }
//
//            // Check for last message
//            if (lastMessageFlag) {
//                outToFile.close();
//                lastMessage = false;
//                break;
//            }
//        }
//
//        socket.close();
//        System.out.println("File " + fileName + " has been received.");
//    }
    public static void receiveAndCreate(int port, String fileName) throws IOException {
    // Create the socket, set the address and create the file to be sent
//    DatagramSocket socket = new DatagramSocket(port);
    DatagramSocket socket = dsock;
    InetAddress address;
    File file = new File("./content/" + fileName);
    FileOutputStream outToFile = new FileOutputStream(file);

    // Create a flag to indicate the last message
    boolean lastMessageFlag = false;

    // Store sequence number
    int sequenceNumber = 0;
    int lastSequenceNumber = 0;

    // For each message we will receive
    while (!lastMessageFlag) {
        // Create byte array for full message and another for file data without header
        byte[] message = new byte[BUFFER_SIZE];
        byte[] fileByteArray = new byte[BUFFER_SIZE - 3];

        // Receive packet and retreive message
        DatagramPacket receivedPacket = new DatagramPacket(message, message.length);
        socket.setSoTimeout(0);
        socket.receive(receivedPacket);
        message = receivedPacket.getData();

        // Get port and address for sending ack
        address = receivedPacket.getAddress();
        port = receivedPacket.getPort();//要改！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！

        // Retrieve sequence number
        sequenceNumber = ((message[0] & 0xff) << 8) + (message[1] & 0xff);

        // Retrieve the last message flag
        if ((message[2] & 0xff) == 1) {
            lastMessageFlag = true;
        } else {
            lastMessageFlag = false;
        }

        if (sequenceNumber == (lastSequenceNumber + 1)) {//Next in line packet -> update the window base. Ditch the window size change! Not necessary.

            map.put(sequenceNumber, message);

            while(map.containsKey(sequenceNumber)){
                //Write to file
                // Retrieve data from message

                byte[] writeMessage = map.get(sequenceNumber);

//                    for (int i=3; i < BUFFER_SIZE ; i++) {
////                        fileByteArray[i-3] = message[i];
//                        fileByteArray[i - 3] = writeMessage[i];
//                    }
                System.arraycopy(writeMessage, 3, fileByteArray, 0, fileByteArray.length);

                // Write the message to the file
                outToFile.write(fileByteArray);
                System.out.println("Received: Sequence number = " + sequenceNumber +", Flag = " + lastMessageFlag);

                //map pop
                map.remove(sequenceNumber);

                //window roll up
                sequenceNumber++;
                lastSequenceNumber++;

            }

//                // Update latest sequence number
//                lastSequenceNumber = sequenceNumber;

            // Send acknowledgement
            sendAck(lastSequenceNumber, socket, address, port);



            // Check for last message
            if (lastMessageFlag) {
                outToFile.close();
            }
        } else {
            // If packet has been received, send ack for that packet again
            // Seq <= lastSeq: repeat packet. Retransmit current ack
            if (sequenceNumber < (lastSequenceNumber + 1)) {
                // Send acknowledgement for received packet
                sendAck(sequenceNumber, socket, address, port);//OK to not send anything
            } else {//sequence number > lastseq + 1: gap.
                // Resend acknowledgement for last packet received

                map.put(sequenceNumber, message);

                sendAck(lastSequenceNumber, socket, address, port);
            }
        }
    }

    socket.close();
    System.out.println("File " + fileName + " has been received.");
}

    public static void sendAck(int lastSequenceNumber, DatagramSocket socket, InetAddress address, int port) throws IOException {
        // Resend acknowledgement
        byte[] ackPacket = new byte[2];
        ackPacket[0] = (byte)(lastSequenceNumber >> 8);
        ackPacket[1] = (byte)(lastSequenceNumber);
        DatagramPacket acknowledgement = new  DatagramPacket(ackPacket, ackPacket.length, address, port);
        socket.send(acknowledgement);
        System.out.println("Sent ack: Sequence Number = " + lastSequenceNumber);
    }

/*
    public static void UDP_Receiver () throws IOException {
        DatagramSocket serverSocket = new DatagramSocket(9876);
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket);
        String sentence = new String(receivePacket.getData());
        InetAddress IPAddress = receivePacket.getAddress();
        int port = receivePacket.getPort();
        System.out.println("Received packet from " + IPAddress + ":" + port);
        System.out.println("Data: " + sentence);
        serverSocket.close();

    }*/

}
