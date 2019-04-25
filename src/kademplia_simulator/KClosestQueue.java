package kademplia_simulator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * KClosestQueue
 */
public final class KClosestQueue implements Iterable<Node> {
    private final int k;
    private final Identifier target;
    private TreeSet<DistanceNode> nodes;
    private Map<Node, Set<Node>> traversedNodes;

    public KClosestQueue(Node bootstrapNode, Identifier target, int k, Node originNode) {
        if(bootstrapNode == null || target == null) 
           throw new NullPointerException();

        if(!(k > 0))
            throw new IllegalArgumentException("k must be bigger than 0");

        this.nodes = new TreeSet<>();
        this.traversedNodes = new HashMap<>();
        this.target = target;
        this.k = k;

        this.nodes.add(new DistanceNode(bootstrapNode, this.target));
        Set<Node> traversedNodes = new HashSet<>();
        traversedNodes.add(originNode);
        this.traversedNodes.put(bootstrapNode, traversedNodes);
    }


    public boolean add(Node node, Node queriedNode) {
        if (nodes == null)
            throw new NullPointerException();

            
        if (!this.nodes.add(new DistanceNode(node, target)))
            return false;
            
        Set<Node> traversedNodes = new HashSet<>(this.getTraversedNodes(queriedNode));
        traversedNodes.add(queriedNode);
        this.traversedNodes.put(node, traversedNodes);

        this.updateNodes();

        return true;
    }


    public Node getClosestNode() {
        return this.nodes.first().getNode();
    }


    public Set<Node> getTraversedNodes(Node queriedNode) {
        if(queriedNode == null) throw new RuntimeException();

        return this.traversedNodes.get(queriedNode);
    }


    private void updateNodes() {
        while(this.nodes.size() > this.k) {
            this.nodes.pollLast().getNode();
        }
    }


    @Override
    public Iterator<Node> iterator() {
        return new InternalIterator();
    }



    private class InternalIterator implements Iterator<Node> {
        private Iterator<DistanceNode> iterator;

        private InternalIterator() {
            this.iterator = nodes.iterator();
        }


        @Override
        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Override
        public Node next() {
            return this.iterator.next().getNode();
        }
    }
}
