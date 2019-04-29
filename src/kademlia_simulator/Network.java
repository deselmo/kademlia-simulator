package kademlia_simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Network class, this is an auxiliary class for Coordinator.
 * 
 * This class keeps track of all the nodes connected to the network.
 * It contains the logic to join new nodes in the netowrk.
 * With this class is possible in constant time to:
 * - check if a node is already in the network;
 * - get a random node in the network.
 */
public final class Network {

    /**
     * List of nodes joined to this network,
     * used to extract, in constant time, a random node.
     */
    private final List<Node> nodeList;

    /**
     * Set of nodes joined to this network,
     * used to check, in constant time, if a node is already in the network.
     */
    private final Set<Node> nodeSet;

    private final Random random;


    /**
     * Constructs a Network.
     */
    public Network(final Random random) {
        if(random == null) throw new NullPointerException();

        this.nodeList = new ArrayList<>();
        this.nodeSet = new HashSet<>();
        this.random = random;
    }


    /**
     * Add the specified node in the data structure of this network.
     * 
     * @param node  to add to the network
     * @return  {@code false} if a node with the same identifier is already in 
     *          the network
     */
    private final boolean addNode(final Node node) {
        if(node == null) throw new NullPointerException();

        if (this.contains(node))
            return false;

        this.nodeList.add(node);
        this.nodeSet.add(node);

        return true;
    }


    /**
     * Join the specified node to this network.
     * 
     * @param node  whose is joining to this network
     * @return  {@code false} if a node with the same identifier is already in 
     *          the network
     */
    public final boolean join(final Node node) {
        if(node == null) throw new NullPointerException();

        return this.addNode(node);
    }


    /**
     * Join the specified node to this network, then call the lookup
     * on the specified node for each identifiers in identifiersToFind.
     * 
     * @param node  whose is joining to this network
     * @param identifiersToFind  to search after node joined this network
     * @return  {@code false} if a node with the same identifier is already in 
     *          the network
     */
    public final boolean join(final Node node, final Set<Identifier> identifiersToFind) {
        if(node == null || identifiersToFind == null) throw new NullPointerException();

        if(this.contains(node))
            return false;

        final Node bootstrapNode = this.getRandomNode();

        this.addNode(node);

        for(Identifier identifier : identifiersToFind) {
            node.lookup(bootstrapNode, identifier);
        }

        return true;
    }


    /**
     * Returns the the number of nodes in this network.
     * 
     * @return  the number of nodes in this network
     */
    public final int size() {
        return this.nodeList.size();
    }


    /**
     * Returns {@code true} if this network contains the specified node.
     * 
     * @param node  node whose presence in this network is to be tested
     * @return  {@code true} if this network contains the specified node
     * @throws NullPointerException if the specified node is null
     */
    public final boolean contains(final Node node) {
        if(node == null) throw new NullPointerException();

        return this.nodeSet.contains(node);
    }


    /**
     * Removes all the nodes from this network.
     */
    public final void clear() {
        this.nodeList.clear();
        this.nodeSet.clear();
    }


    /**
     * Returns a random node in this network.
     * 
     * @return  a random node in this network,
     *          or {@null null} if this network contains no nodes.
     */
    private final Node getRandomNode() {
        if(this.size() == 0) return null;

        return this.nodeList.get((int) (this.random.nextDouble() * this.size()));
    }


    /**
     * Returns a list of all the nodes contained to this network.
     * 
     * @return  a list of all the nodes contained to this network
     */
    public final List<Node> getNodes() {
        return new ArrayList<>(this.nodeList);
    }


    /**
     * Returns a String representing this network in GML format.
     * 
     * For reasons of compatibility, an incremental number was used instead of
     * the original ID;
     * the original ID is contained in the comment of the node.
     * 
     * @return  the netowork representation in GML format
     */
    public final String toGML() {
        final List<Node> nodes = this.getNodes();

        // map to save the link between incremental and original ID
        final Map<Node, Integer> mapID = new HashMap<>();

        final StringBuilder sb = new StringBuilder();

        sb.append("graph\n");
        sb.append("[\n");

        // print all the nodes
        for(int i = 0; i < nodes.size(); i++) {
            final Node node = nodes.get(i);

            mapID.put(node, i);

            sb.append("  node\n");
            sb.append("  [\n");

            sb.append("    id ");
            sb.append(i);
            sb.append("\n");

            sb.append("    comment ");
            sb.append("\"");
            sb.append(node.getIdentifier().toString(16));
            sb.append("\"");
            sb.append("\n");

            sb.append("  ]\n");
        }

        // print all the edges
        for(Node node : nodes) {
            for(Node target : node.getKnownNodes()) {
                sb.append("  edge\n");
                sb.append("  [\n");

                sb.append("    source ");
                sb.append(mapID.get(node));
                sb.append("\n");

                sb.append("    target ");
                sb.append(mapID.get(target));
                sb.append("\n");

                sb.append("    comment ");
                sb.append("\"");
                sb.append(node.getIdentifier().toString(16));
                sb.append(" -> ");
                sb.append(target.getIdentifier().toString(16));
                sb.append("\"");
                sb.append("\n");


                sb.append("  ]\n");
            }
        }

        sb.append("]\n");

        return sb.toString();
    }


    /**
     * The GML format is used to represent the network.
     * 
     * @return  a String representation of this object
     */
    @Override
    public final String toString() {
        return this.toGML();
    }
}