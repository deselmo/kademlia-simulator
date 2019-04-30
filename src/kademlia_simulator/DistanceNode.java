package kademlia_simulator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * DistanceNode class representing a node with a target and the corresponding distance
 */
public final class DistanceNode implements Comparable<DistanceNode> {

    /**
     * corresponding node
     */
    private final Node node;

    /**
     * corresponfing target
     */
    private final Identifier target;

    /**
     * distance between the identifier of the corresponding node and the
     * corrisponding target
     */
    private final BigInteger distance;


    /**
     * Consructs a new DistanceNode with the specified node and target.
     * 
     * Compute the distance between the identifier of the specified node and 
     * the specified target.
     * 
     * @param node  specified node
     * @param target  specified target
     */
    public DistanceNode(final Node node, final Identifier target) {
        if(node == null || target == null) throw new NullPointerException();

        this.node = node;
        this.target = target;
        this.distance = node.getIdentifier().distance(target);
    }


    /**
     * Generates and returns a list of DistanceNode from the specified list of nodes
     * and the specified target
     * 
     * @param nodes  specified list of Node
     * @param target  specified target
     * @return  a list of corresponding DistanceNode
     */
    public static final List<DistanceNode> listFromNodeToDistanceNode(
                                                final List<Node> nodes,
                                                final Identifier target) {

        if(nodes == null || target == null) throw new NullPointerException();

        final List <DistanceNode> distanceNodes = new ArrayList<>();

        for(Node node : nodes)
            distanceNodes.add(new DistanceNode(node, target));

        return distanceNodes;
    }


    /**
     * Generates and returns a list of Node from the specified list of DistanceNode
     * 
     * @param distanceNodes  specified list of DistanceNode
     * @return  a list of corresponding Node
     */
    public static final List<Node> listFromDistanceNodeToNode(
                                        final List<DistanceNode> distanceNodes) {

        if(distanceNodes == null) throw new NullPointerException();

        final List <Node> nodes = new ArrayList<>();

        for(DistanceNode distanceNode : distanceNodes)
            nodes.add(distanceNode.getNode());

        return nodes;
    }


    /**
     * Returns the distance of this DistanceNode.
     * 
     * @return  the distance of this DistanceNode
     */
    public final BigInteger getDistance() {
        return this.distance;
    }


    /**
     * Returns the corresponding node of this DistanceNode
     * 
     * @return  the corresponding node of this DistanceNode
     */
    public final Node getNode() {
        return this.node;
    }


    /**
     * Returns the corresponding target of this DistanceNode
     * 
     * @return  the corresponding target of this DistanceNode
     */
    public final Identifier getTarget() {
        return this.target;
    }


    /**
     * Implementation of the compareTo method of the Comparable interface,
     * to make the DistanceNode objects sortable.
     * 
     * They are sorted wrt their distance.
     */
    @Override
    public final int compareTo(final DistanceNode other) {
        if(other == null) throw new NullPointerException();

        if(other.target != this.target)
            throw new IllegalArgumentException("the two nodes must have the same target");

        return this.distance.compareTo(other.distance);
    }


    /**
     * Two DistanceNode are considered equal if they have the same node and the
     * same target.
     * 
     * @return  {@true} if this node and obj node have the same node and the
     *          same target
     */
    @Override
    public final boolean equals(final Object obj) {
        if(obj == null || obj.getClass() != this.getClass())
            return false;

        DistanceNode other = (DistanceNode) obj;

        return this.node.equals(other.node) &&
               this.target.equals(other.target);
    }


    /**
     * Two DistanceNode have the same hashCode if they have the same node and the
     * same target.
     * 
     * @return  the hashCode depending on the node and the target of this DistanceNode
     */
    @Override
    public final int hashCode() {
        return Objects.hash(this.node, this.target);
    }
}