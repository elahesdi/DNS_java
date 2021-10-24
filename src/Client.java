/*
import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class Client {

    public QueryType queryType = QueryType.A;
    public int MAX_DNS_PACKET_SIZE = 512;
    private int timeout = 5000;
    private int maxRetries = 3;
    private byte[] server = new byte[4];
    String address;
    private String name;
    private int port = 53;

    public Client(String args[]) {
        try {
            this.parseInputs(args);
        } catch (Exception e) {
            throw new IllegalArgumentException("ERROR\tIncorrect input syntax: Please check arguments and try again");
        }
        if (server == null || name == null) {
            throw new IllegalArgumentException("ERROR\tIncorrect input syntax: Server IP and domain name must be provided.");
        }
    }

    public void createRequest() {
        System.out.println(" send request for " + name + "  |  Server: " + address + " | type of request: " + queryType);
        sendRequest(1);
    }

    private void sendRequest(int retryNumber) {
        if (retryNumber > maxRetries) {
            System.out.println("ERROR\tMaximum number of retries " + maxRetries + " exceeded");
            return;
        }

        try {
            //Create Datagram socket and request object(s)
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(timeout);
            InetAddress inetaddress = InetAddress.getByAddress(server);
            Request request = new Request(name, queryType);

            byte[] requestBytes = request.getRequest();
            byte[] responseBytes = new byte[1024];

            DatagramPacket requestPacket = new DatagramPacket(requestBytes, requestBytes.length, inetaddress, port);
            DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);

            //Send packet and time response
            long startTime = System.currentTimeMillis();
            socket.send(requestPacket);
            socket.receive(responsePacket);
            long endTime = System.currentTimeMillis();
            socket.close();

            System.out.println("Response received after " + (endTime - startTime) / 1000. + " seconds " + "(" + (retryNumber - 1) + " retries)");

            Response response = new Response(responsePacket.getData(), requestBytes.length, queryType);
            response.showResponse();

        } catch (SocketException e) {
            System.out.println("ERROR\tCould not create socket");
        } catch (UnknownHostException e) {
            System.out.println("ERROR\tUnknown host");
        } catch (SocketTimeoutException e) {
            System.out.println("ERROR\tSocket Timeout");
            System.out.println("Reattempting request...");
            sendRequest(++retryNumber);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void parseInputs(String args[]) {
        List<String> argsList = Arrays.asList(args);
        ListIterator<String> iterator = argsList.listIterator();

        while (iterator.hasNext()) {
            String arg = iterator.next();

            if (arg.equals("-p"))
                port = Integer.parseInt(iterator.next());
            else if (arg.equals("-mx"))
                queryType = QueryType.MX;
            else if (arg.equals("-ns"))
                queryType = QueryType.NS;

            else {
                if (arg.contains("@")) {
                    address = arg.substring(1);
                    String[] addressComponents = address.split("\\.");

                    for (int i = 0; i < addressComponents.length; i++) {
                        int ip = Integer.parseInt(addressComponents[i]);
                        if (ip < 0 || ip > 255) {
                            throw new NumberFormatException("ERROR    IP Address numbers must be between 0 and 255, inclusive.");
                        }
                        server[i] = (byte) ip;
                    }
                    name = iterator.next();
                }
            }

        }
    }

}*/
import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class Client {

    public QueryType queryType = QueryType.A;
    public int MAX_DNS_PACKET_SIZE = 512;
    private int timeout = 5000;
    private int maxRetries = 3;
    private byte[] server = new byte[4];
    String address;
    private String name;
    private int port = 53;

    public Client(String args[]) {
        try {
            this.parseInputs(args);
        } catch (Exception e) {
            throw new IllegalArgumentException("ERROR   Please check arguments and try again");
        }
        if (server == null || name == null) {
            throw new IllegalArgumentException("ERROR    Server IP and domain name must be provided.");
        }
    }

    public void makeRequest() {
        System.out.println("DnsClient sending request for " + name);
        System.out.println("Server: " + address);
        System.out.println("Request type: " + queryType);
        pollRequest(1);
    }

    private void pollRequest(int retryNumber) {
        if (retryNumber > maxRetries) {
            System.out.println("ERROR\tMaximum number of retries " + maxRetries+ " exceeded");
            return;
        }

        try {
            //Create Datagram socket and request object(s)
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(timeout);
            InetAddress inetaddress = InetAddress.getByAddress(server);
            Request request = new Request(name, queryType);

            byte[] requestBytes = request.getRequest();
            byte[] responseBytes = new byte[1024];

            DatagramPacket requestPacket = new DatagramPacket(requestBytes, requestBytes.length, inetaddress, port);
            DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);

            //Send packet and time response
            long startTime = System.currentTimeMillis();
            socket.send(requestPacket);
            socket.receive(responsePacket);
            long endTime = System.currentTimeMillis();
            socket.close();

            System.out.println("Response received after " + (endTime - startTime)/1000. + " seconds " + "(" + (retryNumber - 1) + " retries)");

            Response response = new Response(responsePacket.getData(), requestBytes.length, queryType);
            response.showResponse();
            //iterative
            while (response.getANCount() == 0){
                response.setRDFalse();
                if (response.getAdditionalRecords().length != 0 ){
                    int i = 0;
                    while (response.getAdditionalRecords()[i]==null){
                        i++;
                    }
                     address = response.getAdditionalRecords()[0].getDomain();
                   // System.out.println(i+"  = address : " + address);
                     ipToBytes();
                     socket = new DatagramSocket();
                     socket.setSoTimeout(timeout);
                     inetaddress = InetAddress.getByAddress(server);
                     request = new Request(name, queryType);

                    byte[] requestBytes1 = request.getRequest();
                    byte[] responseBytes1 = new byte[1024];

                     requestPacket = new DatagramPacket(requestBytes1, requestBytes1.length, inetaddress, port);
                     responsePacket = new DatagramPacket(responseBytes1, responseBytes1.length);

                    //Send packet and time response
                     startTime = System.currentTimeMillis();
                    socket.send(requestPacket);
                    socket.receive(responsePacket);
                     endTime = System.currentTimeMillis();
                    socket.close();
                    System.out.println("Response received after " + (endTime - startTime)/1000. + " seconds " + "(" + (retryNumber - 1) + " retries)");
                    response = new Response(responsePacket.getData(), requestBytes.length, queryType);
                    response.showResponse();
                }
            }



        } catch (SocketException e) {
            System.out.println("ERROR\tCould not create socket");
        } catch (UnknownHostException e ) {
            System.out.println("ERROR\tUnknown host");
        } catch (SocketTimeoutException e) {
            System.out.println("ERROR\tSocket Timeout");
            System.out.println("Reattempting request...");
            pollRequest(++retryNumber);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void parseInputs(String args[]) {
        List<String> argsList = Arrays.asList(args);
        ListIterator<String> iterator = argsList.listIterator();

        while (iterator.hasNext()) {
            String arg = iterator.next();

            if (arg.equals("-p"))
                port = Integer.parseInt(iterator.next());
            else if (arg.equals("-mx"))
                queryType = QueryType.MX;
            else if (arg.equals("-ns"))
                queryType = QueryType.NS;

            else {
                if (arg.contains("@")) {
                    address = arg.substring(1);
                   ipToBytes();
                    name = iterator.next();
                }
            }

        }
    }

    public void ipToBytes(){
        String[] addressComponents = address.split("\\.");

        for (int i = 0; i < addressComponents.length; i++) {
            int ip = Integer.parseInt(addressComponents[i]);
            if (ip < 0 || ip > 255) {
                throw new NumberFormatException("ERROR    IP Address numbers must be between 0 and 255, inclusive.");
            }
            server[i] = (byte) ip;
        }
    }

}
