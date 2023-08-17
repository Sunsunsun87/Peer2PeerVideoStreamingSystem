import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Vector;

public class udpServer extends Thread {
    private DatagramSocket dsock = null;
    private boolean closed = false;
    private static int rate = 0;
    private String filePath;
    private int window_size;

    private static final int PORT = 8345;
    private static int clientPORT;
    private static String clientIP;
    private static final int BUFFER_SIZE = 32 * 1024;
    private static final int TIMEOUT = 1000;

    private SharedObject so;

    public udpServer(DatagramSocket dsock, SharedObject so) throws IOException {
//        dsock = new DatagramSocket(udpPort);
        this.dsock = dsock;
        this.so = so;
//        this.file = file;
    }

    public void establishCon() throws IOException {
        System.out.println("waiting for UDP request......");
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        dsock.receive(receivePacket);
        byte[] pathData = receivePacket.getData();
        int length = receivePacket.getLength();
        this.filePath = "./content/" + new String(pathData, 0, length);
        System.out.println("received UDP request");

        clientPORT = receivePacket.getPort();
        clientIP = receivePacket.getAddress().getHostAddress();

//        byte[] respondData = new byte[1024];
//        DatagramPacket respondPacket = new DatagramPacket(respondData, respondData.length);
//        dsock.send(receivePacket);
//
//        byte[] receiveData2 = new byte[1024];
//        DatagramPacket receivePacket2 = new DatagramPacket(receiveData2, receiveData2.length);
//        dsock.receive(receivePacket2);
//
//        byte[] data = receivePacket2.getData();
//        int window_size = (data[0] & 0xff) << 8 + (data[1] & 0xff);
//        this.window_size = window_size;
//        System.out.println("window size is: " + window_size);
    }


