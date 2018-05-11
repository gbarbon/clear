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
public class MaxPrefix extends Prefix {

    public MaxPrefix() {
        super();
    }

    public MaxPrefix(MaxPrefix other) {
        super();
        copy(other);
    }

    public MaxPrefix(List<String> actions) {
        super(actions);
    }

    /**
     * Generate the maximal prefix for a node of an LTS.
     * @param node the current node
     * @param lts the LTS to which the node belongs
     * @param actions the sequence of actions representing inevitability
     */
    public MaxPrefix(LTS lts, GraphNode node, List<String> actions) {
        super();

        for (GraphEdge edge : lts.getInEdges(node)) {
            evaluateEdge(lts, edge, actions);
        }
    }

    /**
     * Generate the maxPrefix for the scc
     * @param scc the SCC to which the entering edges belongs
     * @param sccSet the sccSet to which the scc belongs
     * @param sequence the sequence of actions representing inevitability
     */
    public MaxPrefix(SCCSet sccSet, SCC scc, List<String> sequence) {
        super();
        boolean continues;

        // compute the max prefix of the set of entering edges of the scc,
        // validating the coherency against a sequence of actions.
        MaxPrefix enteringMaxPrefix = new MaxPrefix();
        for (GraphEdge edge : sccSet.getInEdges(scc)) {
            enteringMaxPrefix.evaluateEdge(sccSet.getLts(), edge, sequence);
        }

        // computes the maxPrefix of the scc
        do {
            continues = false;
            for (String action : scc.getActions()) {
                if (checkActionCoherency(action, sequence, enteringMaxPrefix)) {
                    enteringMaxPrefix.add(action);
                    continues = true;
                }
            }
        } while (continues);
        this.copy(enteringMaxPrefix);
    }

    /**
     * Evaluate the edge and collects the max prefix, used by the constructor
     * @param lts the LTS to which the edge belongs
     * @param edge the edge under analysis
     * @param actions the sequence of actions representing inevitability
     */
    private void evaluateEdge(LTS lts, GraphEdge edge, List<String> actions) {
        GraphNode source = lts.getSource(edge);
        if (checkActionCoherency(edge.getAction(), actions, source.getMaxPrefix())) {
            Affix<String> tmp = new Affix<>();
            tmp.copy(source.getMaxPrefix());
            tmp.add(edge.getAction());
            retainLongest(tmp);
        } else
            retainLongest(source.getMaxPrefix());
    }
}
