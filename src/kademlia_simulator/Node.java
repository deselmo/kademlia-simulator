package kademlia_simulator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/**
 * Node
 */
public final class Node {

    private final Identifier identifier;
    private final RoutingTable routingTable;
    private final int k;

    private static final int ALPHA = 5;


    public Node(Identifier identifier, int m, int k) {
        this.identifier = identifier;
        this.k = k;

        this.routingTable = new RoutingTable(this, m, k);
    }


    public Identifier getIdentifier() {
        return this.identifier;
    }


    public List<Node> getKnownNodes() {
        return this.routingTable.getNodes();
    }


    public void ping() throws TimeoutException {}

    public Collection<Node> findNode(Identifier target, Set<Node> traversedNodes) 
            throws TimeoutException {

        if(target == null) throw new NullPointerException();

        this.routingTable.insert(traversedNodes);

        return this.routingTable.kClosestNodes(target);
    }


    public void lookup(Node bootstrapNode, Identifier target) {
        if(bootstrapNode == null || target == null)
            throw new NullPointerException();

        KClosestQueue kClosestNodes =
            new KClosestQueue(bootstrapNode, target, this.k, this);

        Set<Node> queriedNodes = new HashSet<>();
        Set<Node> insertedNodes = new HashSet<>();

        boolean lastLookup = false;

        do {
            Node closestNode = kClosestNodes.getClosestNode();

            // the key is the queried node and the value is what the query returned
            Map<Node, Collection<Node>> foundNodes = new HashMap<>();

            for(Node node : kClosestNodes) {
                if(!queriedNodes.add(node))
                    continue;

                if(!tryFindNode(node, foundNodes, target,
                                kClosestNodes.getTraversedNodes(node)))
                    continue;
                
                for(Node queriedNode : foundNodes.keySet())
                    for(Node foundNode : foundNodes.get(queriedNode))
                        if(insertedNodes.add(foundNode))
                            this.routingTable.insert(foundNode);

                if(foundNodes.size() > Node.ALPHA)
                    break;
            }

            for(Node queriedNode : foundNodes.keySet())
                for(Node foundNode : foundNodes.get(queriedNode))
                    kClosestNodes.add(foundNode, queriedNode);

            if(lastLookup)
                break;

            if(closestNode.equals(kClosestNodes.getClosestNode()))
                lastLookup = true;

        } while(true);
    }


    static public boolean tryFindNode(Node node,
                                      Map<Node, Collection<Node>> foundNodes,
                                      Identifier target,
                                      Set<Node> traversedNodes) {
        try {
            foundNodes.put(node, node.findNode(target, traversedNodes));
        } catch (TimeoutException exception) {
            return false;
        }
        return true;
    }


    static public boolean tryPing(Node node) {
        try {
            node.ping();
        } catch (TimeoutException exception) {
            return false;
        }
        return true;
    }


    @Override
    public boolean equals(Object obj) {
        if(obj == null || obj.getClass() != this.getClass())
            return false;

        Node other = (Node) obj;

        return this.identifier.equals(other.identifier);
    }


    @Override
    public int hashCode() {
        return this.identifier.hashCode();
    }
}
