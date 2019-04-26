package kademlia_simulator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Butket
 */
public final class Bucket {
    /**
     * limit to the dimension of this bucket
     */
    private final long k;

    /**
     * internal representation of the bucket
     */
    private final List<Node> nodes;


    /**
     * Consructs a new Bucket.
     * 
     * @param k  the size of this bucket
     */
    public Bucket(long k) {
        if(k == 0)
            throw new IllegalArgumentException("Bucket size must be bigger than 0");

        this.k = k;
        this.nodes = new LinkedList<>();
    }


    /**
     * Promotes the specified node to the tail of this bucket
     * if the bucket already contains the specified node, moves it to the tail;
     * if the bucket is not full, appends the specified node to the tail;
     * if the bucket is full and the least recently seen node is dead,
     *    appends the specified node to the tail;
     * if the bucket is full and the least recently seen node is still alive,
     *    moves it to the tail.
     * 
     * @param node  the specified node to be inserted in the bucket
     */
    public void insert(Node node) {
        if(node == null) throw new NullPointerException();

        // if the node is present (and if so removed) or the bucked is not full,
        // append the node to the tail of this bucket.
        if(this.nodes.remove(node) || 
           this.nodes.size() < k) {

            this.nodes.add(node);
            
            return;
        }


        // this bucket is full

        // the least recently seen node shold be removed of promoted to the tail
        // anyway it is removed from its current position
        Node leastRecentlySeenNode = this.nodes.remove(0);

        // if th ping is successful, the node is still alive
        if(Node.tryPing(leastRecentlySeenNode)) {

            // add the least recently seen node to the tail of this bucket
            this.nodes.add(leastRecentlySeenNode);

        } else {

            // the least recently seen node doesn't respond to the ping,
            // evict it and
            // add the new node to the tail of this bucket
            this.nodes.add(node);
        }
    }


    public List<Node> getNodes() {
        return new ArrayList<Node>(this.nodes);
    }
}
