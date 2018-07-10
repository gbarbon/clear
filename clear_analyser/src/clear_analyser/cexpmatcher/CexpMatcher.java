package clear_analyser.cexpmatcher;

import clear_analyser.graph.GraphEdge;
import clear_analyser.graph.GraphNode;
import clear_analyser.graph.LTS;
import clear_analyser.matcher.BFSStackMatcherMinDiff;
import clear_analyser.modelchecker.CADP;
import clear_analyser.graph.Counterexample;
import clear_analyser.nbfinder.Neighbourhood;
import clear_analyser.utils.STDOut;

import java.io.*;
import java.util.*;


/**
 * Allows counterexample abstraction
 */
public class CexpMatcher {
    private Counterexample cexp;  // counterexample trace
    private LTS badGraph;
    private int maxAttempts; //maximum number of attempts to find a counterex.
    private String baseDir;
    private CADP cadp;
    private boolean isAbstracted = false; //true if the cexp has been abstracted
    private int absCexpLength = 0; // lenght of the abstracted counterexample
    private STDOut outputWriter;
    private Map<GraphNode, Neighbourhood> foundNbs;  // used to track neighbourhood in spec

    /**
     * Class constructor
     *
     * @param maxAttempts max number of iteration to prdouce a counterexample
     * @param baseDir ref directory
     * @param badGraph counterexample LTS to compare with
     * @param cadp cadp model checker instance
     */
    // TODO: also normal LTS (not a counterexample LTS) can be used
    public CexpMatcher(int maxAttempts, String baseDir,
                       LTS badGraph, CADP cadp, STDOut writer) {
        this.maxAttempts = maxAttempts;
        this.baseDir = baseDir;
        this.badGraph = badGraph;
        this.cadp = cadp;
        this.outputWriter = writer;
        this.foundNbs = new HashMap<>();
    }

    /**
     * Call counterexample production, matching and abstraction methods.
     *
     * @return false if the counterexample is not created, true otherwise
     * @throws Exception
     */
    // TODO: exception handling not working, improve it
    // FIXME: this is not the best way to handle counterexample abstraction in case of lassos !l
    public boolean execute(boolean safety) {
        boolean res = false;
        long startTime, endTime;
        startTime = System.currentTimeMillis();
        try {
            if (!shortestCexpProducer(safety))
                return false;
            matchCexp();
            cexpAbstraction();
            endTime = System.currentTimeMillis();
            //if (safety) {
                outputWriter.printComplete("Shortest Cexp:      " + cexpToString(), true, true);
            outputWriter.printComplete("Dump:               " + cexpDump(), true, true);

            outputWriter.printComplete("Abs. Shortest Cexp: " + absCexpToString(), true, true);
            //} else {
                //outputWriter.printComplete("Shortest and abstracted shortest counterexample " +
                //        "cannot be printed\nThey are not in the form of a trace.", true, true);
            //}
            outputWriter.printComplete(metrics(), true, true);
            outputWriter.printComplete("Found Neighbourhood correspondence:\n\n"+foundNbsToString
                    (),
                    true, true);
            res = true;
        } catch (FileNotFoundException e) {
            endTime = System.currentTimeMillis();
            outputWriter.printComplete("Shortest Cexp was not produced.", true, true);
            //e.printStackTrace();
        }
        outputWriter.printComplete("Counterexample Abstraction total exec time: " +
                (endTime - startTime) + " ms", true, true);
        return res;
    }

    /**
     * Try to produce a counterexample, max 'maxAttempt' attempts.
     * @return true if a counterexample is produced, false otherwise
     * @throws Exception
     */
    private boolean cexpProducer(boolean safety) throws Exception{
        this.cexp = cadp.counterexampleGen(0, maxAttempts, safety);
        return (this.cexp != null);
    }

    /**
     * Produce the shortest counterexample
     * @return true if a counterexample is produced, false otherwise
     */
    private boolean shortestCexpProducer(boolean safety) throws FileNotFoundException {
        this.cexp = cadp.shortestCounterexampleGen(safety);
        // this.cexp.addFinalITrans(); // to add final trace to counterexample, not used / not
        // working
        return (this.cexp != null);
    }
    /**
     * Matches the counterexample with the Counterexample LTS.
     * Every node of the counterexample will have a correspondence
     * with the counterexample LTS.
     *
     * Uses existing algorithm
     *
     * Nodes are sets as corresponding using the setEquivalentInSpec method.
     */
    private void matchCexp() {
        BFSStackMatcherMinDiff sm = new BFSStackMatcherMinDiff(outputWriter);
        sm.matchNodes(badGraph, badGraph.getInitialNode(), cexp, cexp.getInitialNode());
        autDump();
    }

