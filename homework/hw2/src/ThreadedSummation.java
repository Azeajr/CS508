import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class ThreadedSummation {
    private static class Summation implements Runnable {
        static AtomicReference finalSum = new AtomicReference<>(new BigInteger("0"));
        BigInteger interSum;
        List<Integer> randInts;
        int startIndex, endIndex;

        private Summation(List<Integer> randInts, int startIndex, int endIndex) {
            interSum = new BigInteger("0");
            this.randInts=randInts;
            this.startIndex=startIndex;
            this.endIndex=endIndex;
        }

        @Override
        public void run() {
            for (int i = startIndex; i < endIndex; i++) {
                interSum = interSum.add(BigInteger.valueOf(randInts.get(i).longValue()));
            }
            finalSum.updateAndGet((v)-> ((BigInteger)v).add(interSum));
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
        Summation temp = new Summation(testData,0, testData.size()/2);
        Summation temp2 = new Summation(testData,testData.size()/2, testData.size());

        Thread t1 = new Thread(temp);
        Thread t2 = new Thread(temp2);
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("Sum: "+ temp.finalSum);
        System.out.println("Sum2: "+ temp2.finalSum);

        Summation temp3 = new Summation(testData,0, testData.size()/2);
        Summation temp4 = new Summation(testData,testData.size()/2, testData.size());

        Thread t3 = new Thread(temp3);
        Thread t4 = new Thread(temp4);
        t3.start();
        t4.start();
        t3.join();
        t4.join();

        System.out.println("Sum: "+ temp3.finalSum);
        System.out.println("Sum2: "+ temp4.finalSum);

        long numSUm = 0;
        for (int i = 0; i < testData.size(); i++) {
            numSUm+= testData.get(i).intValue();
        }
        System.out.println("Manual sum: " + numSUm);
    }
}
