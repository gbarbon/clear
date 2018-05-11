package clear_analyser.sccfinder;

import clear_analyser.graph.GraphEdge;
import clear_analyser.graph.GraphNode;
import clear_analyser.graph.LTS;
import clear_analyser.utils.STDOut;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Represents sets of strongly connected components
 */
public class SCCSet {
    Set<SCC> sccs; // contains the result
    private SCC initialSCC; // the scc that contains the initial node

    private GraphEdge fakeInitEdge;
    private GraphNode fakeInitNode;
    private GraphNode authenticInitNode;
    private SCC fakeInitScc;
    private SCC authenticInitScc;

    public LTS getLts() {
        return lts;
    }

    public void setLts(LTS lts) {
        this.lts = lts;
    }

    private LTS lts; // the LTS instance to which the SCCSet belongs

    public SCCSet(LTS lts) {
        sccs = new HashSet<>();
        this.lts = lts;
    }

    public void setSCCs(SCCSet sccSet) {
        sccs = sccSet.getSCCs();
    }

    public void addAll(SCCSet sccSet){
        this.sccs.addAll(sccSet.getSCCs());
    }

    /**
     * Add an scc to the sccs set and set the initialSCC value if the scc is the initial one
     * @param scc an scc to add to this
     */
    public void add(SCC scc){
        this.sccs.add(scc);
        //System.out.println("Adding scc");
        //if (scc.isInitialSCC())
            //System.out.println("Been called");
            //setInitialSCC(scc);
    }

    /**
     *
     * @return the list of strongly connected components
     */
    public Set<SCC> getSCCs() {
        return sccs;
    }

    /**
     *
     * @return number of detected scc
     */
    public int sccNumber() {
        return sccs.size();
    }

    /**
     * Launche the collection of edges (and actions) in every scc
     */
    public void collectEdgesInScc() {
        STDOut.dbugLog("Starting collectEdgesInScc");
        sccs.parallelStream().forEach(e -> collectEdgesActions(e));
    }

    /**
     * Collect for each scc the edges that exit current scc
     * @deprecated
     */
    public void getOutEdges() {
        //sccs.parallelStream().forEach(e -> e.collectExitingEdges(lts));
    }

    /**
     * Collect for each scc the edges that enter current scc
     * @deprecated
     */
    public void getInEdges() {
        //sccs.parallelStream().forEach(e -> e.collectIncomingEdges(lts));
    }

    /**
     *
     * @return number of nodes of the biggest SCC
     */
    public int biggestSccSize() {
        int tmpLength, length = 0;
        for (SCC singleComp : sccs) {
            tmpLength = singleComp.size();
            if (tmpLength > length) {
                length = tmpLength;
            }
        }
        return length;
    }

    /**
     *
     * @return the subset of this SCCset that contains only correct scc
     */
    public SCCSet getCorrectSCCSubSet() {
        SCCSet greenSet = new SCCSet(lts);
        sccs.parallelStream().forEach(s -> {
            if (s.isCorrect())
                greenSet.add(s);
        });
        return greenSet;
    }

    /**
     *
     * @param action searched action
     * @return the subset of this SCCset that contains only SCC that match the given action
     */
    public SCCSet getSCCSubSetContainingAction(String action) {
        SCCSet actionSet = new SCCSet(lts);
        sccs.parallelStream().forEach(s -> {
            if (s.containsAction(action))
                actionSet.add(s);
        });
        return actionSet;
    }

    /**
     *
     * @param action searched action
     * @return the subset of this SCCset that DO NOT contain SCC that match the given action
     */
    public SCCSet getSCCSubSetNotContAction(String action) {
        SCCSet actionSet = new SCCSet(lts);
        sccs.parallelStream().forEach(s -> {
            if (!s.containsAction(action))
                actionSet.add(s);
        });
        return actionSet;
    }

    /**
     *
     * @param colIter the color iteration number
     */
    public void setAsIncorrect(int colIter) {
        for (SCC scc : sccs) {
            scc.setAsIncorrect(colIter);
        }
    }

    /**
     * Sets the initial SCC
     */
    public void setInitialSCC() {
        for (SCC scc : sccs) {
            if (scc.getNodes().contains(lts.getInitialNode())) {
                scc.setIsInitialSCC();
                initialSCC = scc;
                break;
            }
        }

        if (initialSCC.size()!=1) {
            // the initial node has an scc with mor than one node
            fakeSccCreator();
        }

    }

