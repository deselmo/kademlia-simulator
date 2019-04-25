package kademplia_simulator;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;


/**
 * Coordinator class represnts a a centralized coordinator which manages
 * virtual nodes of a Kademlia network.
 */
public final class Coordinator implements Runnable {

    private static final double percentageOfNodesPerBucket = 0.1;
    private static final double MAX_ATTEMPTS = 1000000;

    /**
     * number of nodes that will join the network
     */
    private final int m;

    /**
     * number of bits of the identifiers of the network
     */
    private final int n;

    /**
     * number of size of the routing table buckets
     */
    private final int k;


    /**
     * The vitual Kademlianetwork.
     */
    private final Network network;

    /**
     * The random byte arrays generator.
     */
    public final Random random;


    /**
     * Constructs a Coordinator.
     * 
     * @param m  the number m of bits of the identifiers of the network
     * @param n  the number n of nodes that will join the network
     * @param k  the number k of size of the routing table buckets
     */
    public Coordinator(final int m, final int n, final int k) {
        if(!(n > 0 && m > 0 && k > 0))
            throw new IllegalArgumentException("n, m, k must be bigger than 0.");

        if(m > 256)
            throw new IllegalArgumentException("m must not be bigger than 256.");

        if(!(Math.pow(2, m) >= n))
            throw new IllegalArgumentException("2**m must be bigger than or equal to n.");

        this.m = m;
        this.n = n;
        this.k = k;

        this.random = new Random();
        this.network = new Network(this.random);
    }


    @Override
    public void run() {
        initialization_phase();
        routing_table_construction_phase();
    }


    private void initialization_phase() {
        this.network.clear();

        this.network.join(this.newRandomNode());
    }


    private void routing_table_construction_phase() {
        int count = 0;

        while(this.network.size() != this.n) {

            Node randomNode = this.newRandomNode();
            
            Set<Identifier> pairedRandomIdentifier =
                generatePairedRandomIdentifier(randomNode);

            boolean joined = this.network.join(randomNode, pairedRandomIdentifier);


            // Using and truncating the sha, there is no guarantee that all
            // possible values for identifiers will be generated;
            // after MAX_ATTEMPTS fails a RuntimeException is throw.
            if(!joined) {
                count++;
                if(count > MAX_ATTEMPTS) {
                    throw new RuntimeException("Too many attempts to "+
                        "generate the network, try increasing m");
                }
            }
        }
    }


    private Node newRandomNode() {
        Identifier identifier = new Identifier(m, this.random);

        return new Node(identifier, m, k);
    }


    private Set<Identifier> generatePairedRandomIdentifier(Node node) {
        Identifier nodeIdentifier = node.getIdentifier();

        Set<Identifier> identifiers = new HashSet<>();

        for(int i = 0; i < this.m; i++) {

            int identifiersToGenerate = 
                Math.max(1, (int) (this.k * percentageOfNodesPerBucket));

            // try to generate a fixed percentage of identifiers per bucket

            for(int j = 0; j < identifiersToGenerate; j++) {
                identifiers.add(nodeIdentifier.randomInBucket(i, this.random));
            }
        }

        return identifiers;
    }


    public String getNetworkInGML() {
        return network.toGML();
    }
}
