public class ThreadedSummation {
    private static class Summation implements Runnable {

        int number, step, sum;
        StringBuilder sumString = new StringBuilder();

        public Summation(int number, int step) {
            this.number = number;
            this.step = step;
            sum = 0;
        }

        @Override
        public void run() {
            int temp;
            for (int i = 1; i <= number; i++) {
                temp = 1 + step * (i - 1);
                sum += temp;
                if (i != number) {
                    sumString.append(temp).append(", ");
                } else {
                    sumString.append(temp);
                }

            }
            System.out.println("Thread: " + Thread.currentThread().getId() + " Sum: " + sum + "\nSummation: " + sumString.toString());
        }
    }

    public static void main(String[] args) throws InterruptedException {

        Thread t1 = new Thread(new Summation(100, 1));
        Thread t2 = new Thread(new Summation(100, 2));
        Thread t3 = new Thread(new Summation(100, 5));
        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        System.out.println("Finally");

    }
}
