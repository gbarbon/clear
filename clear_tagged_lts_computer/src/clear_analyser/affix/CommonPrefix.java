package clear_analyser.affix;

import edu.uci.ics.jung.graph.DelegateTree;
import clear_analyser.graph.GraphEdge;
import clear_analyser.graph.GraphNode;
import clear_analyser.graph.LTS;
import clear_analyser.sccfinder.SCC;
import clear_analyser.sccfinder.SCCSet;
import clear_analyser.utils.STDOut;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * Created by gbarbon.
 */
// TODO : create interface "Common" ? (there are some methods that are common to prefix and suffix)

public class CommonPrefix extends Prefix  implements  Comparable<CommonPrefix> {

    public CommonPrefix() {
        super();
    }

    public CommonPrefix(CommonPrefix other) {
        super();
        copy(other);
    }

    /**
     * Create a CommonPrefix from an Affix (copy
     * @param affix an Affix of String
     */
    public CommonPrefix(Affix<String> affix) {
        super(affix);
    }

    public CommonPrefix(List<String> actions) {
        super(actions);
    }

    /**l
     * Generate the common prefix for a node of an LTS, validating the coherency against a
     * sequence of actions.
     * @param node the current node
     * @param actions the sequence of actions representing inevitability
     * @param lts the LTS to which the node belongs
     */
    public CommonPrefix(LTS lts, GraphNode node, List<String> actions) {
        super();
        // collect the intersection of the commonPrefixes of the predecessor nodes
        // The intersection always get the last
        int i = 0;
        Deque<GraphEdge> edgesToDo = new ArrayDeque<>();
        STDOut.dbugLog("node is " + node.getId());
        lts.getInEdges(node).parallelStream().forEach(edge -> {
            if (lts.getSource(edge).equals(node))
                // self-loop edges (source=dest) must be checked at the end
                edgesToDo.addLast(edge);
            else
                edgesToDo.addFirst(edge);
        });
        while (!edgesToDo.isEmpty()) {
            GraphEdge edge = edgesToDo.removeFirst();
            if (lts.getSource(edge).equals(node))
                // if the source is the same node, use 'this' as source prefix, since source is
                // still null
                evaluateEdge(edge, this, actions, i);
            else
                evaluateEdge(edge, lts.getSource(edge).getCommonPrefix(), actions, i);
            i++;
        }
    }

    /**
     * Generate the shortest commonPrefix for an exiting node of the SCC
     * @param sccSet
     * @param scc
     * @param dest a node that represent an exits of the scc
     */
    public CommonPrefix(SCCSet sccSet, SCC scc, GraphNode dest, List<String> sequence) {
        super();
        int k = 0;  // depth of the shortest path
        int stopVal = -1;  // stopping value
        Set<GraphNode> visitedNodes = new HashSet<>();
        Queue<Pair<GraphNode, Integer>> nodesTodo = new LinkedList<>();
        DelegateTree<GraphNode, GraphEdge> resultsTree = new DelegateTree<>();
        Set<List<GraphNode>> resultsInversedPaths = new HashSet<>();

        if (dest==null)
            throw new IllegalArgumentException();

        // base case: the exiting node is also an entering node
        // commonPrefix is the entering one
        if (sccSet.getEnteringNodes(scc).contains(dest)) {
            this.copy(scc.getCommonInPrefix());
            return;
        }

        // init
        nodesTodo.add(new ImmutablePair<>(dest, 0));
        resultsTree.addVertex(dest);
        visitedNodes.add(dest);

        // finding the shortest paths between dest and the sources
        while(!nodesTodo.isEmpty()) {
            if (stopVal!=-1 && k==stopVal) {
                // stop condition: all the shortest paths have been discovered
                // TODO: verify value of stopVal and K, not sure it is correct
                break;
            }
            Pair<GraphNode, Integer> currPair = nodesTodo.remove();
            GraphNode currNode = currPair.getLeft();
            k = currPair.getRight();
            for (GraphNode pred : sccSet.getLts().getPredecessors(currNode)) {
                if (sccSet.getEnteringNodes(scc).contains(pred)) {
                    resultsTree.addChild(sccSet.getLts().findEdge(pred, currNode), currNode, pred);
                    resultsInversedPaths.add(resultsTree.getPath(pred));
                    if (stopVal==-1)
                        stopVal = k+1;  // TODO: verify if this is the correct value
                } else if (visitedNodes.add(pred) && scc.containsNode(pred)) {
                    resultsTree.addChild(sccSet.getLts().findEdge(pred, currNode), currNode, pred);
                    nodesTodo.add(new ImmutablePair<>(pred, k+1));
                }
            }
        }

        // converting the lists of nodes into coherent prefixes
        List<CommonPrefix> results = new ArrayList<>();
        for (List<GraphNode> list : resultsInversedPaths) {
            List<String> tmpStringList = new LinkedList<>();
            Iterator it = list.iterator();
            if (it.hasNext()) {
                // extract root (since is not needed)
                Object test = it.next();
            }
            while (it.hasNext()) {
                GraphNode node = (GraphNode) it.next();
                tmpStringList.add(resultsTree.getParentEdge(node).getAction());
            }
            // putting the list in the right direction (w.r.t. the lts)
            Collections.reverse(tmpStringList);

            // creating the new common prefix instance, using the inCommonPrefix as base
            CommonPrefix currCommonPrefix = new CommonPrefix();
            currCommonPrefix.copy(scc.getCommonInPrefix());

            // add only coherent actions to the prefix
            currCommonPrefix.updateCommonPrefix(tmpStringList, sequence, scc.getCommonInPrefix());
            results.add(currCommonPrefix);
        }

        // retrieving the shortest prefix
        if (!results.isEmpty()) {
            this.copy(results.get(0));
            for (CommonPrefix prefix : results)
                this.retainShortest(prefix);
        } else {
            this.copy(scc.getCommonInPrefix());
        }
    }

