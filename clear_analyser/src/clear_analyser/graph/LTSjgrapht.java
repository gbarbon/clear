package clear_analyser.graph;

import org.jgrapht.alg.DirectedNeighborIndex;
import org.jgrapht.graph.ListenableDirectedGraph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * Represent an LTS, implements JGraphT
 * Created by gbarbon.
 */
public class LTSjgrapht extends ListenableDirectedGraph<GraphNode,GraphEdge> {
    private GraphNode initialNode;  // initial node of an LTS
    private GraphNode sink;
    private boolean isCounterexampleLTS;
    private DirectedNeighborIndex<GraphNode, GraphEdge> index;  // cache of each vertex
    // neighbourhood

    public LTSjgrapht() {
        super(GraphEdge.class);
        initialNode = null;
        sink = null;
        isCounterexampleLTS = false;
        index = new DirectedNeighborIndex<>(this);
        addGraphListener(index);  // adding listener to current graph
    }

    public GraphNode getInitialNode() {
        return initialNode;
    }

    public void setInitialNode(GraphNode node) {
        this.initialNode = node;
    }

    private boolean hasSink() {
        return sink != null;
    }

    public void setSink(GraphNode node) {
        sink = node;
    }

    public GraphNode getSink() {
        return sink;
    }

    public boolean isCounterexampleLTS() {
        return isCounterexampleLTS;
    }

    public void setAsCounterexampleLTS() {
        isCounterexampleLTS = true;
    }


    /**
     *
     * @return a collection with all the vertices but without the sink
     */
    public Collection<GraphNode> getVerticesWOSink(){
        Collection<GraphNode> vertices = new HashSet<>();
        vertices.addAll(super.vertexSet());
        vertices.remove(sink);
        return vertices;
    }

    /**
     * creates and return the sink
     */
    private GraphNode createSink() {
        // the id of the sink graph is the id of the vertex with the highest id +1
        // that is equivalent to the current size of the graph
        //System.out.println("Creating sink");
        // sink = new GraphNode(super.getVertexCount());  // OLD VERSION, after script
        // modification did not work anymore
        sink = new GraphNode(-1); // sink is node with -1
        super.addVertex(sink);   // TODO: check if this is necessary!
        System.out.println("Sink is: "+ sink.toString());
        return sink;
    }

    // wrapping all functions from jung to jgrapht:

    public Collection<GraphEdge> getOutEdges(GraphNode node) {
        return super.outgoingEdgesOf(node);
    }

    public Collection<GraphEdge> getInEdges(GraphNode node) {
        return super.incomingEdgesOf(node);
    }


    public Collection<GraphNode> getSuccessors(GraphNode node) {
        return index.successorsOf(node);
    }

    public Collection<GraphNode> getPredecessors(GraphNode node) {
        return index.predecessorsOf(node);
    }

    public int getSuccessorCount(GraphNode node) {
        return getSuccessors(node).size();
    }

    public boolean addEdge(GraphEdge edge, GraphNode source, GraphNode dest) {
        addVertex(source);
        addVertex(dest);
        return super.addEdge(source, dest, edge);
    }

    public Collection<GraphEdge> findEdgeSet(GraphNode n1, GraphNode n2) {
        return getAllEdges(n1, n2);
    }

    public GraphNode getDest(GraphEdge edge) {
        return getEdgeTarget(edge);
    }

    public GraphNode getSource(GraphEdge edge) {
        return getEdgeSource(edge);
    }

    public Collection<GraphEdge> getEdges() {
        return edgeSet();
    }

    public Collection<GraphNode> getVertices() {
        return vertexSet();
    }

    public int getEdgeCount() {
        return getEdges().size();
    }

    /**
     * Add a correct transition to the Counterexample LTS (always out transitions)
     * @param sourceNode
     * @param edge
     */
    public void addCorrectTransition(GraphNode sourceNode, GraphEdge edge){
        //System.out.println("Adding a correct transition");
        if (!hasSink()) {
            //System.out.println("Lts does not have the sink, adding it now");
            createSink(); // create the sink node if not created yet
        }
        edge.setAsCorrect();
        addEdge(edge, sourceNode, sink);
    }

