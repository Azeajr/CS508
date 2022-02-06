import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class ThreadedSummation {
    private static class Summation implements Runnable {
        BigInteger sum;
        Iterator<Integer> iterator;

        private Summation(List<Integer> randInts) {
            iterator = randInts.iterator();
            sum = new BigInteger("0");
        }

        @Override
        public void run() {
            while(iterator.hasNext()){
                sum = sum.add(BigInteger.valueOf(iterator.next().longValue()));
            }
        }
    }

    public static List<Integer> randIntegerArray(int arraySize, int maxValue){
        List<Integer> randInts = new ArrayList<>(arraySize);
        Random rand = new Random();
        for (int i = 0; i < arraySize; i++) {
            randInts.add(rand.nextInt(maxValue));
        }
        return randInts;
    }

    public static long timeTrial(Runnable prog, int numThreads) throws InterruptedException {
        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(prog);
        }

        long start = System.nanoTime();

        for(Thread x: threads){
            x.start();
        }

        for(Thread x: threads){
            x.join();
        }

        long end = System.nanoTime();


        return end-start;
    }

    public static void main(String[] args) throws InterruptedException {
        List<Integer> testData = randIntegerArray(250, 100);
        Summation temp = new Summation(testData);
        System.out.println("Nano Seconds: " + timeTrial(temp, 1) + "\nSum: " + temp.sum);

        Summation temp2 = new Summation(testData);
        System.out.println("Nano Seconds: " + timeTrial(temp2, 2) + "\nSum: " + temp2.sum);
    }
}
