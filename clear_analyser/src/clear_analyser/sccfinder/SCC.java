package clear_analyser.sccfinder;

import clear_analyser.affix.CommonPrefix;
import clear_analyser.affix.CommonSuffix;
import clear_analyser.affix.MaxPrefix;
import clear_analyser.affix.MaxSuffix;
import clear_analyser.graph.GraphEdge;
import clear_analyser.graph.GraphNode;
import clear_analyser.graph.LTS;

import java.util.*;

/**
 * Created by gbarbon.
 * Describe a Strongly Connected Component in a graph
 * // FIXME: edges and action collection still not called yet!!
 */
public class SCC {
    private Set<GraphNode> component;  // sets allow only one instance of same node
    private Set<String> actions;  // sets containing actions in the scc
    private Set<GraphEdge> edges;  // edges of the SCC
    private GraphNode root;  // the node that represent the root of the scc

    // The following is used by the new paper version [26-01-2018]
    private int depth;

    // TODO: move the following to the SCCSet class?
    private boolean isInitialScc;  // if the scc contains the initial node, it is the initial scc

    // FIXME: the colorIter may not be updated when not checking that an edge belongs to an SCC
    private int colorIter;
    // TODO: is the following flag really necessary
    private boolean globalColorIsSet;  // flag that states wether global color has been set or not
    private GraphEdge.TransitionType type;

    // following fields are used for liveness analysis
    // ALL DEPRECATED
    private CommonPrefix commonInPrefix; // common prefix of inEdges
    private MaxPrefix maxInPrefix; // max prefix of inEdges
    private CommonSuffix commonOutSuffix; // common prefix of inEdges
    private MaxSuffix maxOutSuffix; // max prefix of inEdges

    //private CommonPrefix commonPrefix;  // the CommonPrefix inside this scc
    private MaxPrefix maxPrefix;  // the MaxPrefix inside this scc
    //private CommonSuffix commonSuffix;  // the CommonSuffix inside this scc
    private MaxSuffix maxSuffix;  // the MaxSuffix inside this scc
    private MaxSuffix btMaxSuffix;

    public SCC() {
        component = new HashSet<>();
        globalColorIsSet = false;
        colorIter = -2;
        isInitialScc = false;
    }

    /**
     * Add node to the component and set this scc as reference in the node object
     * @param node the GraphNode to add
     * @return true if succeeds
     */
    boolean addNode(GraphNode node) {
        node.setSCC(this);
        return component.add(node);
    }

    /**
     * Set the root node
     * @param node the node to be root
     */
    public void setRootNode(GraphNode node) {root = node;}

    /**
     * Returns the root node of this scc
     * @return the root node of this scc
     */
    public GraphNode getRootNode() {return root;}

    /**
     * @param action action to be added to the scc
     * @return true if this scc did not already contain the specified action
     */
    public boolean addAction(String action) {
        return actions.add(action);
    }

    public int getColorIter() {
        return colorIter;
    }

    /**
     * @param action action whose presence in this scc is to be tested
     * @return true if this set contains the specified element
     */
    public boolean containsAction(String action) {
        if (actions==null || actions.isEmpty()) {
            throw new AssertionError();
        }
        return actions.contains(action);
    }

    /**
     *
     * @param node the graph node
     * @return true it node belongs to this scc, false otherwise
     */
    public boolean containsNode(GraphNode node) {
        return  (component.contains(node));
    }

    /**
     *
     * @param edge
     * @return true if edge belongs to this scc, false otherwise
     */
    public boolean containsEdge(GraphEdge edge) {
        if (edges == null || edges.isEmpty())
            // edges should have been collected before launching this method
            throw new AssertionError();
        return (edges.contains(edge));
    }


    /**
     * @return the set of actions in this scc
     */
    public Set<String> getActions() {
        // following part is removed since this is possible whit single node scc
        /*
        if (actions==null || actions.isEmpty()) {
            throw new AssertionError();
        }*/
        return actions;
    }

    public void setActions(Set<String> actions) {
        this.actions = actions;
    }

    public int size() {
        return component.size();
    }

    /**
     * @return the nodes in the SCC
     */
    public Set<GraphNode> getNodes() {
        return component;
    }

    public Set<GraphEdge> getEdges() {
        //if (edges == null || edges.isEmpty())
        //    throw new AssertionError();
        // it is possible to have scc composed of a single node
        // without loops, even if they're not true scc
        return edges;
    }

    public void setEdges(Set<GraphEdge> edges) {
        this.edges = edges;
    }

    //==============================================================================================
    // Color related methods
    //==============================================================================================

    public GraphEdge.TransitionType getType() {
        return type;
    }

    public void setType(GraphEdge.TransitionType type) {
        this.type = type;
    }

