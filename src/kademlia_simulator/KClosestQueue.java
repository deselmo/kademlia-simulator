package kademlia_simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * KClosestQueue class representing an sorted list of k node wrt a specified target
 */
public final class KClosestQueue implements Iterable<Node> {

    /**
     * size of this queue
     */
    private final int k;

    /**
     * specified target identifier
     */
    private final Identifier target;

    /**
     * TreeSet representing a sorted queue of different nodes
     */
    private final TreeSet<DistanceNode> nodes;

    /**
     * map that for each node contains through which nodes that node as been reached
     */
    private final Map<Node, Set<Node>> traversedNodes;


    /**
     * Consructs a new KClosestQueue with the specified size, the specified target,
     * and adding in its queue the specified bootstrapNode reachable via the specified
     * originNode.
     * 
     * @param bootstrapNode  first node to add to this queue
     * @param target  specified target, to calculate the distances
     * @param k  specified size of this queue
     * @param originNode  node through which the boostrapNode is reachable
     */
    public KClosestQueue(final Node bootstrapNode, final Identifier target,
                         final int k, final Node originNode) {

        if(bootstrapNode == null || target == null || originNode == null) 
           throw new NullPointerException();

        if(!(k > 0))
            throw new IllegalArgumentException("k must be bigger than 0");

        this.nodes = new TreeSet<>();
        this.traversedNodes = new HashMap<>();
        this.target = target;
        this.k = k;

        // create a DistanceNode for the bootstrapNode
        this.nodes.add(new DistanceNode(bootstrapNode, this.target));

        // create the set of node through the bootstrap node is reachable
        // now it contains only the originNode
        final Set<Node> currentTraversedNodes = new HashSet<>();
        currentTraversedNodes.add(originNode);

        // map the bootstrapNode to its traversedNodes
        this.traversedNodes.put(bootstrapNode, currentTraversedNodes);
    }


    /**
     * Tries to add the specified node to this queue.
     * 
     * If the specified node if closer than at least one node in this queue,
     * the specified node is added in this queue.
     * 
     * Anyway how the specified node is reachable is saved in this object.
     * 
     * @param node  specified node that we are trying to add
     * @param queriedNode  node through which the specified node is reachable
     * @return  {@code false} if the specified node is already in this queue
     */
    public final boolean add(final Node node, final Node queriedNode) {
        if (nodes == null)
            throw new NullPointerException();

        // add the node to the internal TreeSet,
        // if the specified node is already present return {@code false}
        if (!this.nodes.add(new DistanceNode(node, target)))
            return false;


        // compute the traversed, map in this.traversedNode that the specified node
        // is reachable through the queriedNode plus the traversed nodes for queriedNode
        Set<Node> currentTraversedNodes =
                new HashSet<>(this.getTraversedNodes(queriedNode));
        currentTraversedNodes.add(queriedNode);
        this.traversedNodes.put(node, currentTraversedNodes);

        // if the size of this queue is bigger that k, reduce it to k
        this.updateNodes();

        return true;
    }


    /**
     * Returns a list containing the nodes in this queue.
     * The returned list has size smaller than or equal to k.
     * 
     * @return  the nodes contained in this queue
     */
    public final List<Node> getNodes() {
        final List<Node> closestNodes = new ArrayList<>();
        for(final DistanceNode distanceNode : this.nodes)
            closestNodes.add(distanceNode.getNode());

        return closestNodes;
    }


    /**
     * Returns the closest node to the target in this queue.
     * 
     * @return  the closest node to the target in this queue
     */
    public final Node getClosestNode() {
        return this.nodes.first().getNode();
    }


    /**
     * Returns the set of nodes needed to be traversed to reach the specified node.
     * 
     * @param node  the specified node
     * @return  the nodes needed to be traversed to reach the specified node
     */
    public final Set<Node> getTraversedNodes(final Node node) {
        if(node == null) throw new RuntimeException();

        return this.traversedNodes.get(node);
    }


    /**
     * Removes the last nodes of the internal TreeSet until the size of the
     * internal TreeSet is reduced to k.
     */
    private final void updateNodes() {
        while(this.nodes.size() > this.k) {
            this.nodes.pollLast().getNode();
        }
    }


    /**
     * Implementation of the iterator method of the Iterable interface.
     */
    @Override
    public Iterator<Node> iterator() {
        return new InternalIterator();
    }



    /**
     * private class used to covert the iterator of DistanceNode of the internal TreeSet
     * to an iterator of Node.
     */
    private final class InternalIterator implements Iterator<Node> {
        private final Iterator<DistanceNode> iterator;

        private InternalIterator() {
            this.iterator = nodes.iterator();
        }


        @Override
        public final boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Override
        public final Node next() {
            return this.iterator.next().getNode();
        }
    }
}
