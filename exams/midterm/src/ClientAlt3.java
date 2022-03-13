import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


public class ClientAlt3 {

    private static String makeRequest(String url) throws IOException {
        URL tgt = new URL(url);
        HttpURLConnection con = (HttpURLConnection) tgt.openConnection();
        con.setRequestMethod("GET");
        InputStream is;
        try {
            is = con.getInputStream();
        } catch (IOException ex) {
            is = con.getErrorStream();
        }
        //InputStream is = con.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append('\n');
            line = br.readLine();
        }
        is.close();
        return sb.toString();
    }

    private static class PoemGetter implements Runnable {
        String rootSite = "http://raven.quartyard.us/stanza/";
        //static StringBuilder poem = new StringBuilder();
        BlockingQueue<String> line = new ArrayBlockingQueue<>(1);
//        AtomicInteger stanza = new AtomicInteger(1);
        int stanza;
        static AtomicReference<StringBuilder> poem = new AtomicReference<StringBuilder>(new StringBuilder());
//        String line = null;

        PoemGetter(int stanza){
            this.stanza = stanza;
        }




        @Override
        public void run() {
            try {
//                line.add( makeRequest(rootSite + stanza));
                line.offer(makeRequest(rootSite + stanza),1, TimeUnit.NANOSECONDS);

            } catch (Exception e) {
                e.printStackTrace();
                // Thread.currentThread().interrupt();
            }

//            while(stanza.get() <=3){
//                try {
//                    int tempStanza = stanza.get();
//                    System.out.println(tempStanza);
//                    line = makeRequest(rootSite + tempStanza);
//                    poem.updateAndGet((poem)->{
//                        if(stanza.get() == tempStanza){
//                            poem.append(line);
//                            stanza.incrementAndGet();
//                        }
//                        return poem;
//                    });
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
        }

        public String getPoem() {
            return line.peek();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        PoemGetter poemGetter = new PoemGetter(1);
//        poemGetter.run();
        Thread[] threads = new Thread[5];

        for(int j = 1; j <=18; j++){
            System.out.println(j);
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread(new PoemGetter(j));
                threads[i].start();
            }

            for (int i = 0; i < threads.length; i++) {
                threads[i].join();
            }
        }

        System.out.println(poemGetter.getPoem());
    }
}