    @Override
    public void run() {
        byte arr1[] = new byte[150];

        udpClient udpClient = null;//UDP client creation. Using designated port
        try {
            udpClient = new udpClient(dsock, so);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Client created");

//        while (true) {


//            byte[] buffer = new byte[BUFFER_SIZE];
//            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
//            try {
//                System.out.println("waiting for UDP request......");
//                dsock.receive(packet);//Initial packet containing path
//                System.out.println("received UDP request");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

//            byte[] path = packet.getData();
//            int pathLen = packet.getLength();
//            String Filename = new String(path, 0, pathLen);
//            String filePath = "./" + Filename;

            try {
                establishCon();
            } catch (IOException e) {
                e.printStackTrace();
            }

            File file = new File(filePath);
//            String IP =Get_IP();
//            int port = packet.getPort();

            try {
//                createAndSend(clientIP, clientPORT, filePath.split("/")[-1]);
                createAndSend(clientIP, clientPORT, filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

//        }
    }


    public String Get_IP() {
        List<String> contents = FileUtil.readFile("data.dat");
        String lastRow = contents.get(contents.size() - 1);
        return lastRow.split(" ")[1];

    }

    public void close() {
        closed = true;
    }

    public static void setRate(int rate) {
        udpServer.rate = rate;
    }
    public static void createAndSend(String hostName, int port, String fileName) throws IOException {
        System.out.println("Sending the file");

        // Create the socket, set the address and create the file to be sent
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName(hostName);
        File file = new File(fileName);

        // Create a byte array to store the filestream
        InputStream inFromFile = new FileInputStream(file);
        byte[] fileByteArray = new byte[(int)file.length()];
        inFromFile.read(fileByteArray);

        // Start timer for calculating throughput
        StartTime timer = new StartTime(0);

        // Create a flag to indicate the last message and a 16-bit sequence number
        int sequenceNumber = 0;
        boolean lastMessageFlag = false;

        // Create a flag to indicate the last acknowledged message and a 16-bit sequence number
        int ackSequenceNumber = 0;
        int lastAckedSequenceNumber = 0;
        boolean lastAcknowledgedFlag = false;

        // Create a counter to count number of retransmissions and initialize window size
        int retransmissionCounter = 0;
        int windowSize = 128;

        // Vector to store the sent messages
        Vector<byte[]> sentMessageList = new Vector <byte[]>();

        // For as each message we will create
        for (int i=0; i < fileByteArray.length; i = i + BUFFER_SIZE - 3 ) {

            // Increment sequence number
            sequenceNumber += 1;

            // Create new byte array for message
            byte[] message = new byte[BUFFER_SIZE];

            // Set the first and second bytes of the message to the sequence number
            message[0] = (byte)(sequenceNumber >> 8);
            message[1] = (byte)(sequenceNumber);

            // Set flag to 1 if packet is last packet and store it in third byte of header
            if ((i + BUFFER_SIZE - 3) >= fileByteArray.length) {
                lastMessageFlag = true;
                message[2] = (byte)(1);
            } else { // If not last message store flag as 0
                lastMessageFlag = false;
                message[2] = (byte)(0);
            }

            // Copy the bytes for the message to the message array
            if (!lastMessageFlag) {
                for (int j=0; j != BUFFER_SIZE - 3; j++) {
                    message[j+3] = fileByteArray[i+j];
                }
            }
            else if (lastMessageFlag) { // If it is the last message
                for (int j=0;  j < (fileByteArray.length - i); j++) {
                    message[j+3] = fileByteArray[i+j];
                }
            }

            // Package the message
            DatagramPacket sendPacket = new DatagramPacket(message, message.length, address, port);

            // Add the message to the sent message list
            sentMessageList.add(message);

            while (true) {
                // If next sequence number is outside the window
                if ((sequenceNumber - windowSize) > lastAckedSequenceNumber) {

                    boolean ackRecievedCorrect = false;
                    boolean ackPacketReceived = false;

                    while (!ackRecievedCorrect) {
                        // Check for an ack
                        byte[] ack = new byte[2];
                        DatagramPacket ackpack = new DatagramPacket(ack, ack.length);

                        try {
                            socket.setSoTimeout(50);
                            socket.receive(ackpack);
                            ackSequenceNumber = ((ack[0] & 0xff) << 8) + (ack[1] & 0xff);//seq number -> ack
                            ackPacketReceived = true;
                        } catch (SocketTimeoutException e) {
                            ackPacketReceived = false;
                            //System.out.println("Socket timed out while waiting for an acknowledgement");
                            //e.printStackTrace();
                        }

                        if (ackPacketReceived) {//receive3 send base(lastseq) - 1 (update when receive base + 1)
                            if (ackSequenceNumber >= (lastAckedSequenceNumber + 1)) {
                                lastAckedSequenceNumber = ackSequenceNumber;
                            }
                            ackRecievedCorrect = true;
                            System.out.println("Ack recieved: Sequence Number = " + ackSequenceNumber);
                            break; 	// Break if there is an ack so the next packet can be sent
                        } else { // Resend the packet
                            System.out.println("Resending: Sequence Number = " + sequenceNumber);
//                            // Resend the packet following the last acknowledged packet and all following that (cumulative acknowledgement)
//                            for (int y=0; y != (sequenceNumber - lastAckedSequenceNumber); y++) {
//                                byte[] resendMessage = new byte[1024];
//                                resendMessage = sentMessageList.get(y + lastAckedSequenceNumber);
//
//                                DatagramPacket resendPacket = new DatagramPacket(resendMessage, resendMessage.length, address, port);
//                                socket.send(resendPacket);
//                                retransmissionCounter += 1;
//                            }
                            //Resend the first packet.
                            byte[] resendMessge = new byte[BUFFER_SIZE];

                            //lastAckedSequenceNumber or +1 ?????????????????????????????????
                            //??????????????????????????????????????????????
                            resendMessge = sentMessageList.get(lastAckedSequenceNumber);

                            DatagramPacket resendPacket = new DatagramPacket(resendMessge, resendMessge.length, address, port);
                            socket.send(resendPacket);
                            retransmissionCounter += 1;

                        }
                    }
                } else { // Else pipeline is not full, break so we can send the message
                    break;
                }
            }

            // Send the message
            socket.send(sendPacket);
            System.out.println("Sent: Sequence number = " + sequenceNumber + ", Flag = " + lastMessageFlag);


            // Check for acknowledgements
            while (true) {
                boolean ackPacketReceived = false;
                byte[] ack = new byte[2];
                DatagramPacket ackpack = new DatagramPacket(ack, ack.length);

                try {
                    socket.setSoTimeout(10);
                    socket.receive(ackpack);
                    ackSequenceNumber = ((ack[0] & 0xff) << 8) + (ack[1] & 0xff);
                    ackPacketReceived = true;
                } catch (SocketTimeoutException e) {
                    //System.out.println("Socket timed out waiting for an ack");
                    ackPacketReceived = false;
                    //e.printStackTrace();
                    break;
                }

                // Note any acknowledgements and move window forward
                if (ackPacketReceived) {
                    if (ackSequenceNumber >= (lastAckedSequenceNumber + 1)) {
                        lastAckedSequenceNumber = ackSequenceNumber;
                        System.out.println("Ack recieved: Sequence number = " + ackSequenceNumber);
                    }
                }
            }
        }

        // Continue to check and resend until we receive final ack
        while (!lastAcknowledgedFlag) {

            boolean ackRecievedCorrect = false;
            boolean ackPacketReceived = false;

            while (!ackRecievedCorrect) {
                // Check for an ack
                byte[] ack = new byte[2];
                DatagramPacket ackpack = new DatagramPacket(ack, ack.length);

                try {
                    socket.setSoTimeout(50);
                    socket.receive(ackpack);
                    ackSequenceNumber = ((ack[0] & 0xff) << 8) + (ack[1] & 0xff);
                    ackPacketReceived = true;
                } catch (SocketTimeoutException e) {
                    //System.out.println("Socket timed out waiting for an ack1");
                    ackPacketReceived = false;
                    //e.printStackTrace();
                }

                // If its the last packet
                if (lastMessageFlag) {
                    lastAcknowledgedFlag = true;
                    break;
                }
                // Break if we receive acknowledgement so that we can send next packet
                if (ackPacketReceived) {
                    System.out.println("Ack recieved: Sequence number = " + ackSequenceNumber);
                    if (ackSequenceNumber >= (lastAckedSequenceNumber + 1)) {
                        lastAckedSequenceNumber = ackSequenceNumber;
                    }
                    ackRecievedCorrect = true;
                    break; // Break if there is an ack so the next packet can be sent
                } else { // Resend the packet
                    // Resend the packet following the last acknowledged packet and all following that (cumulative acknowledgement)
                    for (int j=0; j != (sequenceNumber-lastAckedSequenceNumber); j++) {
                        byte[] resendMessage = new byte[BUFFER_SIZE];
                        resendMessage = sentMessageList.get(j + lastAckedSequenceNumber);
                        DatagramPacket resendPacket = new DatagramPacket(resendMessage, resendMessage.length, address, port);
                        socket.send(resendPacket);
                        System.out.println("Resending: Sequence Number = " + lastAckedSequenceNumber);

                        // Increment retransmission counter
                        retransmissionCounter += 1;
                    }
                }
            }
        }

        socket.close();
        System.out.println("File " + fileName + " has been sent");

        // Calculate the average throughput
        int fileSizeKB = (fileByteArray.length) / 1024;
//        int transferTime = timer.getTimeElapsed() / 1000;
        double transferTime = timer.getTimeElapsed() * 1.0 / 1000;
        double throughput = (double) fileSizeKB / transferTime;
        System.out.println("File size: " + fileSizeKB + "KB, Transfer time: " + transferTime + " seconds. Throughput: " + throughput + "KBps");
        System.out.println("Number of retransmissions: " + retransmissionCounter);
    }

//    public static void createAndSend(String hostName, int port, String fileName) throws IOException {
//        System.out.println("Sending the file");
//
//        // Create the socket, set the address and create the file to be sent
//        DatagramSocket socket = new DatagramSocket();
//        InetAddress address = InetAddress.getByName(hostName);
//        File file = new File(fileName);
//
//        // Create a byte array to store the filestream
//        InputStream inFromFile = new FileInputStream(file);
//        byte[] fileByteArray = new byte[(int)file.length()];
//        inFromFile.read(fileByteArray);
//
//        // Start timer for calculating throughput
//        StartTime timer = new StartTime(0);
//
//        // Create a flag to indicate the last message and a 16-bit sequence number
//        int sequenceNumber = 0;
//        boolean lastMessageFlag = false;
//
//        // 16-bit sequence number for acknowledged packets
//        int ackSequenceNumber = 0;
//
//        // Create a counter to count number of retransmissions
//        int retransmissionCounter = 0;
//
//        // For as each message we will create
//        for (int i=0; i < fileByteArray.length; i = i+1021+31*1024 ) {
//
//            // Increment sequence number
//            sequenceNumber += 1;
//
//            // Create new message
//            byte[] message = new byte[1024*32];
//
//            // Set the first and second bytes of the message to the sequence number
//            message[0] = (byte)(sequenceNumber >> 8);
//            message[1] = (byte)(sequenceNumber);
//
//            // Set flag to 1 if packet is last packet and store it in third byte of header
//            if ((i+1021+31*1024) >= fileByteArray.length) {
//                lastMessageFlag = true;
//                message[2] = (byte)(1);
//            } else { // If not last message store flag as 0
//                lastMessageFlag = false;
//                message[2] = (byte)(0);
//            }
//
//            // Copy the bytes for the message to the message array
//            if (!lastMessageFlag) {
//                for (int j=0; j <= 31*1024+1020; j++) {
//                    message[j+3] = fileByteArray[i+j];
//                }
//            }
//            else if (lastMessageFlag) { // If it is the last message
//                for (int j=0;  j < (fileByteArray.length - i)  ;j++) {
//                    message[j+3] = fileByteArray[i+j];
//                }
//            }
//
//            // Send the message
//            DatagramPacket sendPacket = new DatagramPacket(message, message.length, address, port);
//            socket.send(sendPacket);
//            System.out.println("Sent: Sequence number = " + sequenceNumber + ", Flag = " + lastMessageFlag);
//
//            // For verifying the acknowledgements
//            boolean ackReceivedCorrect = false;
//            boolean ackPacketReceived = false;
//
//            while (!ackReceivedCorrect) {
//                // Check for an ack
//                byte[] ack = new byte[2];
//                DatagramPacket ackpack = new DatagramPacket(ack, ack.length);
//
//                try {
//                    socket.setSoTimeout(1000);
//                    socket.receive(ackpack);
//                    ackSequenceNumber = ((ack[0] & 0xff) << 8) + (ack[1] & 0xff);
//                    ackPacketReceived = true;
//                } catch (SocketTimeoutException e) {
//                    System.out.println("Socket timed out waiting for an ack");
//                    ackPacketReceived = false;
//                    //e.printStackTrace();
//                }
//
//                // Break if there is an ack so that the next packet can be sent
//                if ((ackSequenceNumber == sequenceNumber) && (ackPacketReceived)) {
//                    ackReceivedCorrect = true;
//                    System.out.println("Ack received: Sequence Number = " + ackSequenceNumber);
//                    break;
//                } else { // Resend packet
//                    socket.send(sendPacket);
//                    System.out.println("Resending: Sequence Number = " + sequenceNumber);
//
//                    // Increment retransmission counter
//                    retransmissionCounter += 1;
//                }
//            }
//        }
//
//        socket.close();
//        System.out.println("File " + fileName + " has been sent");
//
//        // Calculate the average throughput
//        int fileSizeKB = (fileByteArray.length) / 1024;
//        int transferTime = timer.getTimeElapsed() / 1000;
//        double throughput = (double) fileSizeKB / transferTime;
//        System.out.println("File size: " + fileSizeKB + "KB, Transfer time: " + transferTime + " seconds. Throughput: " + throughput + "KBps");
//        System.out.println("Number of retransmissions: " + retransmissionCounter);
//    }

    /*
    public static void UDP_Sender() throws Exception {
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName("localhost");
        byte[] header = new byte[8];
        // Add header data
        byte[] payload = "Hello, World!".getBytes();
        byte[] sendData = new byte[header.length + payload.length];
        System.arraycopy(header, 0, sendData, 0, header.length);
        System.arraycopy(payload, 0, sendData, header.length, payload.length);
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
        clientSocket.send(sendPacket);
        clientSocket.close();
    }
    */

}