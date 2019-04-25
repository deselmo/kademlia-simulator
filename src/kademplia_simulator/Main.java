package kademplia_simulator;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        int m = 28;
        int n = 1000;
        int k = 20;

        long startTime = System.nanoTime();

        Coordinator coordinator = new Coordinator(m, n, k);
        
        coordinator.run();
        String networkGML = coordinator.getNetworkInGML();
        
        try(PrintWriter out = new PrintWriter("/home/william/Desktop/kademlia/"+m+"_"+n+"_"+k+".gml")) {
            out.println(networkGML);
        }

        long endTime = System.nanoTime();

        System.out.println("finish: " + ((endTime - startTime) / 1000000000) + " seconds");
        
        // System.out.println(networkGML);
    }
}