package clear_analyser.graph;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import clear_analyser.utils.CmdLineCaller;
import clear_analyser.utils.STDOut;

import java.io.*;
import java.util.*;

/**
 * Represent an LTS, implements Jung
 * Created by gbarbon.
 */
public class LTS extends DirectedSparseMultigraph<GraphNode,GraphEdge>  {
    private GraphNode initialNode;  // initial node of an LTS
    private GraphNode sink;
    private boolean isCounterexampleLTS;
    STDOut outputWriter;
    Map<Integer, GraphNode> specNodeIndex;

    private GraphNode fakeFinalNode;
    private Set<GraphEdge> fakeFinalEdges;

    public LTS(STDOut outputWriter) {
        super();
        initialNode = null;
        sink = null;
        isCounterexampleLTS = false;
        this.outputWriter = outputWriter;
        specNodeIndex = null;
    }

/*    public LTS(DirectedSparseMultigraph<GraphNode, GraphEdge> graph, GraphNode initialNode) {
        super();
        this.initialNode = initialNode;
        sink = null;
        isCounterexampleLTS = false;
    }*/

    public STDOut getOutputWriter() {
        return outputWriter;
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

    @Override
    public Collection<GraphEdge> findEdgeSet(GraphNode n1, GraphNode n2) {
        return super.findEdgeSet(n1, n2);
    }

    /**
     *
     * @return a collection with all the vertices but without the sink
     */
    public Collection<GraphNode> getVerticesWOSink(){
        Collection<GraphNode> vertices = new HashSet<>();
        vertices.addAll(super.getVertices());
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
        outputWriter.printComplete("Sink is: "+ sink.toString(), true, true);
        return sink;
    }

    @Override
    public boolean addEdge(GraphEdge edge, GraphNode source, GraphNode dest) {
       return super.addEdge(edge, source, dest, EdgeType.DIRECTED);
        //Pair<GraphNode> pair = new Pair<>(source, dest);
        //return super.addEdge(edge, pair, EdgeType.DIRECTED);
        //return super.addEdge(edge, source, dest);
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
        edge.setNodes(sourceNode, sink);
        addEdge(edge, sourceNode, sink);
    }

    /**
     *
     * @param node a node of this LTS
     * @return the collection of Correct (green) Transitions exiting from the given node
     */
    public Collection<GraphEdge> getOutCorrectTransitions(GraphNode node) {
        Collection<GraphEdge> transitionSet = new HashSet<>();
        for (GraphEdge edge : super.getOutEdges(node)) {
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
        for (GraphEdge edge : super.getOutEdges(node)) {
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
        for (GraphEdge edge : super.getOutEdges(node)) {
            if (edge.isNeutral())
                transitionSet.add(edge);
            // TODO: implement following check, we cannot return edges if there are still UNSET edges
            /*else if (edge.isUnset())
                throw new Exception(); */
        }
        return transitionSet;
    }

    /**
     *
     * @param node the chosen graph node
     * @return true if the outgoing transitions for the node passed as parameter are all correct,
     * false otherwise
     */
    public boolean areOutTransCorrect(GraphNode node) {
        return (!getOutCorrectTransitions(node).isEmpty() &&
                getOutIncorrectTransitions(node).isEmpty() &&
                getOutNeutralTransitions(node).isEmpty());
    }

    /**
     *
     * @param node the chosen graph node
     * @return true if the outgoing transitions for the node passed as parameter are all incorrect,
     * false otherwise
     */
    public boolean areOutTransIncorrect(GraphNode node) {
        return (getOutCorrectTransitions(node).isEmpty() &&
                !getOutIncorrectTransitions(node).isEmpty() &&
                getOutNeutralTransitions(node).isEmpty());
    }

    /**
     *
     * @param node the chosen graph node
     * @return true if the outgoing transitions for the node passed as parameter are all neutral,
     * false otherwise
     */
    public boolean areOutTransNeutral(GraphNode node) {
        return (getOutCorrectTransitions(node).isEmpty() &&
                getOutIncorrectTransitions(node).isEmpty() &&
                !getOutNeutralTransitions(node).isEmpty());
    }

    /**
     *
     * @param node the chosen graph node
     * @return true if the outgoing transitions for the node passed as parameter belong to the
     * following combinations (where G:correct, R:incorrect, B:neutral): B, B+R, B+G, B+R+G, R+G.
     * Note that the presence of neutral allow to cover the first 4 combinations.
     */
    public boolean areOutTransMixed(GraphNode node) {
        boolean redEmpty, greenEmpty, blackEmpty;

        greenEmpty = getOutCorrectTransitions(node).isEmpty();
        redEmpty = getOutIncorrectTransitions(node).isEmpty();
        blackEmpty = getOutNeutralTransitions(node).isEmpty();

        return !blackEmpty || (!greenEmpty && !redEmpty);
    }


    /**
     * Add final edges for evaluating deadlock and livelocks
     * FIXME: missing removal function, add it !
     */
    public void addFakeFinalEdges() {

        // create fake final node
        fakeFinalEdges = new HashSet<>();
        fakeFinalNode = new GraphNode(-3);
        this.addVertex(fakeFinalNode);

        for (GraphNode node :  this.getVertices() ) {
            if (this.getOutEdges(node).isEmpty()) {
                // throw new NotImplementedException();
                GraphEdge fakeFinalEdge = new GraphEdge("FINALEDGE");
                fakeFinalEdges.add(fakeFinalEdge);
                this.addEdge(fakeFinalEdge, node, fakeFinalNode);
            }
        }
    }

    /**
     * Shallow copy of an LTS
     * @param other the LTS to copy
     * @return the shallow copy
     */
    public static LTS copy(LTS other) {
        LTS res = new LTS(other.getOutputWriter());
        res.specNodeIndex  = new HashMap<>();  // TODO: can be removed?

        for (GraphNode node : other.getVertices()) {
            res.addVertex(node);
            res.specNodeIndex.put(node.getId(), node);  // TODO: can be removed?
        }
        for (GraphEdge edge : other.getEdges()) {
            GraphNode source = other.getSource(edge);
            GraphNode dest = other.getDest(edge);
            res.addEdge(edge, source, dest);
        }

        res.setInitialNode(other.getInitialNode());
        if (other.hasSink()) {
            res.setSink(other.getSink());
        }
        return res;
    }

    /**
     * Shallow copy of a subset of the LTS
     * @param other the LTS to copy
     * @param nodesToCopy nodes representing the subset
     * @return the shallow copy
     */
    public static LTS copySubset(LTS other, Collection nodesToCopy) {
        LTS res = new LTS(other.getOutputWriter());
        res.specNodeIndex  = new HashMap<>();  // TODO: can be removed?

        for (GraphNode node : other.getVertices()) {
            if (nodesToCopy.contains(node)) {
                res.addVertex(node);
                res.specNodeIndex.put(node.getId(), node);  // TODO: can be removed?
            }
        }
        for (GraphEdge edge : other.getEdges()) {
            GraphNode source = other.getSource(edge);
            GraphNode dest = other.getDest(edge);
            if (nodesToCopy.contains(source) && nodesToCopy.contains(dest)) {
                res.addEdge(edge, source, dest);
            }
        }

        res.setInitialNode(other.getInitialNode());
        if (other.hasSink()) {
            res.setSink(other.getSink());
        }
        return res;
    }

    @Override
    public boolean removeEdge(GraphEdge edge) {
        return super.removeEdge(edge);
    }

    /**
     * Remove all the edges containing the action passed as parameter
     * @param action the String describing the edges to remove
     */
    public void removeAllEdges(String action) {
        /*boolean res = false;
        for (GraphEdge edge : getEdges()) {
            if (edge.getAction().equals(action))
                res = removeEdge(edge);
        }*/
        new ArrayList<>(getEdges()).stream()
                .filter(e -> e.getAction().equals(action))
                .forEach(this::removeEdge);
    }

    /**
     * Remove all the edges between the given set of nodes containing the action passed as parameter
     * @param action the String describing the edges to remove
     * @param nodes nodes representing sources and destinations of the edges to remove
     */
    public void removeAllEdges(String action, Collection nodes) {
        /*boolean res = false;
        for (GraphEdge edge : getEdges()) {
            GraphNode source = getSource(edge);
            GraphNode dest = getDest(edge);
            if (nodes.contains(source) && nodes.contains(dest)) {
                if (edge.getAction().equals(action))
                    res = removeEdge(edge);
            }
        }*/
        new ArrayList<>(getEdges()).stream()
                .filter(e -> (e.getAction().equals(action) && nodes.contains(getSource(e)) && nodes
                        .contains(getDest(e))))
                .forEach(this::removeEdge);
    }

    /**
     * Load a graph from an aut file
     * @param baseDir directory
     * @param testName name of the file to load
     * @throws Exception // TODO: to implement correctly
     * @return false if the size of the LTS is less or equal to 1, true otherwise
     */
    public boolean autLoaderOLD(String baseDir, String testName)  throws Exception  {
        BufferedReader br = new BufferedReader(new FileReader(baseDir + "/" + testName + ".aut"));
        // TODO: check that the file is actually an aut file
        // TODO: add exception in case of missing file

        class GraphEdgeExt  {
            GraphEdge edge;
            GraphNode source;
            GraphNode dest;

            GraphEdgeExt(GraphEdge edge, GraphNode source, GraphNode dest) {
                this.edge = edge;
                this.source = source;
                this.dest = dest;
            }
        }

        String line;
        int specStartNodeId = -1;
        int debugIterations = 0;
        Map<Integer, GraphNode> specNodeIndex = new HashMap<>();
        //Map<GraphEdge, Pair> edgePairMap = new Hashtable<>();
        List<GraphEdge> listEdges = new ArrayList<GraphEdge>();
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
                    edge.setNodes(fromNode, toNode);
                    //addEdge(edge, fromNode, toNode);
                    //edgePairMap.put(edge, new Pair<>(fromNode, toNode));
                    listEdges.add(edge);

                    // FIXME: TRY TO AVOID THE NODE INDEX!
                }

            }
            if (debugIterations%100000 == 0)
                outputWriter.printComplete("autLoader: read "+ debugIterations  + " lines", false,
                        false);
            debugIterations++;
        }
        br.close();

        int debug = 0;
        for (GraphEdge edge : listEdges) {
            addEdge(edge, edge.source, edge.dest);
            if (debug%100000 == 0)
                outputWriter.printComplete("autLoader: inserted "+ debug  + " lines", false,
                        false);
            debug++;
        }

        setInitialNode(specNodeIndex.get(specStartNodeId));
        if (super.vertices.size() <= 1) {
            outputWriter.printError("autLoader: possible ERROR, the loaded lts contains only a " +
                    "node", true, true);
            // FIXME: should throw an exception in this case
            return false;
        }
        return true;
    }

