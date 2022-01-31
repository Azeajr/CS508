public class ThreadedSum {
    private static class SumInterval implements Runnable {

        long high, low;
        long step = 1;
        long sum = 0;
        StringBuilder sumString = new StringBuilder();

        public SumInterval(long low, long high, long step) {
            this.low = low;
            this.high = high;
            this.step = step;
        }

        @Override
        public void run() {
            for (long i = low; i <= high; i+=step) {
                sum += i;
                if(low % step)
                sumString.append(i);

            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 1-250,000,000
        SumInterval threadSum1 = new SumInterval(1, 250000000);

        SumInterval threadSum2 = new SumInterval(1, 125_000_000);
        SumInterval threadSum3 = new SumInterval(125_000_001, 250000000);




        Thread t1 = new Thread(threadSum2);
        Thread t2 = new Thread(threadSum3);
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("First thread sum: " + threadSum2.sum);
        System.out.println("Second thread sum: " + threadSum3.sum);
        System.out.println("Total: " + (threadSum2.sum + threadSum3.sum));

    }
}
