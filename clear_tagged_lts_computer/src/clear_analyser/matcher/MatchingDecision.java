package clear_analyser.matcher;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import clear_analyser.graph.GraphEdge;
import clear_analyser.graph.GraphNode;
import clear_analyser.graph.LTS;

/**
 * a matching decision is a match between a node of the Bad LTS and
 */
public abstract class MatchingDecision {

    // final GraphNode origin;
    //
    // public MatchingDecision(GraphNode origin) {
    // super();
    // this.origin = origin;
    // }
    //
    // protected final GraphNode getOrigin() {
    // return origin;
    // }

    final private Set<GraphNode> conflictNodes;  // used only by the first check in coherent method

    /**
     * @param conflictNodes from the BFSStackMatcherMinDiff class
     */
    public MatchingDecision(Set<GraphNode> conflictNodes) {
        super();
        this.conflictNodes = conflictNodes;
    }

    public abstract boolean nextAlternative(LTS specGraph, LTS badGraph);

    public abstract GraphNode getBadNode();

    public abstract GraphNode getSpecNode();

    /**
     * Checking matching coherence, 5 different checks are performed
     *
     * @param specNode  the Full LTS node
     * @param badNode   the Counterexample LTS node
     * @param specGraph the Full LTS
     * @param badGraph  the Counterexample LTS
     * @return true if coherent, false otherwise
     */
    boolean coherent(GraphNode specNode, GraphNode badNode, LTS specGraph, LTS badGraph) {

        // FIRST CHECK: conflicts
        // if badNode has an equivalent node in the Full that correspond to one of the
        // equivalent of one of the conflict nodes, return false
        // TODO: but why?
        if (conflictNodes != null) {
            for (GraphNode conflict : this.conflictNodes) {
                if (conflict.getEquivalentInSpec() != null
                        && badNode.getEquivalentInSpec().equals(conflict.getEquivalentInSpec())) {
                    //System.err.println("match false because conflicting nodes exist.");
                    return false;
                }
            }
        }

        // multimaps of <action, node> for the spec and the bad nodes
        Multimap<String, GraphNode> badOutEdges = HashMultimap.create();
        Multimap<String, GraphNode> specOutEdges = HashMultimap.create();
        for (GraphEdge edge : badGraph.getOutEdges(badNode)) {
            badOutEdges.put(edge.getAction(), badGraph.getDest(edge));
        }
        for (GraphEdge edge : specGraph.getOutEdges(specNode)) {
            specOutEdges.put(edge.getAction(), specGraph.getDest(edge));
        }

        // SECOND CHECK: check that edges are a subset of spec graph
        // if the bad one has more edges with a given key than the full one, then return false
        for (String k : badOutEdges.keySet()) {  // keyset returns distinct (collapses duplicates)
            int badCard = badOutEdges.keys().count(k);
            if (specOutEdges.keys().count(k) < badCard) {
                //System.err.println("match false because bad cardinality for "+ k);
                return false;
            }
        }

        // THIRD CHECK: check more detailed for parallel edges
        // if there exists more than one edge between a badnode and one of its successors, this
        // should be true also for the corresponding specnode and its successors, otherwise false
        // is returned
        for (GraphNode outNode : badGraph.getSuccessors(badNode)) { // looping on successors
            GraphNode eqOutNode = outNode.getEquivalentInSpec();
            if (eqOutNode == null) {  // works only when the equivalent node for the successor is
                // not set
                Collection<GraphEdge> connectingEdges = badGraph.findEdgeSet(badNode, outNode);
                if (connectingEdges.size() > 1) { // works only if there exit more than one edge
                    // between the two nodes
                    Set<GraphNode> potentialMatcheSet = new HashSet<>();
                    boolean first = true;
                    for (GraphEdge e : connectingEdges) { // loops over the edeges between the
                        // badNode and the choosen successor (outNode)
                        if (first) {  // first iteration
                            first = false;
                            potentialMatcheSet.addAll(specOutEdges.get(e.getAction()));
                        } else {
                            // we keep only elements that are also present in the full LTS in the
                            // corresponding node
                            potentialMatcheSet.retainAll(specOutEdges.get(e.getAction()));
                        }
                    }
                    // at the end, if the set is empty, this means that the badnode and the
                    // specnode do not have the same exiting edges
                    if (potentialMatcheSet.isEmpty()) {
                        //System.err.println("match false because bad cardinality for parallel " +
                        //        "edges");
                        return false;
                    }
                }
            }
        }

        // FOURTH CHECK: check if out nodes matched are coherent
        // this check is based only on edge label equivalence
        // It checks whether the edge between two states in the counterexample lts exists between
        // the corresponding nodes in the Full lts, otherwise returns false
        for (GraphEdge outEdge : badGraph.getOutEdges(badNode)) {  // looping on successors
            GraphNode outNode = badGraph.getDest(outEdge);
            GraphNode eqOutNode = outNode.getEquivalentInSpec();
            if (eqOutNode != null) {  // works only if the equivalence is set
                // if there is a out node that has been matched, check
                // label of edge and remove it from to match
                Collection<GraphEdge> potentialEqEdges = specGraph.findEdgeSet(specNode, eqOutNode);
                // System.err.println("DEBUG: "+ potentialEqEdges);
                boolean found = false;
                for (GraphEdge e : potentialEqEdges) {
                    if (e.getAction().equals(outEdge.getAction())) {
                        found = true;
                        break;
                        // stops if there exit an equivalent actions between the two nodes in the
                        // full TLS and the corresponding two in the Counterexample LTS
                        // TODO: it is not checking simulation, but only label equivalence.
                        // TODO (continues): Is this correct?
                    }
                }
                if (!found) {
                    // not coherent, fail immediately
                    /*System.err.println("match false, edge " + badNode.toStringShort() + " - " +
                            outEdge + " - " + outNode.toStringShort() + " does not exist in spec.");
                    System.err.println("Equivalent were " + badNode.getEquivalentInSpec()
                            .toStringShort() + " and " + eqOutNode.toStringShort());
                    System.err.println("But destinations of  " + specNode
                            .toStringShort() + " are " + specGraph.getSuccessors(specNode));*/
                    return false;
                }
            }
        }
        // FIFTH CHECK: check if in nodes matched are coherent
        // Same as the previous check (fourth check), but works with the incoming edges instead
        for (GraphEdge inEdge : badGraph.getInEdges(badNode)) {
            GraphNode inNode = badGraph.getSource(inEdge);
            GraphNode eqInNode = inNode.getEquivalentInSpec();
            if (eqInNode != null) {
                // if there is a out node that has been matched, check
                // label of edge and remove it from to match
                Collection<GraphEdge> potentialEqEdges = specGraph.findEdgeSet(eqInNode, specNode);
                boolean found = false;
                for (GraphEdge e : potentialEqEdges) {
                    if (e.getAction().equals(inEdge.getAction())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    // not coherent, fail immediately
                    //System.err.println("match false because bad match for in node " + inNode);
                    return false;
                }
            }
        }
        return true;  // if the decision pass all 5 checks
    }
}
