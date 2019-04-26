package kademlia_simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Network
 */
public final class Network {

    /**
     * The list of nodes,
     * used to extract a random node in constant time.
     */
    private final List<Node> nodeList;

    /**
     * The map to the nodes that joined this network,
     * used to get nodes in constant time from their identifier.
     */
    private final Set<Node> nodeSet;

    private final Random random;


    /**
     * Constructs a Network.
     */
    public Network(Random random) {
        this.nodeList = new ArrayList<>();
        this.nodeSet = new HashSet<>();
        this.random = random;
    }


    private boolean addNode(Node node) {
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
     * @param node  node whose is joining to this network
     * @return {@code false} if there is no nodes to join
     * @throws NullPointerException if the specified node is null
     */
    public boolean join(Node node) {
        if(node == null) throw new NullPointerException();

        return this.addNode(node);
    }


    public boolean join(Node node, Set<Identifier> identifiersToFind) {
        if(node == null) throw new NullPointerException();

        if(this.contains(node))
            return false;

        Node bootstrapNode = this.getRandomNode();

        this.addNode(node);


        for(Identifier identifier : identifiersToFind) {
            node.lookup(bootstrapNode, identifier);
        }

        return true;
    }


    /**
     *
     * @return the number of nodes in this list
     */
    public int size() {
        return this.nodeList.size();
    }


    /**
     * 
     * @param node  node whose presence in this network is to be tested
     * @return  {@code true} if this network contains the specified node
     * @throws NullPointerException if the specified node is null
     */
    public boolean contains(Node node) {
        if(node == null) throw new NullPointerException();

        return this.nodeSet.contains(node);
    }


    /**
     * Removes all the nodes from this network.
     */
    public void clear() {
        this.nodeList.clear();
        this.nodeSet.clear();
    }


    /**
     * 
     * @return  a random node in this network,
     *          or {@null null} if this network contains no nodes.
     */
    private Node getRandomNode() {
        if(this.size() == 0) return null;

        return this.nodeList.get((int) (this.random.nextDouble() * this.size()));
    }


    public List<Node> getNodes() {
        return new ArrayList<>(this.nodeList);
    }


    public String toGML() {
        List<Node> nodes = this.getNodes();
        Map<Node, Integer> mapID = new HashMap<>();

        StringBuilder sb = new StringBuilder();
        sb.append("graph\n");
        sb.append("[\n");

        for(int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);

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


    @Override
    public String toString() {
        return this.toGML();
    }
}