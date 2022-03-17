import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * CS508
 *
 * @author zea_an
 * @since 2022-03-13
 */
public class Client {
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

    private static class PoemGetter {
        final String rootSite = "http://raven.quartyard.us/stanza/";
        static BlockingQueue<String>[] poem = new BlockingQueue[18];

        PoemGetter() {
            for (int i = 0; i < poem.length; i++) {
                poem[i] = new ArrayBlockingQueue<>(1);
            }
        }

        /**
         * Returns an array of threads that will get the stanza of the poem requested.
         *
         * @param stanza The stanza of the poem we are trying to get
         * @return Array of 5 threads that will request this stanza from the site
         */
        public Thread[] getStanzaThreads(int stanza, int numThreads) {
            Thread[] threads = new Thread[numThreads];

            for (int i = 0; i < numThreads; i++) {
                Runnable x = () -> {

                    try {
                        poem[stanza - 1].add(makeRequest(rootSite + stanza));

                    } catch (IllegalStateException | IOException ignore) {
                    }
                };

                threads[i] = new Thread(x);
            }
            return threads;
        }

        /**
         * Converts string array to a single string.
         *
         * @return Raven poem as a string
         */
        public String getPoem() throws InterruptedException {
            StringBuilder sb = new StringBuilder();

            for (BlockingQueue<String> bq : poem) {
                sb.append(bq.poll(5, TimeUnit.SECONDS));
            }

            return sb.toString();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        PoemGetter poemGetter = new PoemGetter();
        Thread print = new Thread(() -> {
            try {
                System.out.println(poemGetter.getPoem());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        print.start();

        int numThreads = 10;

        Thread[][] poemThreads = new Thread[18][numThreads];

        for (int i = 0; i < 18; i++) {
            poemThreads[i] = poemGetter.getStanzaThreads(i + 1, numThreads);
        }

        for (Thread[] poemThread : poemThreads) {
            for (int j = 0; j < poemThreads[0].length; j++) {
                poemThread[j].start();
            }
        }

        for (Thread[] poemThread : poemThreads) {
            for (int j = 0; j < poemThreads[0].length; j++) {
                poemThread[j].join();
            }
        }

        print.join();
    }
}