    /**
     * Checks color integrity among all the edges in the scc.
     * If true, it also set the SCC global color.
     *
     * @return true if edges has all the same color, false otherwise
     */
    public boolean checkColorIntegrity() {
        if (edges == null || edges.isEmpty())
            throw new AssertionError();
        int i = 0;
        GraphEdge.TransitionType prevCol = GraphEdge.TransitionType.UNSET;
        for (GraphEdge edge : edges) {
            if (i == 0)
                prevCol = edge.getType();
            else if (!prevCol.equals(edge.getType()))
                return false;
            i++;
        }
        type = prevCol;  // setting global color
        globalColorIsSet = true;  // setting flag
        return true;
    }

    /**
     * @return true if edges has all the same colorIter number, false otherwise
     */
    public boolean checkColorIterIntegrity() {
        if (edges == null || edges.isEmpty())
            throw new AssertionError();
        int i = 0;
        int prevColIter = -2;
        for (GraphEdge edge : edges) {
            if (i == 0)
                prevColIter = edge.getColorIter();
            else if (prevColIter != edge.getColorIter())
                return false;
            i++;
        }
        this.colorIter = prevColIter;
        if (colorIter==-2)
            // -2 should not be possible, only used to initialize SCC
            throw  new AssertionError();
        return true;
    }

    /**
     * @return true if coloring is successful, false otherwise
     */
    public boolean setAsCorrect(int colorIter) {
        if (checkColorIntegrity()) {
            edges.parallelStream().forEach(e -> e.setAsCorrect(colorIter));
            this.colorIter = colorIter;
            type = GraphEdge.TransitionType.GREEN;
            globalColorIsSet = true;
            return true;
        }
        return false;
    }

    /**
     * @return true if coloring is successful, false otherwise
     */
    public boolean setAsIncorrect(int colorIter) {
        if (checkColorIntegrity()) {
            edges.parallelStream().forEach(e -> e.setAsIncorrect(colorIter));
            this.colorIter = colorIter;
            type = GraphEdge.TransitionType.RED;
            globalColorIsSet = true;
            return true;
        }
        return false;
    }

    /**
     * @return true if coloring is successful, false otherwise
     */
    public boolean setAsNeutral(int colorIter) {
        if (checkColorIntegrity()) {
            edges.parallelStream().forEach(e -> e.setAsNeutral(colorIter));
            this.colorIter = colorIter;
            type = GraphEdge.TransitionType.BLACK;
            globalColorIsSet = true;
            return true;
        }
        return false;
    }

    /**
     * @return true if coloring is successful, false otherwise
     */
    public boolean setAsUnset(int colorIter) {
        if (checkColorIntegrity()) {
            edges.parallelStream().forEach(e -> e.setAsUnset(colorIter));
            this.colorIter = colorIter;
            type = GraphEdge.TransitionType.UNSET;
            globalColorIsSet = true;
            return true;
        }
        return false;
    }

    /**
     * @deprecated belongs to old implementation (without prefix suffix)
     * @return true if all the edges in this scc are correct, false otherwise
     */
    public boolean isCorrect() {
        if (!globalColorIsSet) {
            return checkColorIntegrity() && type.equals(GraphEdge.TransitionType.GREEN);
        }
        return type.equals(GraphEdge.TransitionType.GREEN);
    }

    /**
     * @return true if all the edges in this scc are incorrect, false otherwise
     */
    public boolean isIncorrect() {
        if (!globalColorIsSet) {
            return checkColorIntegrity() && type.equals(GraphEdge.TransitionType.RED);
        }
        return type.equals(GraphEdge.TransitionType.RED);
    }

    /**
     * @return true if all the edges in this scc are neutral, false otherwise
     */
    public boolean isNeutral() {
        if (!globalColorIsSet) {
            return checkColorIntegrity() && type.equals(GraphEdge.TransitionType.BLACK);
        }
        return type.equals(GraphEdge.TransitionType.BLACK);
    }

    /**
     * @return true if all the edges in this scc are unset, false otherwise
     */
    public boolean isUnset() {
        if (!globalColorIsSet) {
            return checkColorIntegrity() && type.equals(GraphEdge.TransitionType.UNSET);
        }
        return type.equals(GraphEdge.TransitionType.UNSET);
    }

    /**
     * Verifies whether it is possible to color the SCC or not
     * TODO: maybe not correct, please check
     * @param currentIter
     * @return
     */
    public boolean canColor(int currentIter) {
        return checkColorIterIntegrity() && ((colorIter == currentIter) || colorIter==-1);
    }

    /**
     *
     * @param action string containing action to match
     * @return the set of edges of this scc that matches the action
     */
    public Set<GraphEdge> getEdgesMatchingAction(String action) {
        Set<GraphEdge> matchingEdges = new HashSet<>();
         edges.parallelStream().forEach(e -> {
             if (e.getAction().equals(action))
                 matchingEdges.add(e);
         });
        return  matchingEdges;
    }



