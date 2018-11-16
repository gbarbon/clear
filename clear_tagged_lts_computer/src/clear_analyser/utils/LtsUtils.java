package clear_analyser.utils;

import edu.uci.ics.jung.graph.util.Pair;
import clear_analyser.graph.GraphEdge;
import clear_analyser.graph.GraphNode;
import clear_analyser.graph.LTS;

import java.io.IOException;
import java.util.*;

/**
 * Contains various utility functions, some coming from old GraphSimp class
 */
public class LtsUtils {

    /**
     * Check if each node of counterexample lts has an equivalent node in the
     * Full LTS (which simulates the Counterexample lts node)
     * @param badGraph the Counterexample LTS
     * @return true if each node has an equivalent in the Full LTS, false otherwise
     */
    public static boolean equivalenceExistence(LTS badGraph) {
        boolean res = true;
        System.out.println("\n*** Equivalence Existence ***");
        for (GraphNode b : badGraph.getVertices()) {
            if (b.getEquivalentInSpec() == null) {
                // FIXME: temporary comment
                //System.err.println("No correspondence for: " + b);
                res = false;
            }
        }
        if (!res) {
            System.out.println("No correspondence for at least one node, the Counterexample LTS " +
                    "does not match the Full LTS.");
        }
        System.out.println("LtsUtils.equivalenceExistence check: " + res);
        return res;
    }

