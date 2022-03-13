import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * CS508
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
        String rootSite = "http://raven.quartyard.us/stanza/";
        static AtomicReference<String[]> poem = new AtomicReference<>(new String[18]);
        String line = null;

        /**
         * Returns an array of threads that will get the stanza of the poem requested.
         * @param stanza The stanza of the poem we are trying to get
         * @return Array of 5 threads that will request this stanza from the site
         */
        public Thread[] getStanzaThreads(int stanza){
            Thread[] threads = new Thread[5];

            for (int i = 0; i < 5; i++) {
                Runnable x = () -> {
                    AtomicBoolean done = new AtomicBoolean(false);

                    try {
                        line = makeRequest(rootSite + stanza);

                        if (done.get()) {
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (line != null) {
                        poem.updateAndGet((poem) -> {
                            poem[stanza-1] = line;
                            done.set(true);

                            return poem;
                        });
                    }
                };

                threads[i] = new Thread(x);
            }
            return threads;
        }

        /**
         * Converts string array to a single string.
         * @return Raven poem as a string
         */
        public String getPoem() {
            StringBuilder sb = new StringBuilder();

            String[] poemComplete = poem.get();
            
            for (String s : poemComplete) {
                sb.append(s);
            }

            return sb.toString();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        PoemGetter poemGetter = new PoemGetter();

        Thread[][] poemThreads = new Thread[18][5];

        for (int i = 0; i < 18; i++) {
            poemThreads[i] = poemGetter.getStanzaThreads(i+1);
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

        System.out.println(poemGetter.getPoem());
    }
}