    //==============================================================================================
    // Affix-related methods
    //==============================================================================================

    /**
     * Used with Affix, but can be used also in other cases
     * @param otherSCC
     * @param otherLTS
     * @return
     */
    public static SCC copy(SCC otherSCC, LTS otherLTS, LTS newLTS) {
        SCC resSCC = new SCC();

        for (GraphNode n : otherSCC.getNodes()) {
            resSCC.addNode(n);
        }

        return resSCC;
    }

    /**
     * @deprecated the common prefix is not available for an scc but only for its exiting nodes
     */
    public CommonPrefix getCommonPrefix() {
        throw new UnsupportedOperationException();
        //return commonPrefix;
    }

    /**
     * @deprecated the common prefix is not available for an scc but only for its exiting nodes
     */
    public void setCommonPrefix(CommonPrefix commonPrefix) {
        throw new UnsupportedOperationException();
        //this.commonPrefix = commonPrefix;
    }

    public MaxPrefix getMaxPrefix() {
        return maxPrefix;
    }

    /**
     * Note that nodes inside the scc that are not exiting node will contain the max prefix of
     * the whole scc
     * @param maxPrefix
     */
    public void setMaxPrefix(MaxPrefix maxPrefix) {
        this.maxPrefix = maxPrefix;
        for (GraphNode node : component) {
            if (node.getMaxPrefix()!=null)
                throw new AssertionError("Nodes of the scc should have null maxPrefix at this " +
                        "time");
            node.setMaxPrefix(maxPrefix);
        }
    }

    /**
     * @deprecated the common suffix is not available for an scc but only for its entering nodes
     */
    public CommonSuffix getCommonSuffix() {
        throw new UnsupportedOperationException();
        //return commonSuffix;
    }

    /**
     * @deprecated the common suffix is not available for an scc but only for its entering nodes
     */
    public void setCommonSuffix(CommonSuffix commonSuffix) {
        throw new UnsupportedOperationException();
        //this.commonSuffix = commonSuffix;
    }


    public MaxSuffix getMaxSuffix() {
        return maxSuffix;
    }

    /**
     * Notes that nodes inside the scc contain the max suffix of the whole scc
     * @param maxSuffix
     */
    public void setMaxSuffix(MaxSuffix maxSuffix) {
        this.maxSuffix = maxSuffix;
        for (GraphNode node : component) {
            if (node.getMaxSuffix()!=null)
                throw new AssertionError("Nodes of the scc should have null maxSuffix at this " +
                        "time");
            node.setMaxSuffix(maxSuffix);
        }
    }

    /**
     * Notes that nodes inside the scc contain the max suffix of the whole scc
     * @param btMaxSuffix
     */
    public void setBTMaxSuffix(MaxSuffix btMaxSuffix) {
        this.btMaxSuffix = btMaxSuffix;
        for (GraphNode node : component) {
            if (node.getBTMaxSuffix()!=null)
                throw new AssertionError("Nodes of the scc should have null btMaxSuffix at this " +
                        "time");
            node.setBTMaxSuffix(btMaxSuffix);
        }
    }

    /**
     * @deprecated not used (and should not be used)
     * @return
     */
    @Deprecated
    public CommonPrefix getCommonInPrefix() {
        return commonInPrefix;
    }

    /**
     * @deprecated not used (and should not be used)
     * @return
     */
    @Deprecated
    public CommonSuffix getCommonOutSuffix() {
        return commonOutSuffix;
    }

    /**
     * Sets the commonOutSuffix and uses it as initial commonSuffix for each node belonging to the
     * scc.
     * ASSUMPTION: note that with this method nodes inside the scc that are not exiting node will
     * contain the incoming common prefix (NOT CORRECT).
     * @deprecated this method is not correct, do not use it
     * @param commonOutSuffix
     */
    @Deprecated
    public void setCommonOutSuffix(CommonSuffix commonOutSuffix) {
        this.commonOutSuffix = commonOutSuffix;
        for (GraphNode node : component) {
            if (node.getCommonSuffix()!=null)
                throw new AssertionError("Nodes of the scc should have null commonSuffix at this " +
                        "time, root node is " + this.getRootNode());
            node.setCommonSuffix(commonOutSuffix);
        }
    }

    /**
     * @deprecated not used (and should not be used)
     * @return
     */
    @Deprecated
    public MaxSuffix getMaxOutSuffix() {
        return maxOutSuffix;
    }

    /**
     * @deprecated not used (and should not be used)
     * @param maxOutSuffix
     */
    @Deprecated
    public void setMaxOutSuffix(MaxSuffix maxOutSuffix) {
        this.maxOutSuffix = maxOutSuffix;
    }


