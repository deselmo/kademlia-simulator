package kademlia_simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Main class, contains the main method;
 * entry point to use the simulator.
 */
public final class Main {

    /**
     * Name of the output directory.
     */
    private static final String outputDirName = "out";


    /**
     * String containing the usage info of the simulator.
     */
    static private final String infoMessage =
        "kademlia_simulator 1.0 ( William Guglielmo )\n" +
        "Usage: kademlia_simulator m n k [num]\n" +
        "m: the number m of bits of the identifiers of the network, m>0 & m**2>n; & m<=256\n" +
        "n: the number n of nodes that will join the network, n>0;\n" +
        "k: the number k of size of the routing table buckets, k>0;\n" +
        "num: the number of outputs to produce, it is an optional number, the default value is 1;\n" +
        "The results are saved in the \"" + Main.outputDirName + "\" folder.";

    /**
     * 
     * @param args  array of argouments, to be used as explained in the infoMessae
     */
    public static void main(final String[] args) {

        if(args.length < 3 || args.length > 4)
            Main.exitWithUsage("Invalid number of arguments.");


        // number of nodes that will join the network
        int m = 0;

        // number of bits of the identifiers of the network
        int n = 0;

        // number of size of the routing table buckets
        int k = 0;

        // number of networks to create
        int num = 1;


        // get parametres from args
        try {
            m = Integer.parseInt(args[0]);
            n = Integer.parseInt(args[1]);
            k = Integer.parseInt(args[2]);

            if(args.length > 3)
                num = Integer.parseInt(args[3]);

        } catch(NumberFormatException exception) {
            Main.exitWithUsage("Invalid number format.");
        }

        if(num < 1)
            Main.exitWithUsage("num must be bigger than 0.");

        // create the out folder, where the results are stored
        (new File(Main.outputDirName)).mkdirs();

        // create num simulation with the specified parameters
        for(int i = 0; i < num; i++) {
    
            // create the coordintor data structure
            Coordinator coordinator = null;
            try {
                coordinator = new Coordinator(m, n, k);
            } catch(IllegalArgumentException exception) {
                Main.exitWithUsage(exception.getMessage());
            }

            // run the coordinator and calculate the time in seconds to create
            // the network
            long elapsedTime = Main.runWithElapsedTimeInSeconds(coordinator);


            String networkGML = coordinator.getNetworkInGML();


            String outputPath = Main.outputDirName + File.separator + 
                    "m" + m + "_n" + n + "_k" + k + "__"+ (i+1) +".gml";

            // write the network in GML format into a file
            try(PrintWriter out = new PrintWriter(outputPath)) {

                out.println(networkGML);

            } catch(FileNotFoundException | SecurityException exception) {

                System.out.println(exception.getMessage());

                continue;
            }

            // print the file where the network is stored
            // and how many seconds were needed to compute the network
            System.out.println(outputPath + ": " + elapsedTime + " seconds");
        }

        System.exit(0);
    }


    /**
     * Prints the usage and close the program with error.
     */
    static private void exitWithUsage() {
        System.out.println(infoMessage);
        System.exit(1);
    }


    /**
     * Prints message, then the usage and close the program with error.
     * 
     * @param message  to print before the program closes
     */
    static private void exitWithUsage(final String message) {
        System.out.println(message + "\n");
        Main.exitWithUsage();
    }

    /**
     * Runs the specified runnable and returns how many seconds were needed to
     * complete the execution.
     * 
     * @param runnable  Runnable to be executed
     * @return  the seconds needed to execute the runnable
     */
    static private long runWithElapsedTimeInSeconds(final Runnable runnable) {
        long startTime = System.nanoTime();
        runnable.run();
        long endTime = System.nanoTime();
    
        return (endTime - startTime) / 1000000000;
    }
}