    /**
     * Simple naive verification of the matching (based on  the simulation
     * relation) between nodes stored in the counterexample LTS
     * (getEquivalentInSpec method).
     * Check if each node of the counterexample lts has the equivalent edges
     * (looking also at the detination node) in the corresponding node in the
     * full lts.
     * @param specGraph the Full LTS
     * @param badGraph the Counterexample LTS
     * @return false the matching is not correct, true otherwise
     */
    public static boolean checkMatchingCorrect(LTS specGraph, LTS badGraph) {
        for (GraphNode b : badGraph.getVertices()) {
            GraphNode eqn = b.getEquivalentInSpec();
            for (GraphEdge be : badGraph.getOutEdges(b)) {
                GraphNode bs = badGraph.getDest(be);
                GraphNode eqbs = bs.getEquivalentInSpec();
                boolean found = false;
                for (GraphEdge se : specGraph.findEdgeSet(eqn, eqbs)) {
                    if (se.getAction().equals(be.getAction())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    System.err.println("error matching " + b + " with " + eqn + " because of " + bs);
                    return false;
                }
            }
        }
        return true;
    }
    /**
     * Print the list of node with their equivalent node in the Full LTS
     * @param badGraph the Counterexample LTS
     * @param outputWriter handle output stream, DEPRECATED
     * @throws IOException
     */
    public static void printEquivalenceMap(LTS badGraph, STDOut outputWriter) throws IOException {
        outputWriter.printComplete("\n*** Equivalence Map ***", true, true);
        for (GraphNode b : badGraph.getVertices()) {
            outputWriter.printComplete("[ " + b.getId() + " == "
                    + (b.getEquivalentInSpec() == null ? null : b.getEquivalentInSpec().getId())
                    + " ]", true, true);
        }
    }

    /**
     * Count differences between two lts using the isDiff function between
     * simulated nodes.
     * @deprecated  not used, since isDiff does not take into account
     * simulation on successor nodes
     * @param specGraph the Full LTS
     * @param badGraph the Counterexample LTS
     * @return the number of differences
     */
    @Deprecated
    public static int countDiff(LTS specGraph, LTS badGraph) {
        int total = 0;
        for (GraphNode bn : badGraph.getVertices()) {
            if (isDiff(specGraph, badGraph, bn)) {
                total++;
            }
        }
        return total;
    }

    /**
     * Count differences between two lts using the isDiff function between
     * simulated nodes. Similar to countDiff, collect different nodes and then
     * compute size of the set.
     * @deprecated  not used, since isDiff does not take into account
     * simulation on successor nodes
     * @param specGraph the Full LTS
     * @param badGraph the Counterexample LTS
     * @return the number of differences
     */
    @Deprecated
    public static int countDiffSpecNodes(LTS specGraph, LTS badGraph) {
        Set<GraphNode> diffSpecNodes = new HashSet<>();
        for (GraphNode bn : badGraph.getVertices()) {
            if (isDiff(specGraph, badGraph, bn)) {
                diffSpecNodes.add(bn.getEquivalentInSpec());
            }
        }
        return diffSpecNodes.size();
    }

    /**
     * Count differences between two lts using the isDiff function between
     * simulated nodes. Not used and DEPRECATED (non useful computation inside).
     * @deprecated  not used, since isDiff does not take into account
     * simulation on successor nodes
     * @param specGraph the Full LTS
     * @param badGraph the Counterexample LTS
     * @return the number of differences
     */
    @Deprecated
    public static int countDiffTypes(LTS specGraph, LTS badGraph) {
        Set<Pair<List<String>>> diffTypes = new HashSet<>();
        for (GraphNode bn : badGraph.getVertices()) {
            if (isDiff(specGraph, badGraph, bn)) {
                List<String> lb = new ArrayList<>();
                for (GraphEdge e : badGraph.getOutEdges(bn)) {
                    lb.add(e.getAction());
                }
                Collections.sort(lb);
                List<String> ls = new ArrayList<>();
                for (GraphEdge e : specGraph.getOutEdges(bn.getEquivalentInSpec())) {
                    ls.add(e.getAction());
                }
                Collections.sort(ls);
                diffTypes.add(new Pair<>(ls, lb));
                // It is not useful to use Pair and List of String
                // if after we only compute the size of the Set!!!
            }
        }
        return diffTypes.size();
    }
    /**
     * Compute the difference between a node of the Counterexample LTS and the
     * the node in the Full LTS which simulates it (through getEquivalentInSpec).
     * @deprecated  it does not take into account simulation on successor nodes
     * @param specGraph the Full LTS
     * @param badGraph the Counterexample LTS
     * @param bn a node of the Counterexample LTS.
     * @return true if the number of exiting transitions is different
     * OR if the number of successors nodes is less than the one in the good
     */
    @Deprecated
    private static boolean isDiff(LTS specGraph, LTS badGraph, GraphNode bn) {
        return badGraph.getOutEdges(bn).size() != specGraph.getOutEdges(bn.getEquivalentInSpec()).size()
                || badGraph.getSuccessorCount(bn) < specGraph.getSuccessorCount(bn.getEquivalentInSpec());
    }

    /**
     * Comparison function between actions.
     * Only search if nodes have same labels (actions) in exiting transitions.
     * Superseded by function outTransComparison.
     * @deprecated  it does not take into account simulation on successor nodes
     * @param specGraph Full LTS
     * @param specNode a node in the Full TLS
     * @param badGraph Counterexample LTS
     * @param badNode a node in the Counterexample LTS
     * @return true if the two nodes have the same exiting actions labels, false otherwise
     */
    @Deprecated
    private static boolean outActionsComparison(LTS specGraph, GraphNode specNode,
                                                LTS badGraph, GraphNode badNode) {
        Set<String> badActions = new HashSet<>();
        Set<String> specActions = new HashSet<>();
        for (GraphEdge e : badGraph.getOutEdges(badNode))
            badActions.add(e.getAction());
        for (GraphEdge e : specGraph.getOutEdges(specNode))
            specActions.add(e.getAction());
        return badActions.equals(specActions);
    }

    /**
     * Old find diff function, search for simulated nodes that are different
     * between a full lts and the counterexample lts.
     * Difference is in terms of number of number of exiting nodes and
     * differences in exiting actions, but does not exploit the simulation on
     * the successor nodes.
     * @deprecated  it does not take into account simulation on successor nodes
     * @param specGraph the Full lts
     * @param badGraph the counterexample lts
     * @param outputWriter the output flow instance (DEPRECATED)
     * @throws Exception
     * @return the number of different nodes
     */
    @Deprecated
    public static int OLDfindDiff(LTS specGraph, LTS badGraph, STDOut outputWriter) throws Exception {
        int total = 0;
        outputWriter.printComplete("\n*** Numeric comparison ***", true, true);
        for (GraphNode bn : badGraph.getVertices()) {
            if (bn.getEquivalentInSpec() == null) {
                outputWriter.printComplete("no matching node for " + bn, true, true);
            } else if (isDiff(specGraph, badGraph, bn)) {
                outputWriter.printComplete(bn.printCorrespondence(), true, true);
                total++;
            }
        }
        outputWriter.printComplete("Number of differences: " + total, true, true);
        total = 0;
        outputWriter.printComplete("\n*** Action name comparison ***", true, true);
        for (GraphNode bn : badGraph.getVertices()) {
            if (bn.getEquivalentInSpec() == null) {
                outputWriter.printComplete("no matching node for " + bn, true, true);
            } else if (!outActionsComparison(specGraph, bn.getEquivalentInSpec(), badGraph, bn)) {
                bn.setAsFrontier();  // becomes a node in frontier
                // if the exiting transitions are different (Test: the number of
                // successors nodes not considered)
                String msg = bn.printCorrespondence();
                msg += "OutEdges: ";
                for (GraphEdge e : badGraph.getOutEdges(bn)) {
                    msg = msg + e.getAction() + ", ";
                }
                msg += "  NodeInSpec OutEdges: ";
                for (GraphEdge e : specGraph.getOutEdges(bn.getEquivalentInSpec())) {
                    msg = msg + e.getAction() + ", ";
                }
                outputWriter.printComplete(msg, true, true);
                // the next line triggers the writing on the nb.log file
                // used for searching neighbourhoods
                // with the python written neighbourhood finder
                outputWriter.printNb(msg, false);
                total++;
            }
        }
        outputWriter.printComplete("Number of differences: " + total, true, true);
        outputWriter.printShort(total + "", false);
        return total;
    }

    /**
     * Print in a string differences between full LTS and Counterexample LTS
     * @param specGraph the full LTS
     * @param badGraph the Counterexample LTS
     * @return the string with the differences
     */
    public String printDifferences(LTS specGraph, LTS badGraph) {
        String msg,str;
        str="*** NEW EDGE COMPARISON ***\n";
        for (GraphNode bn : badGraph.getVertices()) {
            // I check also that bn is not the sink, since the sink
            // has no correspondence in the full LTS
            if (bn.getEquivalentInSpec() == null && !bn.equals(badGraph.getSink())) {
                System.out.print("no matching node for " + bn);
            } else if (bn.isFrontier()) {
                msg = bn.printCorrespondence();
                msg += "OutEdges: ";
                for (GraphEdge e : badGraph.getOutEdges(bn)) {
                    // need the following check since I added correct transitions
                    // also in che counterexample lts
                    if (!e.isCorrect())
                        msg = msg + e.getAction() + ", ";
                }
                msg += "  NodeInSpec OutEdges: ";
                for (GraphEdge e : specGraph.getOutEdges(bn.getEquivalentInSpec())) {
                    msg = msg + e.getAction() + ", ";
                }
                str+=msg+"\n";
            }
        }
        return str;
    }
}
