import java.util.concurrent.atomic.AtomicReference;

public class AtomicRefrenceDemo {
    static Integer x = new Integer(55);
    static AtomicReference<Integer> atomicX = new AtomicReference<Integer>(x);

    public static void main(String[] args) {

        Integer temp = atomicX.get();
        System.out.println(atomicX.get().getClass().getName());
    }



}
