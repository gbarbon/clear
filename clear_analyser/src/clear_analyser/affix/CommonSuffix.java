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

public class CommonSuffix extends Suffix implements Comparable<CommonSuffix> {

    public CommonSuffix() {
        super();
    }

    public CommonSuffix(CommonSuffix other) {
        super();
        copy(other);
    }

    /**
     * Create a CommonSuffix from an Affix (copy)
     * @param affix an Affix of String
     */
    public CommonSuffix(Affix<String> affix) {
        super(affix);
    }

    public CommonSuffix(List<String> actions) {
        super(actions);
    }

    /**
     * Generate the common suffix for a node of an LTS, validating the coherency against a
     * sequence of actions.
     * @param node the current node
     * @param actions the sequence of actions representing inevitability
     * @param lts the LTS to which the node belongs
     */
    public CommonSuffix(LTS lts, GraphNode node, List<String> actions) {
        super();
        // collect the intersection of the commonSuffix of the successors nodes
        int i = 0;
        Deque<GraphEdge> edgesToDo = new ArrayDeque<>();
        lts.getOutEdges(node).parallelStream().forEach(edge -> {
            if (lts.getDest(edge).equals(node))
                // self-loop edges (source=dest) must be checked at the end
                edgesToDo.addLast(edge);
            else
                edgesToDo.addFirst(edge);
        });
        while (!edgesToDo.isEmpty()) {
            GraphEdge edge = edgesToDo.removeFirst();
            if (lts.getDest(edge).equals(node))
                // if the dest is the same node, use 'this' as dest prefix, since dest is
                // still null
                evaluateEdge(edge, this, actions, i);
            else
                evaluateEdge(edge, lts.getDest(edge).getCommonSuffix(), actions, i);
            i++;
        }
    }

    /**
     * @deprecated
     * Generate the shortest commonSuffix for an entering node of the SCC
     * @param sccSet
     * @param scc
     * @param source a node that represent an exits of the scc
     */
    @Deprecated
    public CommonSuffix(SCCSet sccSet, SCC scc, GraphNode source, List<String> sequence) {
        super();
        int k = 0;  // depth of the shortest path
        int stopVal = -1;  // stopping value
        Set<GraphNode> visitedNodes = new HashSet<>();
        Queue<Pair<GraphNode, Integer>> nodesTodo = new LinkedList<>();
        DelegateTree<GraphNode, GraphEdge> resultsTree = new DelegateTree<>();
        Set<List<GraphNode>> resultsPaths = new HashSet<>();

        if (source==null)
            throw new IllegalArgumentException();

        // base case: the entering node is also an exiting node
        // commonSuffix is the exiting one
        if (sccSet.getExitingNodes(scc).contains(source)) {
            this.copy(scc.getCommonOutSuffix());
            return;
        }

        // init
        nodesTodo.add(new ImmutablePair<>(source, 0));
        resultsTree.addVertex(source);
        visitedNodes.add(source);

        // finding the shortest paths between source and the destinations
        while(!nodesTodo.isEmpty()) {
            if (stopVal!=-1 && k==stopVal) {
                // stop condition: all the shortest paths have been discovered
                // TODO: verify value of stopVal and K, not sure it is correct
                break;
            }
            Pair<GraphNode, Integer> currPair = nodesTodo.remove();
            GraphNode currNode = currPair.getLeft();
            k = currPair.getRight();
            for (GraphNode succ : sccSet.getLts().getSuccessors(currNode)) {
                if (sccSet.getExitingNodes(scc).contains(succ)) {
                    resultsTree.addChild(sccSet.getLts().findEdge(currNode, succ), currNode, succ);
                    resultsPaths.add(resultsTree.getPath(succ));
                    if (stopVal==-1)
                        stopVal = k+1;  // TODO: verify if this is the correct value
                } else if (visitedNodes.add(succ) && scc.containsNode(succ)) {
                    resultsTree.addChild(sccSet.getLts().findEdge(currNode, succ), currNode, succ);
                    nodesTodo.add(new ImmutablePair<>(succ, k+1));
                }
            }
        }

        // converting the lists of nodes into coherent prefixes
        List<CommonSuffix> results = new ArrayList<>();
        for (List<GraphNode> list : resultsPaths) {
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

            // creating the new common suffix instance, using the commonOutSuffix as base
            CommonSuffix currCommonSuffix = new CommonSuffix();
            currCommonSuffix.copy(scc.getCommonOutSuffix());

            // add only coherent actions to the prefix
            currCommonSuffix.updateCommonSuffix(tmpStringList, sequence, scc.getCommonOutSuffix());
            results.add(currCommonSuffix);
        }

        // retrieving the shortest prefix
        if (!results.isEmpty()) {
            this.copy(results.get(0));
            for (CommonSuffix suffix : results)
                this.retainShortest(suffix);
        } else {
            this.copy(scc.getCommonOutSuffix());
        }
    }

