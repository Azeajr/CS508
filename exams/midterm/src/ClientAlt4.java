import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


public class ClientAlt4 {

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
        static StringBuilder poem = new StringBuilder();
        BlockingQueue<String> line = new ArrayBlockingQueue<>(1);


        @Override
        public void run() {
            while (line.isEmpty()) {
                try {
                    line.add(makeRequest(rootSite));
                    poem.append(line.peek());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public String getPoem() {
            return poem.toString();
        }
    }

    public static class StanzaGetter implements Callable<String> {
        ExecutorService pool = Executors.newFixedThreadPool(5);
        CompletionService<String> stanzaGetterCompletionService = new ExecutorCompletionService<>(pool);
        StringBuilder poem = new StringBuilder();



        @Override
        public String call() throws Exception {
            String stanza;
            List<Future<String>> futures = new ArrayList<>(5);

            for (int j = 0; j < 5; j++) {
                System.out.println("yay");
                Callable<String> c = () -> {
                    return makeRequest("http://raven.quartyard.us/stanza/" + 1);
                };
                futures.add(stanzaGetterCompletionService.submit(c));
            }
            try {
                for (int i = 0; i < 5; i++) {
                    try{
                        String s = stanzaGetterCompletionService.take().get();
                    } catch (ExecutionException ignore) {}
                }
            }finally {
                for(Future<String > f: futures){
                    f.cancel(true);
                }
            }

            return null;
        }
    }


    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        ExecutorService pool = Executors.newFixedThreadPool(5);
        CompletionService<String> stanzaGetterCompletionService = new ExecutorCompletionService<>(pool);
        StringBuilder poem = new StringBuilder();

        List<Future<String>> futures = new ArrayList<>(5);

        for(int i = 1; i <= 18; i++){
            System.out.println(i);
            for (int j = 0; j < 5; j++) {
                int finalI = i;
                Callable<String> c = () -> {
                    return makeRequest("http://raven.quartyard.us/stanza/" + finalI);
                };
                futures.add(stanzaGetterCompletionService.submit(c));
            }
            try {
                for (int k = 0; k < 5; k++) {
                    try {
                        String r = stanzaGetterCompletionService.take().get();
                        if (r != null) {
                            poem.append(r);
                            break;
                        }
                    } catch (Exception ignore) {
                    }
                }
            } finally {
                for (Future<String> f : futures) {
                    f.cancel(true);
                }
                futures.removeAll(futures);
            }
        }

        System.out.println(poem);
        pool.shutdown();
    }
}