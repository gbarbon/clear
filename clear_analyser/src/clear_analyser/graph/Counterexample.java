package clear_analyser.graph;

import clear_analyser.utils.STDOut;

/**
 * TODO: temporary class, remove if not useful
 * TODO: created cause cannot find a way to return root of graphs
 *
 * Represents a counterexample in the form of a graph
 */
public class Counterexample extends LTS {

/*
    public Counterexample(DirectedSparseMultigraph<GraphNode,GraphEdge> graph, GraphNode initialNode) {
        super(graph, initialNode);
    }
*/

    public Counterexample(STDOut writer) {
        super(writer);
    }

    // to add the final i transition, not used / not working
    /*private void createSink() {
        GraphNode sink = new GraphNode(-1); // sink is node with -1
        super.addVertex(sink);
        setSink(sink);
    }*/

    // to add the final i transition, not used / not working
    /*public void addFinalITrans() {
        int nodeIndx = this.getVertexCount() -1;
        GraphNode fromNode = this.specNodeIndex.get(nodeIndx);
        createSink();
        GraphEdge edge = new GraphEdge("i");
        edge.setNodes(fromNode, getSink());
        addEdge(edge, fromNode, getSink());
    }*/
}