    /**
     *
     * @param node a node of this LTS
     * @return the collection of Correct (green) Transitions exiting from the given node
     */
    public Collection<GraphEdge> getOutCorrectTransitions(GraphNode node) {
        Collection<GraphEdge> transitionSet = new HashSet<>();
        for (GraphEdge edge : super.outgoingEdgesOf(node)) {
            if (edge.isCorrect())
                transitionSet.add(edge);
            // TODO: implement following check, we cannot return edges if there are still UNSET edges
            /*else if (edge.isUnset())
                throw new Exception(); */
        }
        return transitionSet;
    }

    /**
     *
     * @param node a node of this LTS
     * @return the collection of Incorrect (red) Transitions exiting from the given node
     */
    public Collection<GraphEdge> getOutIncorrectTransitions(GraphNode node) {
        Collection<GraphEdge> transitionSet = new HashSet<>();
        for (GraphEdge edge : super.outgoingEdgesOf(node)) {
            if (edge.isIncorrect())
                transitionSet.add(edge);
            // TODO: implement following check, we cannot return edges if there are still UNSET edges
            /*else if (edge.isUnset())
                throw new Exception(); */
        }
        return transitionSet;
    }

    /**
     *
     * @param node a node of this LTS
     * @return the collection of Neutral (black) Transitions exiting from the given node
     */
    public Collection<GraphEdge> getOutNeutralTransitions(GraphNode node) {
        Collection<GraphEdge> transitionSet = new HashSet<>();
        for (GraphEdge edge : super.outgoingEdgesOf(node)) {
            if (edge.isNeutral())
                transitionSet.add(edge);
            // TODO: implement following check, we cannot return edges if there are still UNSET edges
            /*else if (edge.isUnset())
                throw new Exception(); */
        }
        return transitionSet;
    }

    /**
     * Load a graph from an aut file
     * @param baseDir directory
     * @param testName name of the file to load
     * @throws Exception // TODO: to implement correctly
     * @return false if the size of the LTS is less or equal to 1, true otherwise
     */
    public boolean autLoader(String baseDir, String testName)  throws Exception  {
        BufferedReader br = new BufferedReader(new FileReader(baseDir + "/" + testName + ".aut"));
        // TODO: check that the file is actually an aut file
        // TODO: add exception in case of missing file
        String line;
        int specStartNodeId = -1;
        int debugIterations = 0;
        Map<Integer, GraphNode> specNodeIndex = new HashMap<>();
        while ((line = br.readLine()) != null) {
            if (line.startsWith("des")) {
                specStartNodeId = Integer.parseInt(line.substring(5, line.length() - 1).split(",")[0].trim());
            } else {

                String[] data = new String[]{"", "", ""};
                int firstComma = line.indexOf(",");
                int lastComma = line.lastIndexOf(",");
                data[0] = line.substring(0, firstComma).replaceAll("[()]", "");
                data[1] = line.substring(firstComma + 1, lastComma);
                data[2] = line.substring(lastComma + 1).replaceAll("[()]", "");
                // label with quotes
                int firstIndx = line.indexOf("\"");
                if (firstIndx != -1) {
                    int lastIndx = line.lastIndexOf("\"");
                    data[1] = line.substring(firstIndx + 1, lastIndx);
                }

                int fromNodeId = Integer.valueOf(data[0].trim());
                String action = data[1].trim();
                int toNodeId = Integer.valueOf(data[2].trim());

                // TODO: see if it is possible to avoid "i" transitions directly on the script
                if (!action.equals("i")){ // this allows to avoid the final transitions "i"
                    GraphEdge edge = new GraphEdge(action);
                    GraphNode fromNode = specNodeIndex.get(fromNodeId);
                    if (fromNode == null) {
                        fromNode = new GraphNode(fromNodeId);
                        specNodeIndex.put(fromNodeId, fromNode);
                    }
                    GraphNode toNode = specNodeIndex.get(toNodeId);
                    if (toNode == null) {
                        toNode = new GraphNode(toNodeId);
                        specNodeIndex.put(toNodeId, toNode);
                    }
                    addEdge(edge, fromNode, toNode);
                }

            }
            if (debugIterations%1000 == 0)
                System.out.println("autLoader: read "+ debugIterations  + " lines");
            debugIterations++;
        }
        br.close();
        setInitialNode(specNodeIndex.get(specStartNodeId));
        if (super.vertexSet().size() <= 1) {
            System.err.println("autLoader: possible ERROR, the loaded lts contains only a node");
            // FIXME: should throw an exception in this case
            return false;
        }
        return true;
    }

}
