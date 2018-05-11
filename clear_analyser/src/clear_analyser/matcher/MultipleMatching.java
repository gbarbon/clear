package clear_analyser.matcher;

import java.util.Arrays;
import java.util.Set;

import clear_analyser.graph.GraphNode;
import clear_analyser.graph.LTS;

/**
 * This class allows to handle cases in which the badNode can correspond to multiple nodes in the
 * Full LTS. The first matching that is coherent is chosen.
 */
public class MultipleMatching extends MatchingDecision {

    private final GraphNode[] specNodes;
    private final GraphNode badNode;
    private int matchPos = 0;  // position of the node choosen for
    private boolean firstExec = true;

    /**
     *
     * @param specNodes nodes from the Full LTS
     * @param badNode node from the Counterexample LTS
     * @param conflictNodes set of conflicting nodes for the badNode
     */
    public MultipleMatching(GraphNode[] specNodes, GraphNode badNode,
                            Set<GraphNode> conflictNodes) {
        super(conflictNodes);
        //System.err.println("multiple matching");
        this.specNodes = specNodes;
        this.badNode = badNode;
    }

    /**
     * This method trigger the 'coherent' method to evaluate the assignment of the two nodes
     * defined with the constructor.
     *
     * @param specGraph Full LTS
     * @param badGraph Counterexample LTS
     * @return true when a coherent match is foudn, false otherwise
     */
    @Override
    public boolean nextAlternative(LTS specGraph, LTS badGraph) {
        if (!this.firstExec) {
            this.matchPos++; // increase to the next alternative
        }
        this.firstExec = false;
        // looping over possible options among specNodes
        for (; this.matchPos < this.specNodes.length; this.matchPos++) {
            this.badNode.setEquivalentInSpec(this.specNodes[this.matchPos]);
            if (this.coherent(this.specNodes[this.matchPos], this.badNode, specGraph, badGraph)) {
                // if the matching is coherent, we will keep the current specNode and return true
                return true;
            }
        }
        // we tried all the possible alternative in the specNodes, no coherent match exists
        this.badNode.setEquivalentInSpec(null);
        return false;
    }

    @Override
    public GraphNode getBadNode() {
        return this.badNode;
    }

    @Override
    public GraphNode getSpecNode() {
        return this.specNodes[this.matchPos];
    }

    @Override
    public String toString() {
        return "MultipleMatching [specNodes=" + Arrays.toString(specNodes) + ", badNode=" + badNode
                + ", matchPos=" + matchPos + "]";
    }

}
