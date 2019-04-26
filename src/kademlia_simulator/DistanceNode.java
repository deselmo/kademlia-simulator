package kademlia_simulator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * DistanceNode
 */
public class DistanceNode implements Comparable<DistanceNode> {

    private final Node node;
    private final Identifier target;
    private final BigInteger distance;

    /**
     * Constructor
     * @param node
     * @param target
     */
    public DistanceNode(Node node, Identifier target) {
        this.node = node;
        this.target = target;
        this.distance = node.getIdentifier().distance(target);
    }


    public static List<DistanceNode> listFromNodeToDistanceNode(List<Node> nodes,
                                                                 Identifier target) {
        List <DistanceNode> distanceNodes = new ArrayList<>();

        for(Node node : nodes)
            distanceNodes.add(new DistanceNode(node, target));

        return distanceNodes;
    }


    public static List<Node> listFromDistanceNodeToNode(List<DistanceNode> distanceNodes) {
        List <Node> nodes = new ArrayList<>();

        for(DistanceNode distanceNode : distanceNodes)
            nodes.add(distanceNode.getNode());

        return nodes;
    }



    /**
     * @return the distance
     */
    public BigInteger getDistance() {
        return this.distance;
    }


    /**
     * @return the target
     */
    public Identifier getTarget() {
        return this.target;
    }


    /**
     * @return the node
     */
    public Node getNode() {
        return this.node;
    }


    @Override
    public int compareTo(DistanceNode other) {
        if(other.target != this.target)
            throw new IllegalArgumentException("the two nodes must have the same target");

        return this.distance.compareTo(other.distance);
    }


    @Override
    public boolean equals(Object obj) {
        if(obj == null || obj.getClass() != this.getClass())
            return false;

        DistanceNode other = (DistanceNode) obj;

        return this.node.equals(other.node) &&
               this.distance.equals(other.distance) &&
               this.target.equals(other.target);
    }


    @Override
    public int hashCode() {
        return Objects.hash(this.node, this.distance, this.target);
    }
}