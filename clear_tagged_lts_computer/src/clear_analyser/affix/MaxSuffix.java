package clear_analyser.affix;

import clear_analyser.graph.GraphEdge;
import clear_analyser.graph.GraphNode;
import clear_analyser.graph.LTS;
import clear_analyser.sccfinder.SCC;
import clear_analyser.sccfinder.SCCSet;

import java.util.List;

/**
 * Created by gbarbon.
 */
public class MaxSuffix extends Suffix {

    public MaxSuffix() {
        super();
    }

    public MaxSuffix(MaxSuffix other) {
        super();
        copy(other);
    }


    public MaxSuffix(List<String> actions) {
        super(actions);
    }

    /**
     * Generate the maximal suffix for a node.
     * @param node the current node
     * @param lts the LTS to which the node belongs
     * @param actions the sequence of actions representing inevitability
     */
    public MaxSuffix(LTS lts, GraphNode node, List<String> actions) {
        super();

        for (GraphEdge edge : lts.getOutEdges(node)) {
            evaluateEdge(lts, edge, actions);
        }
    }

    /**
     * Generate the maxSuffix for the scc
     * @param scc the SCC to which the entering edges belongs
     * @param sccSet the sccSet to which the scc belongs
     * @param sequence the sequence of actions representing inevitability
     */
    public MaxSuffix(SCCSet sccSet, SCC scc, List<String> sequence) {
        super();
        boolean continues;

        // compute the max suffix of the set of exiting edges of the scc,
        // validating the coherency against a sequence of actions.
        MaxSuffix enteringMaxSuffix = new MaxSuffix();
        for (GraphEdge edge : sccSet.getOutEdges(scc)) {
            enteringMaxSuffix.evaluateEdge(sccSet.getLts(), edge, sequence);
        }

        // computes the maxPrefix of the scc
        do {
            continues = false;
            for (String action : scc.getActions()) {
                if (checkActionCoherency(action, sequence, enteringMaxSuffix)) {
                    enteringMaxSuffix.add(action);
                    continues = true;
                }
            }
        } while (continues);
        this.copy(enteringMaxSuffix);
    }

    /**
     * Returns a backtracking max suffix
     * @param sccSet
     * @param scc
     * @param sequence
     * @return
     */
    public static MaxSuffix btMaxSuffix(SCCSet sccSet, SCC scc, List<String> sequence) {
        boolean continues;
        MaxSuffix enteringMaxSuffix = new MaxSuffix();  // use an empty max suffix
        // computes the maxPrefix of the scc
        do {
            continues = false;
            for (String action : scc.getActions()) {
                if (checkActionCoherency(action, sequence, enteringMaxSuffix)) {
                    enteringMaxSuffix.add(action);
                    continues = true;
                }
            }
        } while (continues);
        return enteringMaxSuffix;
    }

    /**
     * Evaluate the edge and collects the max suffix, used by the constructor
     * @param lts the LTS to which the edge belongs
     * @param edge the edge under analysis
     * @param actions the sequence of actions representing inevitability
     */
    private void evaluateEdge(LTS lts, GraphEdge edge, List<String> actions) {
        GraphNode dest = lts.getDest(edge);
        if (checkActionCoherency(edge.getAction(), actions, dest.getMaxSuffix())) {
            Affix<String> tmp = new Affix<>();
            tmp.copy(dest.getMaxSuffix());
            tmp.addOnTop(edge.getAction());
            retainLongest(tmp);
        } else
            retainLongest(dest.getMaxSuffix());
    }
}
