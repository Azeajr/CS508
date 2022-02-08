import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class ThreadedSummation {
    private static class Summation implements Runnable {
        static AtomicReference<BigInteger> finalSum = new AtomicReference<>(new BigInteger("0"));
        BigInteger interSum;
        List<Integer> randInts;
        int startIndex, endIndex;

        private Summation(List<Integer> randInts, int startIndex, int endIndex) {
            interSum = new BigInteger("0");
            this.randInts = randInts;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        @Override
        public void run() {
            for (int i = startIndex; i <= endIndex; i++) {
                interSum = interSum.add(BigInteger.valueOf(randInts.get(i).longValue()));
            }
            finalSum.updateAndGet(v -> v.add(interSum));
        }

        public static BigInteger getFinalSum() {
            return finalSum.get();
        }

        public static void resetFinalSum() {
            finalSum = new AtomicReference<>(new BigInteger("0"));
        }
    }

    public static List<Integer> randIntegerArray(int arraySize, int maxValue) {
        List<Integer> randInts = new ArrayList<>(arraySize);
        Random rand = new Random();
        for (int i = 0; i < arraySize; i++) {
            randInts.add(rand.nextInt(maxValue));
        }
        return randInts;
    }

    public static long timeTrial(List<Integer> testData, int numThreads) throws InterruptedException {
        Thread[] threads = new Thread[numThreads];

        int smallStep = testData.size() / numThreads;
        int overflow = testData.size() % numThreads;


        int startIndex, endIndex = -1;

        for (int i = 0; i < numThreads; i++) {

            startIndex = endIndex + 1;
            if (i < overflow) {
                endIndex = startIndex + smallStep;
            } else {
                endIndex = startIndex + smallStep - 1;
            }
            threads[i] = new Thread(new Summation(testData, startIndex, endIndex));
        }

        long start = System.nanoTime();

        for (Thread x : threads) {
            x.start();
        }

        for (Thread x : threads) {
            x.join();
        }

        long end = System.nanoTime();
        return end - start;
    }

    public static void printHistogram(long[] histogram) {
        long largest = histogram[0];
        for (long l : histogram) {
            if (l > largest) {
                largest = l;
            }
        }

        for (int i = 0; i < histogram.length; i++) {
            System.out.println((i + 1) + "\t:" + convertToStars((histogram[i]* 100)/largest));
        }
    }

    public static String convertToStars(long nanoSeconds) {
        StringBuilder stars = new StringBuilder();
        for (long j = 0; j < nanoSeconds; j++) {
            stars.append('*');
        }
        return stars.toString();
    }

    public static void main(String[] args) throws InterruptedException {
        List<Integer> testData = randIntegerArray(250_000_000, 100);
        long[] histogram = new long[100];

        for (int i = 0; i < 100; i++) {
            histogram[i] = timeTrial(testData, i + 1);
            Summation.resetFinalSum();
        }

        printHistogram(histogram);
    }
}
