import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * CS508
 *
 * @author zea_an
 * @since 2022-03-09
 *
 */
public class ChatServer implements Runnable {
    private final Socket clientSocket;
    private PrintWriter toClient;
    private BufferedReader fromClient;
    private static final AtomicReference<List<PrintWriter>> clients = new AtomicReference<>(new ArrayList<>());

    /**
     * Serves a ChatServer instance for each client that connects
     * @param clientSocket The socket created when the client connects to the server.
     */
    public ChatServer(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }


    public void run() {
        System.out.println("Client Connected");
        try {
            /**
             * toClient PrintWriter objects serves as the outgoing connection to client. clients is an atomic arrayList
             * of all the current PrintWriter objects available.
             */
            toClient = new PrintWriter(clientSocket.getOutputStream(), true);
            clients.updateAndGet((list) -> {
                list.add(toClient);
                return list;
            });

            /**
             * fromClient is the BuffererReader tied into the clients input.  So fromClient will receive messages from
             * a particular client connected to this thread instance of the server.
             */
            fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String name = fromClient.readLine();
            String str = fromClient.readLine();

            /**
             * If client sends "close" then the while loop will end the server's stop() function will run
             */
            while (str != null && !str.equalsIgnoreCase("close")) {
                /**
                 * Prints the clients message on the server's side to stdout
                 */
                System.out.println(name + ": " + str);

                /**
                 * Get a list of current clients
                 */
                List<PrintWriter> currentClients = clients.get();
                /**
                 * Prints the message from this client to all clients who have a PrintWriter in the clients list.
                 */
                for (PrintWriter p : currentClients) {
                    p.println(name + ": " + str);
                }
                /**
                 * Reads the next line from the client.
                 */
                str = fromClient.readLine();
            }
            /**
             * Handles disconnecting the client from the server.
             */
            stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles disconnecting the client from the server
     *
     * @throws IOException
     */
    public void stop() throws IOException {
        System.out.println("Client Closed Connection");
        /**
         * Removes this toClient PrintWriter from the clients list
         */
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
        /**
         * Starts a new thread upon receiving a new connection from a client.
         */
        while (true) {
            Socket clientSocket = serverSocket.accept();
            Thread thread = new Thread(new ChatServer(clientSocket));
            thread.start();
        }
    }
}