    /**
     * Modifies the prefix by adding the actions of newSeq that are coherent w.r.t sequence
     * @param newSeq a list of new sequences to add
     * @param sequence the inevitable sequence of actions
     * @param prefix the existing prefix
     */
    // TODO: change and use Affix instead of lists of actions
    private void updateCommonPrefix(List<String> newSeq, List<String> sequence, Prefix prefix) {
        for (String action : newSeq) {
            if (checkActionCoherency(action, sequence, prefix))
                prefix.add(action);
        }
    }

    /**
     * Generate the common prefix of the set of entering edges of an scc of an LTS,
     * validating the coherency against a sequence of actions. Note that the common prefix is
     * stocked in each source node of the inEdges.
     * @param scc the SCC to which the entering edges belongs
     * @param sccSet the sccSet to which the scc belongs
     * @param actions the sequence of actions representing inevitability
     */
    public static CommonPrefix inEdgesCommonPrefix(SCCSet sccSet, SCC scc, List<String> actions) {
        CommonPrefix commonPrefix = new CommonPrefix();
        int i = 0;
        for (GraphEdge edge : sccSet.getInEdges(scc)) {
            commonPrefix.evaluateEdge(edge, sccSet.getLts().getSource(edge).getCommonPrefix(),
                    actions, i);
            i++;
        }
        return commonPrefix;
    }

    /**
     * Evaluate the edge and collects the common prefix, used by the constructor.
     * The prefix is provided as parameter since it can be the prefix contained in the edge's
     * source node or another prefix.
     * @param prefix the passed prefix
     * @param edge the edge under analysis
     * @param actions the sequence of actions representing inevitability
     * @param i the current edge index; if 0 the prefix is copied.
     */
    public void evaluateEdge(GraphEdge edge, CommonPrefix prefix, List<String> actions, int i) {
        CommonPrefix paramPrefix = prefix;
        if (paramPrefix==null)
            paramPrefix = new CommonPrefix();
        if (i==0) {
            copy(paramPrefix);
            if (checkActionCoherency(edge.getAction(), actions, paramPrefix))
                add(edge.getAction());
        } else {
            if (checkActionCoherency(edge.getAction(), actions, paramPrefix)) {
                CommonPrefix tmp = new CommonPrefix();
                tmp.copy(paramPrefix);
                tmp.add(edge.getAction());
                retainShortest(tmp);
            } else
                retainShortest(paramPrefix);
        }
    }

    /**
     * New function introduced the 28/01/2018 (paper version, see GCP).
     * @param cp1 a CommonPrefix
     * @param cp2 a CommonPrefix
     * @return the greatest common prefix between the two prefixes
     */
    public static CommonPrefix greatestCommonPrefix(CommonPrefix cp1, CommonPrefix cp2) {
        CommonPrefix result = new CommonPrefix();
        Iterator itr1 = cp1.iterator();
        Iterator itr2 = cp2.iterator();
        while (itr1.hasNext() && itr2.hasNext()) {
            String elem1 = (String) itr1.next();
            String elem2 = (String) itr2.next();
            if (elem1.equals(elem2))
                result.add(elem1);
            else
                break;
        }
        return result;
    }

    /**
     * @param other
     * @return
     */
    @Override
    public int compareTo(CommonPrefix other) {
        // note that this is ascending order
        return Integer.compare(this.size(), other.size());
    }
}
