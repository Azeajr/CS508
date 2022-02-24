import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ChatServer implements Runnable {
    private final Socket clientSocket;
    private PrintWriter toClient;
    private BufferedReader fromClient;
    private static final AtomicReference<List<PrintWriter>> clients = new AtomicReference<>(new ArrayList<>());

//    private static List<PrintWriter> clients = new ArrayList<PrintWriter>();

    public ChatServer(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        System.out.println("Client Connected");
        try {
            toClient = new PrintWriter(clientSocket.getOutputStream(), true);
            clients.updateAndGet((list) -> {
                list.add(toClient);
                return list;
            });

            fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String name = fromClient.readLine();
            String str = fromClient.readLine();

            while (str != null) {
                System.out.println(name + ": " + str);

                List<PrintWriter> currentClients = clients.get();
                for (PrintWriter p : currentClients) {
                    p.println(name + ": " + str);
                }
                str = fromClient.readLine();
            }
            stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() throws IOException {
        clients.updateAndGet((list) -> {
            list.remove(toClient);
            return list;
        });
        fromClient.close();
        toClient.close();
        clientSocket.close();
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(6666);
        while (true) {
            Socket clientSocket = serverSocket.accept();
            Thread thread = new Thread(new ChatServer(clientSocket));
            thread.start();
        }
    }
}
