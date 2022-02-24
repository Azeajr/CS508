import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;

public class SocketExample {

    static class TestServer  implements Runnable{
        private ServerSocket serverSocket;
        private Socket clientSocket;
        private PrintWriter toClient;
        private BufferedReader fromClient;

        public TestServer(int port) throws IOException {
            serverSocket = new ServerSocket(port);
        }

        public void  run(){
            try{
                clientSocket = serverSocket.accept();
                toClient = new PrintWriter(clientSocket.getOutputStream(), true);
                fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String str = fromClient.readLine();
                while (str != null) {
                    if(str.contains("zea")){
                        toClient.println(str + "\tContains zea");
                    }else{
                        toClient.println(str + "\tDoes not contain zea");
                    }
                    str = fromClient.readLine();
                }
                stop();
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        public void stop() throws IOException {
            fromClient.close();
            toClient.close();
            clientSocket.close();
            serverSocket.close();
        }
    }

    static class TestClient implements Runnable {
        private Socket clientSocket;
        private PrintWriter toServer;
        private BufferedReader fromServer;
        private BufferedReader keyboard;

        public TestClient(String ip, int port) throws IOException {
            clientSocket = new Socket(ip, port);
            toServer = new PrintWriter(clientSocket.getOutputStream(), true);
            fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            keyboard = new BufferedReader(new InputStreamReader(System.in));
        }

        public void run(){

        }

        public void sendMessages() throws IOException {
            String str = keyboard.readLine();
            while(!str.equalsIgnoreCase("bye")){
                toServer.println(str);
                toServer.flush();
                System.out.println(fromServer.readLine());
                str= keyboard.readLine();
            }
        }

        public void stopConnection() throws IOException {
            fromServer.close();
            toServer.close();
            clientSocket.close();
            keyboard.close();
        }
    }

    public static void main(String[] args) throws IOException {
        TestServer server = new TestServer(6666);
        Thread thread1 = new Thread(server);
        thread1.start();

        TestClient client = new TestClient("127.0.0.1", 6666);
        client.sendMessages();
        client.stopConnection();

    }
}
