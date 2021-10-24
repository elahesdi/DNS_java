public class Main {
    public static void main(String args[]) throws Exception {
        try {
            Client client = new Client(args);
            client.makeRequest();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}


