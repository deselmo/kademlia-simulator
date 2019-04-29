package kademlia_simulator;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;


/**
 * Coordinator class representing a centralized coordinator which manages
 * virtual nodes of a Kademlia network.
 * A Coordinator object contains no node until it is not run.
 */
public final class Coordinator implements Runnable {

    /**
     * Percentage of node to be generated at random for each bucket in the
     * routing_table_construction_phase.
     */
    private static final double percentageOfNodesPerBucket = 0.1; //10%

    /**
     * Number of nodes that will join the network.
     */
    private final int m;

    /**
     * Number of bits of the identifiers of the network.
     */
    private final int n;

    /**
     * Number of size of the routing table buckets.
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
     * m, n, k must be bigger than 0;
     * m must not be bigger than 256;
     * 2**m must be bigger than or equal to n.
     * 
     * @param m  the number m of bits of the identifiers of the network
     * @param n  the number n of nodes that will join the network
     * @param k  the number k of size of the routing table buckets
     * @throws IllegalArgumentException  if !(m > 0) or
     *                                      !(n > 0) or
     *                                      !(k > 0) or
     *                                      m > 256  or
     *                                      2**m > n
     */
    public Coordinator(final int m, final int n, final int k) {
        if(!(n > 0 && m > 0 && k > 0))
            throw new IllegalArgumentException("m, n, k must be bigger than 0.");

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


    /**
     * Runs the initialization_phase and the routing_table_construction_phase.
     */
    @Override
    public final void run() {
        initialization_phase();
        routing_table_construction_phase();
    }


    /**
     * Clears the network contained by this and adds a bootstrap node to the network.
     */
    private final void initialization_phase() {
        this.network.clear();

        this.network.join(this.newRandomNode());
    }


    /**
     * Generates the remaining nodes to reach n.
     * Each node is generated with a random identifier.
     * For each node is generated a radom set of identifier distribuited on
     * its buckets.
     * Adds all the generated nodes to the networks.
     */
    private final void routing_table_construction_phase() {
        if(this.network.size() < 1)
            throw new RuntimeException(
                "routing_table_construction_phase" + 
                "must to be called after initialization_phase");


        while(this.network.size() != this.n) {

            final Node randomNode = this.newRandomNode();
            
            Set<Identifier> pairedRandomIdentifier =
                generatePairedRandomIdentifier(randomNode);

            this.network.join(randomNode, pairedRandomIdentifier);
        }
    }


    /**
     * Generates a node with a random identifiers in the range of allowed
     * identifiers.
     * 
     * @return  a Node with a random identifier
     */
    private final Node newRandomNode() {
        Identifier identifier = new Identifier(m, this.random);

        return new Node(identifier, m, k);
    }


    /**
     * Generates a set of identifiers uniformly distributed, at random, in the
     * identifiers range paired with the different buckets of the routing
     * tables of node.
     * 
     * For each bucket are generated (this.k * Coordinator.percentageOfNodesPerBucket),
     * duplicates are dropped.
     * At least one ID per bucket is generated.
     * 
     * @param node  to consider to generate paired identifiers
     * @return  a set of random identifiers paired with the buckets of node
     */
    private final Set<Identifier> generatePairedRandomIdentifier(final Node node) {
        if(node == null) throw new NullPointerException();

        final Identifier nodeIdentifier = node.getIdentifier();

        final Set<Identifier> identifiers = new HashSet<>();

        // generate at least one ID per bucket
        final int identifiersToGenerate = 
            Math.max(1, (int) (this.k * Coordinator.percentageOfNodesPerBucket));

        for(int i = 0; i < this.m; i++) {

            // try to generate a fixed percentage of identifiers per bucket
            for(int j = 0; j < identifiersToGenerate; j++) {

                // add a random identifier in the i-th bucket of the routing table of node
                identifiers.add(nodeIdentifier.randomInBucket(i, this.random));
            }
        }

        return identifiers;
    }


    /**
     * Returns the network that this coordinator contains,
     * if this method is called the run method, the returning network is empty.
     * The network returned is a String in the GML format.
     * 
     * @return  a String representing the current network contained by this
     *          coordinator, in the GML format.
     */
    public final String getNetworkInGML() {
        return network.toGML();
    }
}
