package kademlia_simulator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/**
 * Node class representing a node in kademlia.
 * 
 * Node objects contain an identifier and a routing table;
 * and it is possible to simulate on them the ping and the findNode RPC.
 */
public final class Node {

    /**
     * Identifier of this node
     */
    private final Identifier identifier;

    /**
     * Routing table contained in this noode
     */
    private final RoutingTable routingTable;

    /**
     * Size of the buckets of this node
     */
    private final int k;

    /**
     * Simulator of system-wide concurrency parameter
     */
    private static final int ALPHA = 5;


    /**
     * Consructs a new Node with the specified identifier and an empty routing table.
     * 
     * m, n must be bigger than 0;
     * m must not be bigger than 256;
     * 
     * @param identifier  of this Node
     * @param m  number of bits of the identifiers of the node
     * @param k  size of the routing table buckets
     */
    public Node(final Identifier identifier, final int m, final int k) {
        if(identifier == null) throw new NullPointerException();

        if(!(m > 0 && k > 0))
            throw new IllegalArgumentException("m, k must be bigger than 0.");

        if(m > 256)
            throw new IllegalArgumentException("m must not be bigger than 256.");

        this.identifier = identifier;
        this.k = k;

        this.routingTable = new RoutingTable(this, m, k);
    }


    /**
     * Returns the identifier of this node.
     * 
     * @return  the identifier of this node
     */
    final public Identifier getIdentifier() {
        return this.identifier;
    }


    /**
     * Returns the nodes contained in the routing table of this node.
     * 
     * @return  the nodes contained in the routing table of this node
     */
    final public List<Node> getKnownNodes() {
        return this.routingTable.getNodes();
    }


    /**
     * Simulates the ping RPC of kademlia.
     * 
     * @throws  TimeoutException  in this simulation it is never throwed
     */
    public final void ping() throws TimeoutException {}


    /**
     * Simulates the findNode RPC of kademlia.
     * 
     * Adds the specified traversed nodes to this node and
     * returns the k-closest nodes of this node to the target identifier.
     * 
     * @param target  identifier for which to search for the k-closest nodes
     * @param traversedNodes  nodes traversed by a lookup to reach this node
     * @return  the k-closest nodes of this node to the target identifier
     * @throws  TimeoutException  in this simulation it is never throwed
     */
    public final Collection<Node> findNode(final Identifier target, final Set<Node> traversedNodes) 
            throws TimeoutException {

        if(target == null) throw new NullPointerException();

        if(traversedNodes != null)
            this.routingTable.insert(traversedNodes);

        return this.routingTable.kClosestNodes(target);
    }


    /**
     * Locate the k-closest nodes to a specified target in the network.
     * 
     * The lookup start from the specified bootstrap node.
     * All the nodes found during the lookup are used to update the routing table
     * of this node.
     * When this procedure send a findNode to a certain node,
     * it also sends all the nodes traversed to find that node.
     * 
     * @param bootstrapNode  first node to query in the lookup
     * @param target  identifier for which we are looking
     * @return  the k-closest nodes to target in the network
     */
    public final List<Node> lookup(final Node bootstrapNode, final Identifier target) {
        if(bootstrapNode == null || target == null)
            throw new NullPointerException();

        // Object containing
        // - the k-closest nodes,
        // - for each node, through which nodes it has been reached
        final KClosestQueue kClosestNodes =
            new KClosestQueue(bootstrapNode, target, this.k, this);

        // set of already queried nodes in this lookup
        final Set<Node> queriedNodes = new HashSet<>();

        // set of already inserted nodes in this lookup
        final Set<Node> insertedNodes = new HashSet<>();

        // set contained the last Node.ALPHA queried nodes
        final Set<Node> newQueriedNodes = new HashSet<>();

        // lastLookup represents that no node closer to the target than
        // closestNode is returned from the latest findNode
        // if it is {@code true}, then all nodes not yet queried should be queried
        boolean lastLookup = false;

        do {
            // get the closest node from kClosestNodes
            final Node closestNode = kClosestNodes.getClosestNode();

            // the key is the queried node and the value is what the query returned
            final Map<Node, Collection<Node>> foundNodes = new HashMap<>();

            // we are interested only on the last queried nodes
            newQueriedNodes.clear();

            for(final Node node : kClosestNodes) {

                // add the node in the queried node
                // if it has already been queried, then ignore this node
                if(!queriedNodes.add(node))
                    continue;


                // call the findNode,
                // if it fails (in case of timeout) ignore this node
                // put the result in foundNodes having as key the current node
                if(!tryFindNode(node, foundNodes, target,
                                kClosestNodes.getTraversedNodes(node)))
                    continue;

                // add the node to the new queried node
                // the node add to this set will be used to the end of this cycle
                newQueriedNodes.add(node);

                // if lastLookup is {@code false} and
                // if Node.ALPHA nodes has been queried, then break the cicle
                // without query all the nodes in the kClosestNodes
                if(!lastLookup && newQueriedNodes.size() > Node.ALPHA)
                    break;
            }

            // update the routing table of this node and the kClosestNodes;
            // to avoid overhead, in this lookup, each node is inserted only
            // once both in the routing table of this node and in kClosestNodes
            for(final Node queriedNode : newQueriedNodes)
                for(final Node foundNode : foundNodes.get(queriedNode))
                    if(insertedNodes.add(foundNode)) {

                        this.routingTable.insert(foundNode);

                        // when querying the remaining nodes, kClosestNodes
                        // should not be updated
                        if(!lastLookup)
                            kClosestNodes.add(foundNode, queriedNode);
                    }

            // the last cycle has been done
            if(lastLookup)
                break;

            // no node closer to the target than closestNode is returned
            if(closestNode.equals(kClosestNodes.getClosestNode()))
                lastLookup = true;

        } while(true);

        // extract the nodes from kClosestNodes
        return kClosestNodes.getNodes();
    }


    /**
     * Static method to execute the findNode RPC on the specified node.
     * If the findNode throws a TimeoutException, this method returns {@code false}
     * 
     * @param node  specified node to try the findNode
     * @param foundNodes  map where nodes returned from the findNode are putted,
     *                    with the specified node as key
     * @param target  to pass to findNode
     * @param traversedNodes  to pass to findNode
     * @return  {@code false} if the findNode throws a TimeoutException
     */
    static public final boolean tryFindNode(
                                    final Node node,
                                    final Map<Node, Collection<Node>> foundNodes,
                                    final Identifier target,
                                    final Set<Node> traversedNodes) {
        try {
            foundNodes.put(node, node.findNode(target, traversedNodes));
        } catch (TimeoutException exception) {
            return false;
        }
        return true;
    }


    /**
     * Static method to execute the ping RPC on the specified node.
     * If the ping throws a TimeoutException, this method returns {@code false}.
     * 
     * @param node  specified node to try the ping
     * @return  {@code false} if the ping throws a TimeoutException
     */
    static public final boolean tryPing(final Node node) {
        try {
            node.ping();
        } catch (TimeoutException exception) {
            return false;
        }
        return true;
    }


    /**
     * Two nodes are considered equal if they have the same identifier.
     * 
     * @return  {@true} if this node and obj node have the same identifier
     */
    @Override
    public final boolean equals(final Object obj) {
        if(obj == null || obj.getClass() != this.getClass())
            return false;

        final Node other = (Node) obj;

        return this.identifier.equals(other.identifier);
    }


    /**
     * Two nodes have the same hashCode if they have the same identifier.
     * 
     * @return  the hashCode of the identifier of this node
     */
    @Override
    public final int hashCode() {
        return this.identifier.hashCode();
    }
}
