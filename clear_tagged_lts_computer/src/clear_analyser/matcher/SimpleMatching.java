package clear_analyser.matcher;

import java.util.Set;

import clear_analyser.graph.GraphNode;
import clear_analyser.graph.LTS;

// TODO: maybe this class can be included in the multipleMatching one

public class SimpleMatching extends MatchingDecision {

    private final GraphNode specNode;
    private final GraphNode badNode;
    private boolean hasMatched = false; // we set this to true after the first execution of
    // the nextAlternative method

    /**
     * @param specNode      node from the Full TLS
     * @param badNode       node from the Counterexample LTS
     * @param conflictNodes set of conflicting nodes for the badNode
     */
    public SimpleMatching(GraphNode specNode, GraphNode badNode, Set<GraphNode> conflictNodes) {
        super(conflictNodes);
        this.specNode = specNode;
        this.badNode = badNode;
        if (badNode.getEquivalentInSpec() != null) {
            throw new RuntimeException("pushing matching decision for a node that already has a " +
                    "match " + badNode.getId() + "==" + badNode.getEquivalentInSpec().getId());
        }
    }

    /**
     * This method trigger the 'coherent' method to evaluate the assignment of the two nodes
     * defined with the constructor.
     *
     * @param specGraph Full LTS graph
     * @param badGraph  Counterexample LTS graph
     * @return true when a coherent match is foudn, false otherwise
     */
    @Override
    public boolean nextAlternative(LTS specGraph, LTS badGraph) {
        if (this.hasMatched) {
            // if the bad node was already matched, there are no other alternatives
            // thus we set as null the 'equivalent' field and we return false
            this.badNode.setEquivalentInSpec(null);
            return false;
        } else {
            // first execution of the method, thus we set hasMatched to true
            this.hasMatched = true;
            // setting to corresponding node Full node as alternative:
            this.badNode.setEquivalentInSpec(this.specNode);
            // checking matching coherence:
            if (this.coherent(this.specNode, this.badNode, specGraph, badGraph)) {
                return true; // good choice, return true
            }
            // the matching was not coherent, so we are not able to provide a matching
            this.badNode.setEquivalentInSpec(null);
            return false;
        }
    }

    @Override
    public GraphNode getBadNode() {
        return this.badNode;
    }

    @Override
    public GraphNode getSpecNode() {
        return this.specNode;
    }

    @Override
    public String toString() {
        return "SimpleMatching [specNode=" + specNode + ", badNode=" + badNode + ", hasMatched=" +
                hasMatched + "]";
    }

}
