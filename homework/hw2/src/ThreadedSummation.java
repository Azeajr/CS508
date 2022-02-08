import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

/**
 * CS508
 * @author zea_an
 * @since 2022-02-08
 */
public class ThreadedSummation {
    /**
     * Summation class will handle adding the integers contained withing a portion of a list.  By using multiple
     * instances we can subdivide the list and split the work across different threads.
     */
    private static class Summation implements Runnable {
        static AtomicReference<BigInteger> finalSum = new AtomicReference<>(new BigInteger("0"));
        BigInteger interSum;
        List<Integer> randInts;
        int startIndex, endIndex;

        /**
         * Summation class constructor
         * Computes the sum of the Integers within the portion of the list that is passed.
         * @param randInts List of Integer objects
         * @param startIndex The starting index of the section of the list to be summed
         * @param endIndex The ending index(inclusive) of the section of the list to be summed
         */
        private Summation(List<Integer> randInts, int startIndex, int endIndex) {
            interSum = new BigInteger("0");
            this.randInts = randInts;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        /**
         *
         */
        @Override
        public void run() {
            for (int i = startIndex; i <= endIndex; i++) {
                interSum = interSum.add(BigInteger.valueOf(randInts.get(i).longValue()));
            }
            finalSum.updateAndGet(v -> v.add(interSum));
        }

        /**
         * Returns the final sum from the Summation class
         * @return The cumulative sum from collected from every Summation object
         */
        public static BigInteger getFinalSum() {
            return finalSum.get();
        }

        /**
         * Resets the final sum back to zero to be used to sum another list
         */
        public static void resetFinalSum() {
            finalSum = new AtomicReference<>(new BigInteger("0"));
        }
    }

    /**
     * Creates a List object populated with arraySize many random Integers whose value will range from 0 to
     * maxValue(exclusive).
     * @param arraySize Initial capacity of the list
     * @param maxValue The high bound(exclusive) of the possible Integers in the list.
     * @return List object containing random Integers
     */
    public static List<Integer> randIntegerArray(int arraySize, int maxValue) {
        List<Integer> randInts = new ArrayList<>(arraySize);
        Random rand = new Random();
        for (int i = 0; i < arraySize; i++) {
            randInts.add(rand.nextInt(maxValue));
        }
        return randInts;
    }

    /**
     * Function to facilitate the testing of the Summation class using various number of threads.
     * @param testData  The List to be used by all instances of the Summation class.
     * @param numThreads Number of threads to use for this time trial run
     * @return The number of milliseconds elapsed during the trial run as a long value
     * @throws InterruptedException Exception thrown when a thread is while it is waiting, sleeping, etc.
     */
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

        long start = System.currentTimeMillis();

        for (Thread x : threads) {
            x.start();
        }

        for (Thread x : threads) {
            x.join();
        }

        long end = System.currentTimeMillis();
        return end - start;
    }

    /**
     * Prints a histogram from the array values passed in
     * @param histogram An array representing values to be converted into string of characters for histogram
     */
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

    /**
     * Creates a string of stars of length equal to number passed in
     * @param starCount Number of stars to produce
     * @return String of stars of length starCount
     */
    public static String convertToStars(long starCount) {
        StringBuilder stars = new StringBuilder();
        for (long j = 0; j < starCount; j++) {
            stars.append('*');
        }
        return stars.toString();
    }

    public static void main(String[] args) throws InterruptedException {
        List<Integer> testData = randIntegerArray(250_000_000, 100);
        long[] histogram = new long[100];

        for (int i = 0; i < 100; i++) {
            histogram[i] = timeTrial(testData, i + 1);
            System.out.printf("Using %d threads\n", i+1);
            System.out.printf("The sum was %d\n", Summation.getFinalSum());
            System.out.printf("The computation took %d milliseconds\n", histogram[i]);
            Summation.resetFinalSum();
        }

        printHistogram(histogram);
    }
}
