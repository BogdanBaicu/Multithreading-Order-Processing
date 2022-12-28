import java.io.*;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderThread implements Runnable{
    private final BufferedReader orderReader;
    private final BufferedWriter orderWriter;
    private final String directory;
    private final BufferedWriter productWriter;
    AtomicInteger inQueue;
    ExecutorService threadPool;
    // obiect creat pentru wait() and notify()
    private Object lock;
    public OrderThread(BufferedReader orderReader,  BufferedWriter orderWriter,
                       String directory, BufferedWriter productWriter,
                       AtomicInteger inQueue, ExecutorService threadPool){
        this.orderReader = orderReader;
        this.orderWriter = orderWriter;
        this.directory = directory;
        this.productWriter = productWriter;
        this.inQueue = inQueue;
        this.threadPool = threadPool;
    }

    @Override
    public void run() {
        String line;
        String orderID;
        int numberOfProducts;
        while (true) {
            // daca mai am linii in fisierul de intrare, retin urmatoarea linie intr-un string
            try {
                if (!((line = orderReader.readLine()) != null)) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // extrag id-ul comenzii si numarul de produse din aceasta
            StringTokenizer token = new StringTokenizer(line, ",");
            orderID = token.nextToken();
            numberOfProducts = Integer.parseInt(token.nextToken());

            // comanda nu este de tip Empty Order
            if (numberOfProducts != 0) {
                // initializare fisier de intrare order_products.txt
                FileReader productsFile = null;
                try {
                    productsFile = new FileReader(directory + "/order_products.txt");
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }

                // initializez obiectul pe care thread-urile de nivel 2 create de acest thread il vor folosi pentru
                // citire, obiectul folosit pentru wait() si notify() si setez pe 0 numarul de thread-uri de nivel 2 create
                BufferedReader productReader = new BufferedReader(productsFile);
                lock = new Object();
                AtomicInteger numberOfLevel2Threads = new AtomicInteger(0);

                // adaug in thread pool un numar de thread-uri egal cu numarul de produse din comanda
                for (int i = 0; i < numberOfProducts; i++) {
                    inQueue.incrementAndGet();
                    threadPool.execute(new ProductThread(orderID, numberOfProducts,productReader, productWriter, inQueue, threadPool, lock, numberOfLevel2Threads));
                }

                // astept ca thread-urile de nivel 2 create de acest thread sa termine
                try {
                    synchronized (lock) {
                        lock.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // marchez in fisierul de iesire comanda ca fiind shipped
                try {
                    orderWriter.write(line + ",shipped\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}