    /**
     * Sets the commonInPrefix and uses it as initial commonPrefix for each node belonging to the
     * scc.
     * ASSUMPTION: note that nodes inside the scc that are not exiting node will contain the
     * incoming common prefix
     * @deprecated this method is not correct, do not use it
     * @param commonInPrefix
     */
    @Deprecated
    public void setCommonInPrefix(CommonPrefix commonInPrefix) {
        this.commonInPrefix = commonInPrefix;
        for (GraphNode node : component) {
            if (node.getCommonPrefix()!=null)
                throw new AssertionError("Nodes of the scc should have null commonPrefix at this " +
                        "time");
            node.setCommonPrefix(commonInPrefix);
        }
    }

    /**
     * @deprecated not used (and should not be used)
     * @return
     */
    @Deprecated
    public MaxPrefix getMaxInPrefix() {
        return maxInPrefix;
    }

    /**
     * @deprecated not used (and should not be used)
     * @param maxInPrefix
     */
    @Deprecated
    public void setMaxInPrefix(MaxPrefix maxInPrefix) {
        this.maxInPrefix = maxInPrefix;
    }

    /**
     *
     * @return true if this SCC contains the initial node, false otherwise
     */
    public boolean isInitialSCC() { return isInitialScc;}

    /**
     * If the SCC contains the Initial Node, is tagged as initialSCC
     */
    public void setIsInitialSCC() {isInitialScc = true;}

    /**
     *
     */
    public int getDepth() {
        return depth;
    }

    /**
     *
     * @param depth the depth parameter to be set
     */
    public void setDepth(int depth) {
        this.depth = depth;
    }

    /**
     *
     * @param pred the predecessor
     */
    public void  increaseDepthByPredecessor(SCC pred) {
        if (depth<=pred.getDepth()) {
            depth = pred.getDepth() + 1;
        }
    }

    /**
     *
     * @param succ the successor
     */
    public void  increaseSuccessorDepth(SCC succ) {
        if (succ.getDepth()<=depth) {
            succ.setDepth(depth + 1);
        }
    }

    /**
     * Conmparator: compare by depth in ascending order
     */
    public static Comparator<SCC> COMPARE_BY_DEPTH = new Comparator<SCC>() {
        public int compare(SCC one, SCC other) {
            // note that this is ascending order
            return ((Integer) one.getDepth()).compareTo(other.getDepth());
        }
    };

    /*
    */
/**
 * Color all the edges in the scc with the same color
 *//*

    // FIXME: I do not like to use transitiontype here, I would like to use a lambta wtih the
    // FIXME (continues): desired function instead
    public void sccColorer(LTS lts, GraphEdge.TransitionType transitionType) {
        ArrayList<GraphNode> list = new ArrayList<>();
        Collection<GraphEdge> edges;

        list.addAll(component);
        for(int i = 0 ; i < list.size(); i ++){
            for(int j = i+1 ; j < list.size(); j ++){
                edges = lts.findEdgeSet(list.get(i), list.get(j));
                for (GraphEdge edge : edges) {
                    if ((edge.isCorrect() && transitionType.equals(GraphEdge.TransitionType.RED))
                     || (edge.isIncorrect() && transitionType.equals(GraphEdge.TransitionType
                            .GREEN)))
                        edge.setAsNeutral();
                    // TODO: should not be possible if we colour in RED the scc, since the paths
                    // TODO (continues) that lead to the action of the property should not be
                    // TODO (continues) present in the scc
                    else
                        edge.setType(transitionType);
                }
            }
        }
    }

    */

    /**
     * SCC in RED (incorrect), in BLACK (neutral) and UNSET remains the same
     * It only color in BLACK (neutral) SCCs that are GREEN (correct).
     * It is like the sccColorer method, with RED parameter and applied only to GREEN transitions
     *
     * @param
     */
    /*
    public void sccGreenToBlackColorer(LTS lts) {
        ArrayList<GraphNode> list = new ArrayList<>();
        Set<GraphEdge> toColor = new HashSet<>();

        // TODO: if the SCC is colored in the same way, we can check only one transition and
        // TODO (continues): see if it is green or not

        // Init set of GraphEdge to color
        list.addAll(component);
        for(int i = 0 ; i < list.size(); i ++) {
            for (int j = i + 1; j < list.size(); j++) {
                toColor.addAll(lts.findEdgeSet(list.get(i), list.get(j)));
            }
        }

        // coloring edges in green
        // TODO: it is stupid to check each edge...
        toColor.stream().forEach(e -> {
            if (e.isCorrect())
                e.isNeutral();
        });
    }
*/
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + component.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SCC other = (SCC) obj;
        for (GraphNode node : other.component) {
            if (!this.component.contains(node)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        String str = "";
        for (GraphNode node : component) {
            str += node.toStringShort() + " ";
        }
        return str;
    }
}
