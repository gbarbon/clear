package clear_analyser.sccfinder;

import clear_analyser.graph.GraphNode;
import clear_analyser.graph.LTS;

// TODO: not tested yet
public class TarjanRecursive extends AbstractTarjan {

    /**
     *
     * @param lts graph containing neighbourhoods
     */
    public TarjanRecursive(LTS lts) {
        super(lts);
    }

    /**
     * Recursive tarjan algorithm caller
     */
    @Override
    public SCCSet tarjanCaller() {
        SCCSet sccSet = new SCCSet(this.lts);
        // TODO: is this loop useful?
        for (GraphNode node : lts.getVertices()) {
            if (!indexMap.containsKey(node)) {
                sccSet.addAll(strongConnect(node));
            }
        }
        return sccSet;
    }

    /**
     *
     * @param node is the current graph node
     * @return a list of scc
     */
    private SCCSet strongConnect(GraphNode node) {
        indexMap.put(node, index);
        lowLinkMap.put(node, index);
        index++;
        sccStack.push(node);
        SCCSet res = new SCCSet(this.lts);

        // iterate over node successors
        for (GraphNode succ : lts.getSuccessors(node)) {
            if (!indexMap.containsKey(succ)) {
                // succ has not been visited
                res.addAll(strongConnect(succ));
                lowLinkMap.put(node,
                        Math.min(lowLinkMap.get(node), lowLinkMap.get(succ)));
            } else if (sccStack.contains(succ)){
                // succ is in stack, so it belongs to current SCC
                lowLinkMap.put(node,
                        Math.min(lowLinkMap.get(node), indexMap.get(succ)));
            }
        }

        // if node is a root node (of an SCC), return the scc
        if (lowLinkMap.get(node).equals(indexMap.get(node))) {
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

        return res;
    }

}
