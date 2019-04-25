package kademplia_simulator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * RoutingTable class represents a Kademlia routing table
 */
public final class RoutingTable {
    private final Node node;
    private final Bucket[] buckets;
    private final int k;

    public RoutingTable(Node node, int m, int k) {
        this.node = node;
        this.k = k;

        this.buckets = new Bucket[m];
        for(int i = 0; i < this.buckets.length; i++) {
            this.buckets[i] = new Bucket(k);
        }
    }


    public void insert(Node node) {
        if(node == null) throw new NullPointerException();
        
        if(this.node == node)
            return;

        this.buckets[this.indexOf(node.getIdentifier())].insert(node);
    }


    public void insert(Collection<Node> nodes) {
        if(node == null) throw new NullPointerException();

        for(Node node : nodes) {
            this.insert(node);
        }
    }


    public List<Node> kClosestNodes(Identifier target) {
        if(node == null) throw new NullPointerException();

        List<Node> closestNodes = new ArrayList<>();
        List<Node> bucketsNodes = new ArrayList<>();

        int targetIndex = this.indexOf(target);

        // if targetIndex == -1, we are the target,
        // this node has not a bucket for itself
        if(targetIndex != -1) {
            bucketsNodes = this.buckets[targetIndex].getNodes();

            RoutingTable.updateClosestNodes(closestNodes, bucketsNodes, target);
        }

        if(closestNodes.size() < this.k) {

            bucketsNodes.clear();
            for(int i = targetIndex - 1; i >=0 ; i--)
                bucketsNodes.addAll(this.buckets[i].getNodes());

            RoutingTable.updateClosestNodes(closestNodes, bucketsNodes, target);
            
            for(int i = targetIndex + 1;
                    i < this.buckets.length && closestNodes.size() < k;
                    i++) {

                bucketsNodes = this.buckets[i].getNodes();

                RoutingTable.updateClosestNodes(closestNodes, bucketsNodes, target);
            }
        }

        if(closestNodes.size() > this.k) {
            closestNodes = closestNodes.subList(0, k);
        }

        return closestNodes;
    }


    private int indexOf(Identifier other) {
        if(node == null) throw new NullPointerException();

        return this.node.getIdentifier().distance(other).bitLength() - 1;
    }


    private static void updateClosestNodes(List<Node> closestNodes,
                                           List<Node> bucketsNodes,
                                           Identifier target) {
        List<DistanceNode> distanceNodes = DistanceNode.listFromNodeToDistanceNode(bucketsNodes, target);
        distanceNodes.sort(null);

        closestNodes.addAll(DistanceNode.listFromDistanceNodeToNode(distanceNodes));
    }


    public List<Node> getNodes() {
        List<Node> nodes = new ArrayList<>();

        for(Bucket bucket : this.buckets) {
            nodes.addAll(bucket.getNodes());
        }

        return nodes;
    }
}