    /**
     * Abstracts of a counterexample.
     */
    private void cexpAbstraction() {
        for (GraphNode node:
             this.cexp.getVertices()) {
            if (node.getEquivalentInSpec().isFrontier()) {
                // setting corresponding states in frontier as frontier
                node.setAsFrontier();
                this.isAbstracted = true;
                foundNbs.put(node, node.getEquivalentInSpec().getNeighbourhood());
                // setting incoming and outgoing transitions as relevant
                for (GraphEdge edge : cexp.getOutEdges(node)) {
                    if (!edge.isRelevant()) {
                        edge.setRelevant();
                        this.absCexpLength++;
                    }
                }
                for (GraphEdge edge : cexp.getInEdges(node)) {
                    if (!edge.isRelevant()) {
                        edge.setRelevant();
                        this.absCexpLength++;
                    }
                }
            }
        }
        // TODO: can getOutEdges and getInEdges be replaced by getIncidentEdges?
    }

    /**
     * @param printRelevant true if we want to print only relevant actions, false otherwise
     * @return the counterexample in the form of a String
     */
    private String cexpToString(boolean printRelevant) {
        GraphNode tmp = this.cexp.getInitialNode();
        GraphEdge currEdge;
        String str;
        if (printRelevant && tmp.getNeighbourhood()!=null)
            str = "("+ tmp.getId() + ":" + foundNbs.get(tmp).toStringShort() +")--";
        else
            str = "("+ tmp.getId() +")--";
        while(!this.cexp.getOutEdges(tmp).isEmpty()) {
            currEdge = this.cexp.getOutEdges(tmp).iterator().next();
            if (printRelevant) {
                if (currEdge.isRelevant())
                    str += "["+ currEdge.toString() + "]";
                else
                    str += "[...]";
            } else
                str += "[" + currEdge.toString() + "]";
            tmp = this.cexp.getDest(currEdge);
            if (printRelevant && tmp.getNeighbourhood()!=null) {
                str+="-->("+ tmp.getId() + ":" + foundNbs.get(tmp).toStringShort() + ")";
            } else
                str+="-->("+ tmp.getId() +")";
            if (!this.cexp.getOutEdges(tmp).isEmpty()) {
                str += "--";
            }
        }
        return str;
    }

    /**
     * @return the counterexample in the form of a String
     */
    private String cexpDump() {
        GraphNode tmp = this.cexp.getInitialNode();
        GraphEdge currEdge;
        String str;
        str = "("+ tmp.getEquivalentInSpec().getId() +")--";
        while(!this.cexp.getOutEdges(tmp).isEmpty()) {
            currEdge = this.cexp.getOutEdges(tmp).iterator().next();
            str += "[" + currEdge.toString() + "]";
            tmp = this.cexp.getDest(currEdge);
            str+="-->("+ tmp.getEquivalentInSpec().getId() +")";
            if (!this.cexp.getOutEdges(tmp).isEmpty()) {
                str += "--";
            }
        }
        return str;
    }

    public boolean autDump() {
        String resFile = "cexp_dump.aut";

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(baseDir + "/"+resFile, false), "utf-8"))) {

            // writing header
            writer.write("des ("+ this.cexp.getInitialNode().getId()+", " + this.cexp.getEdges()
                    .size()+", " + this.cexp.getVertices().size()+")\n");

            List<GraphNode> orderedVertices = new ArrayList<>(cexp.getVertices());
            Collections.sort(orderedVertices, GraphNode.COMPARE_BY_ID);
            for (GraphNode node: orderedVertices) {
                List<GraphNode> orderedSuccessors = new ArrayList<>(this.cexp.getSuccessors(node));
                Collections.sort(orderedSuccessors, GraphNode.COMPARE_BY_ID);
                for (GraphNode dest : orderedSuccessors) {
                    for (GraphEdge edge : this.cexp.findEdgeSet(node, dest))
                        writer.write("(" + node.getEquivalentInSpec().getId()+ ", \""+ edge
                                .getAction()+"\"" + ", " +
                                dest.getEquivalentInSpec().getId() +")\n");
                }
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String cexpToString() {
        return cexpToString(false);
    }

    /**
     *
     * @return a string with the discovered neighbourhoods
     */
    public String foundNbsToString() {
        String str="";
        for(GraphNode node : foundNbs.keySet()){
            str += "In Counterexample node: "+ node.toStringShort() +"\n"+
                    foundNbs.get(node).toString()+ "\n";
        }
        return str;
    }

    /**
     *
     * @return the abstracted counterexample in the form of a String
     */
    public String absCexpToString() {
        return cexpToString(true);
    }

    /**
     *
     * @return The length of the counterexample
     */
    public int cexpLength() {
        return this.cexp.getEdgeCount();
    }

    /**
     *
     * @return the length of the abstracted counterexample. Return 0 if the
     * counterexample has not been abstracted
     */
    public int absCexpLength() {
        return this.absCexpLength;
    }

    /**
     *
     * @return true if cexp has been abstracted, false otherwise
     */
    public boolean isAbstracted() {
        return this.isAbstracted;
    }

    public String metrics() {
        return "Cexp length is: "+cexpLength()+"\n"+
                "Abstraction Successful: "+isAbstracted()+"\n"+
                "AbsCexp length is: "+absCexpLength();
    }

}
