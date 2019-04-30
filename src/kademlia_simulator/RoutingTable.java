package kademlia_simulator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * RoutingTable class representing a Kademlia routing table
 */
public final class RoutingTable {
    /**
     * node to which this route table belongs
     */
    private final Node node;

    /**
     * buckets contained in this routing table
     */
    private final Bucket[] buckets;

    /**
     * size of the buckets contained in this routing table
     */
    private final int k;

    /**
     * Consructs a new Routing Table belonging to the specified node.
     * 
     * @param node  to which this routing table belongs
     * @param m  number of bits of the indentifiers,
     *           corresponds to the number of buckets in this routing table
     * @param k  size of the buckets contained in this routing table
     */
    public RoutingTable(final Node node, final int m, final int k) {
        this.node = node;
        this.k = k;

        // create m buckets
        this.buckets = new Bucket[m];
        for(int i = 0; i < this.buckets.length; i++) {
            this.buckets[i] = new Bucket(k);
        }
    }


    /**
     * Insert the specified node in this routing table.
     * 
     * The node is inserted in the proper bucket.
     * 
     * @param node  specified to be inserted in this routing table
     */
    public final void insert(final Node node) {
        if(node == null) throw new NullPointerException();
        
        if(this.node == node)
            return;

        // insert the node in the correct 
        this.buckets[this.indexOf(node.getIdentifier())].insert(node);
    }


    /**
     * Insert the specified nodes in this routing table.
     * 
     * @param nodes  specified to be inserted in this routing table
     */
    public final void insert(final Collection<Node> nodes) {
        if(node == null) throw new NullPointerException();

        for(final Node node : nodes)
            this.insert(node);
    }


    /**
     * Returns the nodes contained in all the buckets in this routing table.
     * 
     * @return  the nodes contained in this routing table
     */
    public final List<Node> getNodes() {
        final List<Node> nodes = new ArrayList<>();

        for(final Bucket bucket : this.buckets) {
            nodes.addAll(bucket.getNodes());
        }

        return nodes;
    }


    /**
     * Return the k nodes closest to the target contained in the buckets of this
     * routing table.
     * 
     * @param target  specified target, to find the closest nodes
     * @return  the k nodes closest to target contained in this routing table
     */
    public final List<Node> kClosestNodes(final Identifier target) {
        if(node == null) throw new NullPointerException();

        final List<Node> closestNodes = new ArrayList<>();

        final int targetIndex = this.indexOf(target);

        // the closest nodes are the node in the same bucket of the target;
        // if targetIndex == -1, we are the target and the node to which this 
        // routing table belongs has not a bucket for itself
        if(targetIndex != -1) {

            RoutingTable.updateClosestNodes(
                    closestNodes,
                    this.buckets[targetIndex].getNodes(),
                    target);
        }

        // if the nodes in the bucket of the target less than k
        if(closestNodes.size() < this.k) {

            // first add all the nodes in the buckets above the bucket of the 
            // target (those with index <= to that of the target);
            // these nodes are closest to those below the bucket of the target;
            // but we cannot make assumptions to which of these (above) is closest,
            // so we have to order all these nodes

            // add all the nodes in the buckets above the bucket of the target
            final List<Node> previousBucketsNodes = new ArrayList<>();
            for(int i = targetIndex - 1; i >=0 ; i--)
                previousBucketsNodes.addAll(this.buckets[i].getNodes());

            RoutingTable.updateClosestNodes(
                    closestNodes,
                    previousBucketsNodes, 
                    target);

            
            // if k has not yet been reached, considers the buckets below the
            // bucket of the target;
            // this time we can make assumptions, the more a bucket is close to
            // that of the target, the more the nodes it contains are close to
            // the target compared to those of the buckets with a higher index;
            // add all nodes, one bucket at a time and update the closest nodes
            // until k is reached or all nodes are added.
            for(int i = targetIndex + 1;
                    i < this.buckets.length && closestNodes.size() < k;
                    i++) {

                RoutingTable.updateClosestNodes(
                        closestNodes,
                        this.buckets[i].getNodes(),
                        target);
            }
        }

        // if the closest nodes are more than k, take only the first k nodes
        return (closestNodes.size() <= this.k) ? closestNodes : closestNodes.subList(0, k);
    }


    /**
     * Returns the index of the bucket where the specified identifier is located.
     * 
     * The index depends on how much the prefix is shared by the specified 
     * identifier and that of the node to which this routing table belongs
     * The more it is shared the closer it is to 0.
     * 
     * @param identifier  of which the index is calculated
     * @return  the index of the corresponding bucket
     */
    private final int indexOf(final Identifier identifier) {
        if(node == null) throw new NullPointerException();

        // compute the distance between the identifier of the node to which this
        // routing table belongs and the specified identifier (it is a BigInteger);
        // then get position of the most significat setted bit
        // (in BigInteger it the leftmost bit setted to 1)
        // and reduce it to one (the nearest is in the bucket with index 0)
        return this.node.getIdentifier().distance(identifier).bitLength() - 1;
    }


    /**
     * Order the specified nodes with respect to the distance from target,
     * and then append this nodes to closestNodes
     * 
     * @param closestNodes  list of nodes to which the specified sorted nodes are appended
     * @param nodes  specified nodes to be sorted and appended
     * @param target  identifier based on which to sort
     */
    private static final void updateClosestNodes(final List<Node> closestNodes,
                                                 final List<Node> nodes,
                                                 final Identifier target) {

        // create the list of the corresponding DistanceNode of nodes wrt target
        final List<DistanceNode> distanceNodes =
                DistanceNode.listFromNodeToDistanceNode(nodes, target);

        // sort the distance nodes list
        distanceNodes.sort(null);

        // append the distance nodes sorted list to the closest nodes list
        closestNodes.addAll(DistanceNode.listFromDistanceNodeToNode(distanceNodes));
    }
}