import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class ProductThread implements Runnable{
    private final String orderID;
    int numberOfProducts;
    BufferedReader productReader;
    BufferedWriter productWriter;
    AtomicInteger inQueue;
    ExecutorService threadPool;
    Object lock;
    AtomicInteger numberOfLevel2Threads;


    public ProductThread(String orderID, int numberOfProducts, BufferedReader productReader, BufferedWriter productWriter,
                         AtomicInteger inQueue, ExecutorService threadPool, Object lock, AtomicInteger numberOfLevel2Threads) {
        this.orderID = orderID;
        this.numberOfProducts = numberOfProducts;
        this.productReader = productReader;
        this.productWriter = productWriter;
        this.inQueue = inQueue;
        this.threadPool = threadPool;
        this.lock = lock;
        this.numberOfLevel2Threads = numberOfLevel2Threads;
    }
    @Override
    public void run() {
        String line;
        String lineOrderID;
        while (true) {
            try {
                if (!((line = productReader.readLine()) !=null)) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // extrag id-ul comenzii de pe linia curenta
            StringTokenizer token = new StringTokenizer(line, ",");
            lineOrderID = token.nextToken();

            // daca este egal cu id-comenzii aferente thread-ului, il scriu in fisierul de iesire
            // si notific thread-ul de nivel 1
            if (lineOrderID.equals(orderID)) {
                // am introdus o intarziere de 5 nanos pentru a testa mai bine scalabilitatea
                //try {
                //    Thread.sleep(0,5);
                //} catch (InterruptedException e) {
                //    throw new RuntimeException(e);
                //}
                try {
                        synchronized (lock) {
                            productWriter.write(line + ",shipped\n");
                            numberOfLevel2Threads.incrementAndGet();
                            // notific odata ce numarul de thread-uri de nivel 2 crate este egal cu numarul de
                            // produse ale comenzii de care se ocupa thread-ul de nivel 1 parinte
                            int l2t = numberOfLevel2Threads.get();
                            if(l2t == numberOfProducts)
                                lock.notify();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                // nu avansez in citire deoarece se vor ocupa celelalte thread-uri
                break;
            }
        }
        // scot thread-ul din thread pool
        inQueue.decrementAndGet();
    }
}