    public boolean autLoader(String baseDir, String testName, Boolean fast, boolean safety) throws
            Exception  {
        long startTime = System.currentTimeMillis();
        List<GraphEdge> edgeList;
        edgeList = fileReader(baseDir, testName, fast, safety);
        boolean res = graphLoader(edgeList);
        outputWriter.printComplete("autLoader: loading completed.", true, false);
        long endTime = System.currentTimeMillis();
        outputWriter.printComplete("autLoader exec time: " +
                (endTime - startTime) + " ms\n", true, false);
        return res;
    }


    public List<GraphEdge> fileReader (String baseDir, String testName, boolean fast,
                                       boolean safety)
            throws Exception  {
        BufferedReader br = new BufferedReader(new FileReader(baseDir + "/" + testName + ".aut"));
        // TODO: check that the file is actually an aut file
        // TODO: add exception in case of missing file
        String line;
        int specStartNodeId = -1;
        int debugIterations = 0;
        specNodeIndex = new HashMap<>();
        List<GraphEdge> listEdges = new ArrayList<GraphEdge>();
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
                if (!safety || fast) {
                    // "i" transitions are allowed in liveness mode or
                    // in fast safety mode "i" transition are used only to detect
                    // final transitions
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
                    edge.setNodes(fromNode, toNode);
                    //addEdge(edge, fromNode, toNode);
                    //edgePairMap.put(edge, new Pair<>(fromNode, toNode));
                    listEdges.add(edge);

                    // FIXME: TRY TO AVOID THE NODE INDEX!
                } else {
                    // in normal safety mode, "i" transition cannot be used
                    // (performs the i avoidance if normal mode is used)
                    // this allows to avoid the final transitions "i"
                    if (!action.equals("i") ) {
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
                        edge.setNodes(fromNode, toNode);
                        //addEdge(edge, fromNode, toNode);
                        //edgePairMap.put(edge, new Pair<>(fromNode, toNode));
                        listEdges.add(edge);

                        // FIXME: TRY TO AVOID THE NODE INDEX!
                    }
                }

            }
            if (debugIterations%100000 == 0)
                outputWriter.printComplete("autLoader: read "+ debugIterations  + " lines", false,
                        false);
            debugIterations++;
        }
        br.close();
        setInitialNode(specNodeIndex.get(specStartNodeId));

        outputWriter.printComplete("autLoader: read finished after "+ debugIterations + " " +
                "iterations.", true, false);

        return listEdges;
    }

    public boolean graphLoader(List<GraphEdge> listEdges) {

        int debug = 0;
        for (GraphEdge edge : listEdges) {
            addEdge(edge, edge.source, edge.dest);
            if (debug%100000 == 0)
                outputWriter.printComplete("autLoader: inserted "+ debug  + " lines", false, true);
            debug++;
        }


        outputWriter.printComplete("autLoader: loading finished after "+ debug + " " +
                "iterations.", true, true);

        if (super.vertices.size() <= 1) {
            outputWriter.printError("autLoader: possible ERROR, the loaded lts contains only a " +
                    "node", true, true);
            // FIXME: should throw an exception in this case
            return false;
        }
        return true;
    }

    /**
     * If f option is chosen, it reads the table with the correspondence between states
     * It extracts data from the table containing the equivalence between states
     * @param baseDir
     * @param tableFileName
     * @throws Exception
     */
    public void equivTableLoader(String baseDir, String tableFileName, LTS fullLTS) throws
            Exception {
        BufferedReader br = new BufferedReader(new FileReader(baseDir + "/" + tableFileName
                + ".prd"));
        // TODO: check that the file is actually a table file
        // TODO: add exception in case of missing file
        String line;
        int specStartNodeId = -1;
        int debugIterations = 0;
        Map<Integer, Integer> cLTStoFullLTScorr = new HashMap<>(); // maps
        // <nodeInTheCounterexLTS, nodeInTheFullLTS>
        while ((line = br.readLine()) != null) {

            String[] data = new String[]{"", "", ""};
            int equalSign = line.indexOf("=");
            int comma = line.indexOf(",");
            int openBrace = line.indexOf("{");
            //int closedBrace = line.indexOf("}");
            data[0] = line.substring(0, equalSign).replaceAll("[()]", "");
            data[1] = line.substring(openBrace + 1, comma);
            //data[2] = line.substring(comma + 1, closedBrace).replaceAll("[()]", "");

            int cLTSState = Integer.valueOf(data[0].trim());
            int fullState = Integer.valueOf(data[1].trim());
            //int propState = Integer.valueOf(data[2].trim()); the index of the property is unused

            cLTStoFullLTScorr.put(cLTSState, fullState);

            if (debugIterations % 100000 == 0)
                outputWriter.printComplete("equivTableLoader: read " + debugIterations +
                        " lines", false, true);
            debugIterations++;
        }
        br.close();

        // Now it loads the data in the Counterexample LTS as equivalences
        for (Integer index : specNodeIndex.keySet()) {
            GraphNode node = specNodeIndex.get(index);
            node.setEquivalentInSpec(fullLTS.specNodeIndex.get(cLTStoFullLTScorr.get(index)));
        }

        outputWriter.printComplete("equivTableLoader: read finished after " + debugIterations +
                " iterations.", true, true);
    }

    /**
     * Write the lts with colors in aut format, using colon to separate action and transition type
     * @param baseDir
     * @param fileName
     * @return
     */
    public boolean writeToAut(String baseDir, String fileName) {
        String resFile = fileName+"_clr-res.aut";

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(baseDir + "/"+resFile, false), "utf-8"))) {

            // writing header
            writer.write("des ("+ this.getInitialNode().getId()+", " + this.getEdges().size()+"," +
                    " " + this.getVertices().size()+")\n");

            List<GraphNode> orderedVertices = new ArrayList<>(getVertices());
            Collections.sort(orderedVertices, GraphNode.COMPARE_BY_ID);
            for (GraphNode node: orderedVertices) {
                List<GraphNode> orderedSuccessors = new ArrayList<>(this.getSuccessors(node));
                Collections.sort(orderedSuccessors, GraphNode.COMPARE_BY_ID);
                for (GraphNode dest : orderedSuccessors) {
                    for (GraphEdge edge : this.findEdgeSet(node, dest))
                        writer.write("(" + node.getId() +", \""+ edge.getAction()+"\":"+
                                edge.getType() + ", " + dest.getId() +")\n");
                }
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Write the lts with colors and neighbourhoods in aut format, using colon to separate action
     * and transition type (for colors) and colon to separate state id and neighbourhood type (for
     * neighbourhoods).
     * @param baseDir
     * @param fileName
     * @return
     */
    public boolean autDump(String baseDir, String fileName) {
        String resFile = fileName+"_res-dump.autx";

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(baseDir + "/"+resFile, false), "utf-8"))) {

            // writing header
            writer.write("des ("+ this.getInitialNode().getId()+", " + this.getEdges().size()+"," +
                    " " + this.getVertices().size()+")\n");

            List<GraphNode> orderedVertices = new ArrayList<>(getVertices());
            Collections.sort(orderedVertices, GraphNode.COMPARE_BY_ID);
            for (GraphNode node: orderedVertices) {
                List<GraphNode> orderedSuccessors = new ArrayList<>(this.getSuccessors(node));
                Collections.sort(orderedSuccessors, GraphNode.COMPARE_BY_ID);
                for (GraphNode dest : orderedSuccessors) {
                    for (GraphEdge edge : this.findEdgeSet(node, dest))
                        if (node.isFrontier()) {
                            writer.write("(" + node.getId()+":N:"+node.getNeighbourhood()
                                            .toStringShort()+ ", \""+ edge.getAction()+"\":"+
                                    edge.getType() + ", " + dest.getId() +")\n");
                        } else {
                        writer.write("(" + node.getId()+", \""+ edge.getAction()+"\":"+
                                edge.getType() + ", " + dest.getId() +")\n");
                    }
                }
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Regression check on given file (only liveness)
     * @param baseDir
     * @param fileName
     * @return
     */
    public boolean regressionCheck(String baseDir, String fileName) {
        String resFile = fileName+"_clr-res.aut";
        String expFile = fileName+"_clr-exp.aut";
        int diffReturnValue = 2;  // 2 means error on diff
        List<String> commands = Arrays.asList("diff", resFile, expFile);

        // checking existence of regression results file to mathc results
        File f = new File(baseDir + "/"+ expFile);
        if(!f.exists() || f.isDirectory()) {
            System.out.println(expFile + " is missing");
            return false;
        }
        // write file to aut
        if (!writeToAut(baseDir, fileName)) {
            return false;
        }
        try {
            // diff = 0 means no differences, 1 means differences exist, 2 means error
            diffReturnValue = CmdLineCaller.cmdCaller(commands, null, baseDir, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (diffReturnValue==0);
    }

    public String toSringAut() {
        String str = "";
        for (GraphNode node: this.getVertices()) {
            for (GraphNode dest : this.getSuccessors(node)) {
                for (GraphEdge edge : this.findEdgeSet(node, dest))
                    str+= "(" + node.getId() +", "+ edge.getAction() + ", " + dest.getId() +") " +
                            edge.getType().toString() + "\n";
            }
        }
        return str;
    }

    @Deprecated
    public String toStringAutColors() {
        String str = "des ("+ this.getInitialNode().getId()+", " + this.getEdges().size()+", " + this
                .getVertices().size()+")\n";
        List<GraphNode> orderedVertices = new ArrayList<>(getVertices());
        Collections.sort(orderedVertices, GraphNode.COMPARE_BY_ID);
        for (GraphNode node: orderedVertices) {
            List<GraphNode> orderedSuccessors = new ArrayList<>(this.getSuccessors(node));
            Collections.sort(orderedSuccessors, GraphNode.COMPARE_BY_ID);
            for (GraphNode dest : orderedSuccessors) {
                for (GraphEdge edge : this.findEdgeSet(node, dest))
                    str+= "(" + node.getId() +", \""+ edge.getAction()+"\":l"+ edge.getType() +
                            ", " +
                        dest.getId() +")\n";
            }
        }
        return str;
    }

    public String toSringAutPrefixSuffix() {
        String str = "";
        List<GraphNode> orderedVertices = new ArrayList<>(getVertices());
        Collections.sort(orderedVertices, GraphNode.COMPARE_BY_ID);
        for (GraphNode node: orderedVertices) {
            List<GraphNode> orderedSuccessors = new ArrayList<>(this.getSuccessors(node));
            Collections.sort(orderedSuccessors, GraphNode.COMPARE_BY_ID);
            for (GraphNode dest : orderedSuccessors) {
                for (GraphEdge edge : this.findEdgeSet(node, dest))
                    str+= "(" + node.getId() +", "+ edge.getAction() + ", " + dest.getId() +") " +
                            edge.getType().toString() + "\n";
            }
            str+= "(" + node.getId() +", "+ node.prefixSuffixToString() + "\n";
        }
        return str;
    }
}