    /**
     * Create a fake initial scc
     */
    private void fakeSccCreator() {
        STDOut.dbugLog("Starting fake init");
        // create fake initial node
        fakeInitNode = new GraphNode(-2);
        // -1 is used by the sink (even if the sink
        // is not used in the liveness)
        lts.addVertex(fakeInitNode);
        fakeInitEdge = new GraphEdge("FAKEINITEDGE");
        authenticInitNode = lts.getInitialNode();
        //fakeInitEdge.setNodes(fakeInitNode, authenticInitNode);
        lts.addEdge(fakeInitEdge, fakeInitNode, lts.getInitialNode());
        lts.setInitialNode(fakeInitNode);

        // create fake initial scc
        authenticInitScc = initialSCC;
        fakeInitScc = new SCC();
        fakeInitScc.addNode(fakeInitNode);
        fakeInitScc.setRootNode(fakeInitNode);
        sccs.add(fakeInitScc);
        initialSCC = fakeInitScc;
        STDOut.dbugLog("End fake init");

    }

    public void fakeSccRemover() {
        if (fakeInitScc!=null) {
            STDOut.dbugLog("Starting fake cleaning");
            lts.removeEdge(fakeInitEdge);
            lts.removeVertex(fakeInitNode);
            lts.setInitialNode(authenticInitNode);
            sccs.remove(fakeInitScc);
            initialSCC = authenticInitScc;
            STDOut.dbugLog("End fake cleaning");
        }
    }

    /**
     *
     * @return the SCC that contians the initial node
     */
    public SCC getInitialSCC() {
        if (initialSCC==null) {
            // the SCCset should contain an initial scc
            // but this exception only works if scc of one single node are allowed
            throw new NullPointerException();
        }
        return initialSCC;
    }

    /**
     * Retrieves the SCCs that are successors of the scc passed as parameter.
     * @param scc an scc that belongs to this set
     * @return the successors sccs
     */
    public Set<SCC> getSuccessors(SCC scc) {
        if (!contains(scc))
            return null;
        //Set<SCC> succ = new HashSet<>();
        //getOutEdges(scc).parallelStream().forEach(e -> succ.add(lts.getDest(e).getSCC()));
        //return succ;
        STDOut.dbugLog("getSuccessors return " + getOutEdges(scc).parallelStream()
                .map(e -> e.getAction())
                .collect(Collectors.toList()));
        return getOutEdges(scc).parallelStream()
                .map(e -> lts.getDest(e).getSCC()).collect(Collectors.toSet());
    }

    /**
     * Retrieves the exiting nodes of the scc passed as parameter.
     * @param scc an scc that belongs to this set
     * @return the nodes in the scc that have outEdges
     */
    public Set<GraphNode> getExitingNodes(SCC scc) {
        if (!contains(scc))
            return null;
        Set<GraphNode> exitingNodes = new HashSet<>();
        getOutEdges(scc).parallelStream().forEach(e -> exitingNodes.add(lts.getSource(e)));
        return exitingNodes;
    }

    /**
     * Retrieves the SCCs that are predecessors of the scc passed as parameter.
     * @param scc an scc that belongs to this set
     * @return the successors sccs
     */
    public Set<SCC> getPredecessors(SCC scc) {
        if (!contains(scc))
            return null;
        Set<SCC> preds = new HashSet<>();
        getInEdges(scc).parallelStream().forEach(e -> preds.add(lts.getSource(e).getSCC()));
        return preds;
    }

    /**
     * Retrieves the entering nodes of the scc passed as parameter.
     * @param scc an scc that belongs to this set
     * @return the nodes in the scc that have inEdges
     */
    public Set<GraphNode> getEnteringNodes(SCC scc) {
        if (!contains(scc))
            return null;
        Set<GraphNode> enteringNodes = new HashSet<>();
        getInEdges(scc).parallelStream().forEach(e -> enteringNodes.add(lts.getDest(e)));
        return enteringNodes;
    }

    /**
     *
     * @param scc an scc
     * @return true if this contains scc, false otherwise
     */
    public boolean contains(SCC scc) {
        return sccs.contains(scc);
    }


    /**
     * Retrieves the successors of a GraphNode inside the SCC (successors that does not belongs
     * to the scc passed as parameter are not taken into account)
     * @param scc the scc in which searching for successors
     * @param node the node for which the search for successors is done
     * @return the node successors inside the scc
     */
    public Set<GraphNode> getSuccInsideSCC(SCC scc, GraphNode node) {
        if (!contains(scc))
            throw new IllegalArgumentException("The scc passed as argument does not belongs to " +
                    "this scc set");
        if (!scc.containsNode(node))
            return null;
        Set<GraphNode> nodes = new HashSet<>();
        for (GraphNode succ : lts.getSuccessors(node)) {
            if (scc.containsNode(succ))
                nodes.add(succ);
        }
        return nodes;
    }

