package clear_analyser.sccfinder;

import clear_analyser.graph.GraphNode;
import clear_analyser.graph.LTS;

import java.util.Stack;

// TODO: not tested yet

/**
 * This version avoid collecting SCCs that contains a certain action
 */
public class TarjanIterativeAction extends AbstractTarjan {
    //Set<String> sccActionsSet; // SCCSet actions set

    /**
     *
     * @param lts graph containing neighbourhoods
     */
    public TarjanIterativeAction(LTS lts, String action) {
        super(lts, action);

    }

    /**
     * Iterative tarjan algorithm caller
     */
    @Override
    public SCCSet tarjanCaller() {
        SCCSet sccSet = new SCCSet(this.lts);
        //TODO: is this loop useful?
        for (GraphNode node : lts.getVertices()) {
            if (!indexMap.containsKey(node)) {
                sccSet.addAll(strongConnect(node, action));
            }
        }
        System.out.println("Number of detected SCC: " + sccSet.getSCCs().size());
        return sccSet;
    }

    /**
     *
     * @return a list of scc
     */
    private SCCSet strongConnect(GraphNode startingNode, String action) {
        TarjanIterElem recursionElem;
        GraphNode node, nextNode;
        boolean pendingIter = false; // used to avoid construction of SCC during a pending iteration
        SCCSet res = new SCCSet(this.lts);
        //sccActionsSet = new HashSet<>();

        // checking existence of action in startingNode
        // TODO: can be removed, since the first node is also checked at the beginning of the while
        /*for (GraphEdge edge : lts.getOutEdges(startingNode)) {
            if (edge.getAction().equals(action)) {
                return res;
            }
        }*/

        // second stack, is used to contain the successors of a node:
        Stack<GraphNode> nodeSuccs = new Stack<>();
        // third stack, is used to represent the status of he algorithm
        Stack<TarjanIterElem> recursionStack = new Stack<>();

        // before first iteration:
        recursionStack.push(new TarjanIterElem(startingNode, null, null,
                StackState.TOPSTATE));

        // loop that mimic AbstractTarjan recursion
        while (!recursionStack.isEmpty()) {
            recursionElem = recursionStack.pop();
            node = recursionElem.currNode;  // current node under analysis

            // This was not correct, since it avoided also some relevant SCC that did not contain
            // any action, so it has been commented
            // checking existence of action in currNode
            /*for (GraphEdge edge : lts.getOutEdges(node)) {
                if (edge.getAction().equals(action)) {
                    //return res;
                }
            }*/

            if (recursionElem.state.equals(StackState.TOPSTATE)) {
                indexMap.put(node, index);
                lowLinkMap.put(node, index);
                index++;
                sccStack.push(node);
                nodeSuccs = new Stack<>(); //it should be already empty
                nodeSuccs.addAll(lts.getSuccessors(node));
            } else if (recursionElem.state.equals(StackState.RETURNEDSTATE)) {
                // resuming a pending iteration
                nodeSuccs = recursionElem.nodeSuccs;
                nextNode = recursionElem.nextNode;
                lowLinkMap.put(node, Math.min(lowLinkMap.get(node), lowLinkMap.get(nextNode)));
            } else {
                // state is not TOPSTATE nor RETURNEDSTATE, so there is a problem
                // TODO: throw exception or assert
            }

            // iterate over node successors
            while (!nodeSuccs.empty()) {
                //if (recursionElem.state.equals(StackState.RETURNEDSTATE)) {
                //lowLinkMap.put(node,Math.min(lowLinkMap.get(node), lowLinkMap.get(nextNode)));
                //} else {
                nextNode = nodeSuccs.pop();
                if (!indexMap.containsKey(nextNode)) {
                    // succ has not been visited:
                    // stock current values and stops iteration, to be resumed after exploration of nextNode
                    // pushing current status to the stack (correspond to recursion)
                    recursionStack.push(new TarjanIterElem(node, nextNode, nodeSuccs, StackState
                            .RETURNEDSTATE));
                    recursionStack.push(new TarjanIterElem(nextNode, null, null, StackState.TOPSTATE));
                    pendingIter = true; // avoid the construction of SCC during current iteration
                    break;
                } else if (sccStack.contains(nextNode)) {
                    // succ is in stack, so it belongs to current SCC
                    lowLinkMap.put(node,
                            Math.min(lowLinkMap.get(node), indexMap.get(nextNode)));
                    // TODO FIXME : ADD HERE THE EDGE TO A SET, SINCE HERE WE HAVE THE LINK
                    // BETWEEN THE TWO NODES
                    //lts.findEdgeSet(node, nextNode).parallelStream().forEach(e -> sccActionsSet
                    //        .add(e.getAction()));
                }
                //}
            }

            // if node is a root node (of an SCC), return the scc
            if (lowLinkMap.get(node).equals(indexMap.get(node)) && !pendingIter) {
                //List<GraphNode> singleComponent = new ArrayList<GraphNode>();
                GraphNode w;
                SCC scc = new SCC();
                do {
                    w = sccStack.pop();
                    scc.addNode(w);
                    if (lts.getInitialNode().equals(w))
                        scc.isInitialSCC();
                } while (!w.equals(node));

                scc.setRootNode(node); // add root node to scc
                res.add(scc); // add scc to result (not hat this version allows 'single node sccs')

                // to avoid SCC of one element, de-comment the following lines :
                /*
                if (scc.size() > 1) {
                    // scc.getNodes().parallelStream().forEach(n -> n.setBelongsToSCC());
                    res.add(scc);
                }
                // the following mod allows to collect SCC of a single element where a lasso exists
                else if (lts.getSuccessors(node).contains(node) && scc.size() == 1) {
                    res.add(scc);
                }*/
            }
            pendingIter = false;
        }

        return res;
    }

    private enum StackState {TOPSTATE, RETURNEDSTATE}

    /**
     * element used to convert recursive tarjan to iterative
     * Used inside the 'outsideStack' stack in the strongConnect method
     */
    private class TarjanIterElem {
        GraphNode currNode;
        GraphNode nextNode;
        Stack<GraphNode> nodeSuccs;
        StackState state;

        TarjanIterElem(GraphNode currNode, GraphNode nextNode,
                       Stack<GraphNode> nodeSuccs, StackState state) {
            this.currNode = currNode;
            this.nextNode = nextNode;
            this.nodeSuccs = nodeSuccs;
            this.state = state;
        }
    }

}
