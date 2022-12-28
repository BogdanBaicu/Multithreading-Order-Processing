import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Tema2 {
    public static String directory;
    public static int numberOfThreads;
    public static void main(String[] args) throws Exception {
        // verific daca primesc suficiente argumente
        if (args.length < 2)
            System.err.println("Numar insuficient de argumente");

        // retin calea catre director si numarul de thread-uri
        directory = args[0];
        numberOfThreads = Integer.parseInt(args[1]);

        // declarare thread-uri de nivel 1
        Thread[] threads = new Thread[numberOfThreads];

        // thread pool pentru thread-urile de nivel 2
        AtomicInteger inQueue = new AtomicInteger(0);
        ExecutorService threadPool = Executors.newFixedThreadPool(numberOfThreads);

        // declarare fisier de intrare orders.txt
        FileReader ordersFile = new FileReader(directory + "/orders.txt");
        BufferedReader orderReader = new BufferedReader(ordersFile);

        // declarare fisier de iesire orders_out.txt
        FileWriter ordersOutFile = new FileWriter("orders_out.txt");
        BufferedWriter orderWriter = new BufferedWriter(ordersOutFile);

        // declarare fisier de iesire order_products_out.txt
        FileWriter productsOutFile = new FileWriter("order_products_out.txt");
        BufferedWriter productWriter = new BufferedWriter(productsOutFile);

        // pornire thread-uri de nivel 1
        for (int i = 0; i < numberOfThreads; i++) {
            threads[i] = new Thread(new OrderThread(orderReader, orderWriter,
                                                    directory, productWriter,
                                                    inQueue, threadPool));
            threads[i].start();
        }

        // asteptare thread uri de nivel 1
        for (int i = 0; i < numberOfThreads; i++)
            threads[i].join();

        // daca nu mai am thread-uri in thread pool, il inchid
        int left = inQueue.get();
        if (left == 0)
            threadPool.shutdown();

        // inchidere fisiere de scriere si citire
        orderReader.close();
        ordersFile.close();
        orderWriter.close();
        ordersOutFile.close();
        productWriter.close();
        productsOutFile.close();
    }
}