    /**
     * @param scc the scc instance whose actions and edges
     */
    public void collectEdgesActions(SCC scc) {
        if (!contains(scc))
            throw new IllegalArgumentException("The scc passed as argument does not belongs to " +
                    "this scc set");
        ArrayList<GraphNode> nodeList = new ArrayList<>();

        // init
        nodeList.addAll(scc.getNodes());
        Set<GraphEdge> edges = new HashSet<>();
        Set<String> actions = new HashSet<>();

        // loading edges list
        for (int i = 0; i < nodeList.size(); i++) {
            for (int j = 0; j < nodeList.size(); j++) {
                Collection<GraphEdge> pairEdges =
                        lts.findEdgeSet(nodeList.get(i), nodeList.get(j));
                edges.addAll(pairEdges);
            }
        }

        // loading actions list
        //if (edges.isEmpty())  // not possible to do that with single node scc
        //    throw new AssertionError();
        edges.stream().forEach(e -> actions.add(e.getAction()));

        scc.setEdges(edges);
        scc.setActions(actions);
    }

    /**
     * Check whether an scc is final (thus does not have exiting nodes).
     * @param scc the scc
     * @return true if scc does not have exiting nodes, false otherwise
     */
    public boolean isFinalScc(SCC scc) {
        if (!contains(scc))
            throw new IllegalArgumentException("The scc passed as argument does not belongs to " +
                    "this scc set");
        boolean isFinalScc = true;
        outerloop:
        for (GraphNode node : scc.getNodes()) {
            for (GraphNode succ : lts.getSuccessors(node)) {
                if (!scc.getNodes().contains(succ)) {
                    isFinalScc = false;
                    break outerloop;
                }
            }
        }
        return isFinalScc;
    }

    /**
     * Collects all the edges that exits from this scc.
     * @param scc the scc
     * @return the set of exiting edges
     */
    public Set<GraphEdge> getOutEdges(SCC scc) {
        if (!contains(scc))
            throw new IllegalArgumentException("The scc passed as argument does not belongs to " +
                    "this scc set");
        Set<GraphEdge> outEdges = new HashSet<>();
        for (GraphNode node : scc.getNodes()) {
            for (GraphEdge outEdge : lts.getOutEdges(node)) {
                GraphNode succ = lts.getDest(outEdge);
                if (!scc.getNodes().contains(succ)) {
                    outEdges.add(outEdge);
                }
            }
        }
        return outEdges;
    }

    /**
     * Collects all the edges that enters this scc.
     * @param scc thee scc
     * @return the set of edges that enters the scc
     */
    public Set<GraphEdge> getInEdges(SCC scc) {
        if (!contains(scc))
            throw new IllegalArgumentException("The scc passed as argument does not belongs to " +
                    "this scc set");
        Set<GraphEdge> inEdges = new HashSet<>();
        for (GraphNode node : scc.getNodes()) {
            for (GraphEdge inEdge : lts.getInEdges(node)) {
                GraphNode pred = lts.getSource(inEdge);
                if (!scc.getNodes().contains(pred)) {
                    inEdges.add(inEdge);
                }
            }
        }
        return inEdges;
    }

    /**
     *
     * @param scc
     * @return all the exiting edges that are unset
     */
    public Set<GraphEdge> getUnsetOutEdges(SCC scc) {
        if (!contains(scc))
            throw new IllegalArgumentException("The scc passed as argument does not belongs to " +
                    "this scc set");
        Set<GraphEdge> exitingEdge = new HashSet<>();
        for (GraphNode node : scc.getNodes()) {
            lts.getOutEdges(node).parallelStream().forEach(edge -> {
                        if (!scc.containsEdge(edge) && edge.isUnset())
                            exitingEdge.add(edge);
            });
        }
        return exitingEdge;
    }

    /**
     *
     * @param scc
     * @return all the exiting edge that are not unset
     */
    public Set<GraphEdge> getNonUnsetOutEdges(SCC scc) {
        if (!contains(scc))
            throw new IllegalArgumentException("The scc passed as argument does not belongs to " +
                    "this scc set");
        Set<GraphEdge> exitingEdge = new HashSet<>();
        for (GraphNode node : scc.getNodes()) {
            lts.getOutEdges(node).parallelStream().forEach(edge -> {
                        if (!scc.containsEdge(edge) && !edge.isUnset())
                            exitingEdge.add(edge);
            });
        }
        return exitingEdge;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + sccs.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass()) {
            return false;
        }
        SCCSet other = (SCCSet) obj;
        return this.sccs.equals(other.getSCCs());
    }

    /**
     *
     * @return string containing scc in the form of list of nodes
     */
    @Override
    public String toString() {
        String output = "";
        for (SCC singleComp: sccs) {
            output+="\n"+singleComp.toString();
        }
        return output;
    }
}
