package kademlia_simulator;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Main {
    static String infoMessage =
        "kademlia_simulator 1.0 ( William Guglielmo )\n" +
        "Usage: kademlia_simulator m n k [num]\n" +
        "m: the number m of bits of the identifiers of the network, m>0 & m**2>n; & m<=256\n" +
        "n: the number n of nodes that will join the network, n>0;\n" +
        "k: the number k of size of the routing table buckets, k>0;\n" +
        "num: the number of outputs to produce, it is an optional number, the default value is 1;\n" +
        "The results are saved in the \"out\" folder.";

    public static void main(String[] args) throws FileNotFoundException {

        if(args.length < 3 && args.length > 4) {

            System.out.println(infoMessage.toString());

            System.exit(1);
        }

        int m = 0;
        int n = 0;
        int k = 0;

        int num = 1;
        
        try {
            m = Integer.parseInt(args[0]);
            n = Integer.parseInt(args[1]);
            k = Integer.parseInt(args[2]);

            if(args.length > 3)
                num = Integer.parseInt(args[3]);

        } catch(NumberFormatException exception) {
            System.exit(1);
        }

        // m = 256;
        // n = 2000;
        // k = 20;

        for(int i = 0; i < num; i++) {

            long startTime = System.nanoTime();
    
            Coordinator coordinator = null;
            try {
                coordinator = new Coordinator(m, n, k);
            } catch(IllegalArgumentException exception) {
                System.out.println(exception.getMessage());
                System.exit(1);
            }

            coordinator.run();
            String networkGML = coordinator.getNetworkInGML();

            String outputPath = "out/m"+m+"_n"+n+"_k"+k+"__"+num+".gml";
            
            try(PrintWriter out = new PrintWriter(outputPath)) {
                out.println(networkGML);
            }
    
            long endTime = System.nanoTime();
    
            System.out.println("outputPath: " + 
                    ((endTime - startTime) / 1000000000) + " seconds");
            
        }
    }
}