    /**
     * Modifies the suffix by adding the actions of newSeq that are coherent w.r.t sequence
     * @param newSeq a list of new sequences to add
     * @param sequence the inevitable sequence of actions
     * @param suffix the existing suffix
     */
    // TODO: change and use Affix instead of lists of actions
    private void updateCommonSuffix(List<String> newSeq, List<String> sequence, Suffix suffix) {
        for (String action : newSeq) {
            if (checkActionCoherency(action, sequence, suffix))
                suffix.addOnTop(action);
        }
    }

    /**
     * Generate the commonSuffix of the set of exiting edges of an scc of an LTS,
     * validating the coherency against a sequence of actions. Note that the common suffix is
     * stocked in each dest node of the outEdges.
     * @param scc the SCC to which the exiting edges belong
     * @param sccSet the sccSet to which the scc belongs
     * @param actions the sequence of actions representing inevitability
     */
    public static CommonSuffix outEdgesCommonSuffix(SCCSet sccSet, SCC scc, List<String> actions) {
        CommonSuffix commonSuffix = new CommonSuffix();
        int i = 0;
        for (GraphEdge edge : sccSet.getOutEdges(scc)) {
            commonSuffix.evaluateEdge(edge, sccSet.getLts().getDest(edge).getCommonSuffix(),
                    actions, i);
            i++;
        }
        return commonSuffix;
    }

    /**
     * Evaluate the edge and collects the common suffix, used by the constructor.
     * The suffix is provided as parameter since it can be the suffix contained in the edge's
     * dest node or another suffix.
     * @param suffix the passed suffix
     * @param edge the edge under analysis
     * @param actions the sequence of actions representing inevitability
     * @param i the current edge index; if 0 the suffix is copied.
     */
    public void evaluateEdge(GraphEdge edge, CommonSuffix suffix, List<String> actions, int i) {
        CommonSuffix paramSuffix = suffix;
        if (paramSuffix==null)
            paramSuffix = new CommonSuffix();
        if (i==0) {
            copy(paramSuffix);
            STDOut.dbugLog("Sqeuence is now "+ actions);
            STDOut.dbugLog("Edge is "+edge);
            if (checkActionCoherency(edge.getAction(), actions, paramSuffix)) {
                STDOut.dbugLog("adding action "+ edge.getAction());
                addOnTop(edge.getAction());
            }
        } else {
            if (checkActionCoherency(edge.getAction(), actions, paramSuffix)) {
                CommonSuffix tmp = new CommonSuffix();
                tmp.copy(paramSuffix);
                tmp.addOnTop(edge.getAction());
                retainShortest(tmp);
            } else
                retainShortest(paramSuffix);
        }
    }

    /**
     * New function introduced the 29/01/2018 (paper version).
     * @param cs1 a CommonPrefix
     * @param cs2 a CommonPrefix
     * @return the greatest common suffix between the two suffixes
     * TODO: I don't like the use of Collections.reverse, should be better implement a ListIterator
     * with the previous and hasPrevious methods
     */
    public static CommonSuffix greatestCommonSuffix(CommonSuffix cs1, CommonSuffix cs2) {
        List<String> reversedCs1 = cs1.toList();
        List<String> reversedCs2 =  cs2.toList();
        List<String> result = new ArrayList<>();
        Collections.reverse(reversedCs1);
        Collections.reverse(reversedCs2);
        Iterator itr1 = reversedCs1.iterator();
        Iterator itr2 = reversedCs2.iterator();
        while (itr1.hasNext() && itr2.hasNext()) {
            String elem1 = (String) itr1.next();
            String elem2 = (String) itr2.next();
            if (elem1.equals(elem2))
                result.add(elem1);
            else
                break;
        }
        Collections.reverse(result);
        return new CommonSuffix(result);
    }

    /**
     * @param other
     * @return
     */
    @Override
    public int compareTo(CommonSuffix other) {
        // note that this is ascending order
        return Integer.compare(this.size(), other.size());
    }
}
