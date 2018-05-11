package clear_analyser.sccfinder;

import clear_analyser.graph.GraphNode;
import clear_analyser.graph.LTS;

import java.util.*;

/**
 * Created by gbarbon.
 */
public abstract class AbstractTarjan {
    int index; // used during the algorithm iteration
    Stack<GraphNode> sccStack; // SCCSet stack
    Map<GraphNode, Integer> indexMap; // map instead of index in each node
    Map<GraphNode, Integer> lowLinkMap;
    LTS lts;
    String action;

    AbstractTarjan(LTS lts) {
        index = 0;
        sccStack = new Stack<>();
        indexMap = new HashMap<>();
        lowLinkMap = new HashMap<>();
        this.lts = lts;
    }

    // variant built to avoid SCC containing a given action
    AbstractTarjan(LTS lts, String action) {
        index = 0;
        sccStack = new Stack<>();
        indexMap = new HashMap<>();
        lowLinkMap = new HashMap<>();
        this.lts = lts;
        this.action = action;
    }

    public abstract SCCSet tarjanCaller();



}
