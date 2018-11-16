package clear_analyser.nbfinder;

import clear_analyser.graph.GraphEdge;
import clear_analyser.graph.GraphNode;
import clear_analyser.graph.LTS;
import clear_analyser.utils.STDOut;

import java.util.*;

/**
 * Created by gbarbon.
 */
public class NeighbourhoodFinder {
    private Set<Neighbourhood> allNeighbourhoods;
    private Set<Neighbourhood> onlyGreenNeighbourhoods;
    private Set<Neighbourhood> onlyRedNeighbourhoods;
    private Set<Neighbourhood> onlyGreenRedNeighbourhoods;
    private Set<Neighbourhood> onlyGreenRedBlackNeighbourhoods;
    private Map<GraphNode, Set<GraphEdge>> correctTransToLoad; // transitions to load in the counterexample lts
    private int nOfFirstStepNeighbourhoods; // number of neighbourhoods found with the newFindDiff method
    private STDOut writer;


    public NeighbourhoodFinder(STDOut writer) {
        allNeighbourhoods = new HashSet<>();
        onlyGreenNeighbourhoods = new HashSet<>();
        onlyRedNeighbourhoods = new HashSet<>();
        onlyGreenRedNeighbourhoods = new HashSet<>();
        onlyGreenRedBlackNeighbourhoods = new HashSet<>();
        correctTransToLoad = new HashMap<>();
        nOfFirstStepNeighbourhoods = 0;
        this.writer = writer;
    }

    /**
     * Compares the exiting transition of two nodes by exploiting also the
     * simulation on successors (simulation information already stored and
     * retrieved through the getEquivalentInSpec method).
     * Checked with FSEN17 dataset (result ok), but:
     * TODO: needs to be tested with examples where two transitions with the
     * same label, where one is present in the bad but the other is not
     *
     * @param specGraph Full LTS
     * @param specNode a node in the Full TLS
     * @param badGraph Counterexample LTS
     * @param badNode a node in the Counterexample LTS
     * @return 0 if the out transitions are the same, otherwise the number of different transitions
     */
    private int outTransComparison(LTS specGraph, GraphNode specNode,
                                   LTS badGraph, GraphNode badNode) {
        int nOfDiffEdges = 0; // nodes have equivalent transitions
        Set<GraphEdge> transitionsToAdd;
        GraphEdge newCorrectTransition;
        for(GraphEdge e : specGraph.getOutEdges(specNode)) {
            boolean found = false;
            for(GraphEdge ec : badGraph.getOutEdges(badNode)) {
                // check that the action has the same label AND
                // the destination node in the cLTS is simulated by
                // the destination node in the fLTS
                // TODO: add exception in case getEquivalentInSpec is null
                if (e.getAction().equals(ec.getAction()) &&
                        badGraph.getDest(ec).getEquivalentInSpec().equals(specGraph.getDest(e)))  {
                    found = true;
                    break;
                }
            }
            if (!found) {
                nOfDiffEdges++;
                // load the map of correct transition to add to the bad graph
                newCorrectTransition = new GraphEdge(e.getAction());
                if (correctTransToLoad.containsKey(badNode))
                    // there can be more than one single transition to add
                    transitionsToAdd = correctTransToLoad.get(badNode);
                else
                    transitionsToAdd = new HashSet<>();
                transitionsToAdd.add(newCorrectTransition);
                correctTransToLoad.put(badNode, transitionsToAdd);
            }
        }
        return nOfDiffEdges;
    }

    /**
     * Add the correct transitions loaded in the correctTransition map
     * to the given Counterexample lts
     * @param lts the Counterexample LTS
     * @return false if the lts is not a Counterexample lts, true otherwise
     */
    private boolean loadCorrectTransitions(LTS lts) {
        writer.printComplete("Loading correct transitions", true, true);
        if (!lts.isCounterexampleLTS()) {
            writer.printError("Not a counterexample LTS!", true, true);
            return false; // if the lts is not a counterexample lts
        }
        //System.out.println("Correct Transitions: " + correctTransToLoad.keySet());
        if (correctTransToLoad.isEmpty()) {
            writer.printComplete("No correct transitions to add, no sink state.", true, true);
        }
        for (GraphNode node : correctTransToLoad.keySet()) {
            for (GraphEdge edge : correctTransToLoad.get(node)) {
                lts.addCorrectTransition(node, edge);
            }
        }
        return true;
    }

    /**
     * Searching function for old neighbourhoods (FSEN17), revised with
     * simulation of successor nodes. Needed to locate all the negihbourhood
     * with Correct (GREEN) exiting transitions.
     * First step of neighbourhood localisation. Notice that it does not
     * produces neighbourhood objects, but only sets nodes as belonging to
     * frontier.
     * @param specGraph the Full LTS
     * @param badGraph the Counterexample LTS
     * @return the number of different nodes between the two lts
     */
    public int newFindDiff(LTS specGraph, LTS badGraph) {
        int total = 0;
        long startTime = System.currentTimeMillis();
        for (GraphNode bn : badGraph.getVertices()) {
            if (bn.getEquivalentInSpec() == null) {
                // FIXME: temporary commented
                //System.err.println("No matching node for " + bn);
                // TODO: add exception here, this should not be possible
            } else if (outTransComparison(specGraph, bn.getEquivalentInSpec(), badGraph, bn)!=0) {
                // NOTE(FIXME): to add changes to add the final i transition, one should modify here
                // to avoid the adding of correct transitions to i transitions
                bn.setAsFrontier();
                total++;
            }
        }
        loadCorrectTransitions(badGraph);
        long endTime =  System.currentTimeMillis();
        writer.printComplete("NeighbourhoodFinder.newFindDiff exec time: "+ (endTime-startTime) + "" +
                " ms", true, true);
        return total;
    }


    /**
     * Count the type of exiting transitions, set the type of the neighbourhoods
     * and add the neighbourhood to the correspondig set
     *
     * It modifies the neighbourhood referred by the given node, allowing to
     * maintain a correspondence between the LTS lts and the Sets of the
     * Neighbourhood finder class (this).
     *
     * @param lts the given lts
     * @param node the node under analysis
     * @return the type of the neighbourhood
     */
    private NeighbourhoodType countOutTransSetType(LTS lts, GraphNode node) {
        Neighbourhood neighbourhood = node.getNeighbourhood();
        neighbourhood.setTransitions(lts);
        NeighbourhoodType type = neighbourhood.setType();
        allNeighbourhoods.add(neighbourhood);  // FIXME: redundant?
        switch (type) {
            case GREEN:
                onlyGreenNeighbourhoods.add(neighbourhood);
                break;
            case RED:
                onlyRedNeighbourhoods.add(neighbourhood);
                break;
            case GREENRED:
                onlyGreenRedNeighbourhoods.add(neighbourhood);
                break;
            case GREENREDBLACK:
                onlyGreenRedBlackNeighbourhoods.add(neighbourhood);
                break;
        }
        return type;
    }

    /**
     * Set as incorrect (red) all the transitions in the lts that are not
     * correct
     * @param lts the counterexample lts
     */
    public void ltsRedColourer(LTS lts) {
        long startTime = System.currentTimeMillis();
        if (lts.isCounterexampleLTS()) {
            for (GraphEdge e : lts.getEdges()) {
                if (!e.isCorrect())
                    e.setAsIncorrect(); // set as incorrect all the transitions
            }
        } else {
            // TODO: add exception here
            writer.printComplete("Not a Counterexample LTS!", true, true);
        }
        long endTime =  System.currentTimeMillis();
        writer.printComplete("NeighbourhoodFinder.ltsRedColourer exec time: "+ (endTime-startTime) + "" +
                " ms", true, true);
    }

    /**
     * Colour a collection of edges in black (neutral transitions)
     * @param edges
     */
    public void edgeBlackColourer(Collection<GraphEdge> edges) {
        if (!edges.isEmpty()) {
            for (GraphEdge e : edges) {
                e.setAsNeutral();
            }
        }
    }

    // FIXME: not implemented yet! (but also not used)
    public void iterativeDFS(LTS lts) {
        Stack<GraphNode> dfsStack = new Stack<>();
        //Collection<GraphEdge> decolorList = new ArrayList<>();
        dfsStack.push(lts.getInitialNode());
        GraphNode node;
        while (!dfsStack.empty()) {
            node = dfsStack.pop();
            //decolorList.addAll(lts.getGraph().getInEdges(node)); // add all incoming edges to the list of nodes to decolour
            if (!node.isDfsDiscovered()) {
                node.setDfsDiscovered();
                if (node.isFrontier()) {
                    //edgeBlackColourer(decolorList);
                    //decolorList.clear(); // empty the list
                }
                for (GraphEdge e : lts.getEdges()) {
                    dfsStack.push(lts.getDest(e));
                }
            }
        }
    }

    /**
     *
     *
     * @param lts
     * @param node current node to be analysed
     * @param previousPath set of edges belonging to the path that lead to the current node
     */
    // FIXME: not correct, there's a bug in the implementation (neighbourhood after red
    // FIXME (continue) transitions
    private void recursiveDFS(LTS lts, GraphNode node, Set<GraphEdge> previousPath) {
        node.setDfsDiscovered();
        Set<GraphEdge> path; // used for the path from the initial node to the current one
        if (node.isFrontier()) {
            edgeBlackColourer(previousPath);
            previousPath.clear();  // in this way we don't color in Black more than once the same transition
        }
        for (GraphEdge e : lts.getOutEdges(node)) {
            GraphNode nextNode = lts.getDest(e);
            if (!nextNode.isDfsDiscovered()) {
                path = new HashSet<>(); // need to create another instance, since we must have different sets of paths
                path.addAll(previousPath);
                path.add(e);
                recursiveDFS(lts, nextNode, path);
            }
        }

    }

    private void recursiveDFSInitCaller(LTS lts) {
        recursiveDFS(lts, lts.getInitialNode(), new HashSet<>());
    }

    /**
     * Detects black transitions
     *
     */
    public void blackColorer(LTS lts, GraphNode node) {
        if (node != null) { // added check to avoid extreme case where there are no correct
            // transition, thus no sink exist
            for (GraphEdge e : lts.getInEdges(node)) {
                // System.err.println("BK " + e);
                if (!e.isNeutral()) {
                    if (!e.isCorrect()) { // if not black and not green
                        e.setAsNeutral(); // set back colour
                    }
                    blackColorer(lts, lts.getSource(e)); // recursion on the source of the transition
                }
            }
        }
    }

    private void blackColorerNoRec(LTS lts, GraphNode sink) {
        Set<GraphEdge> black = new HashSet<>();
        Stack<GraphEdge> todo = new Stack<>();
        int debug = 0;

        if (sink != null) {
            for (GraphEdge e : lts.getInEdges(sink)) {
                todo.push(e);
            }

            while (!todo.isEmpty()) {
                GraphEdge e = todo.pop();
                for (GraphEdge predEdge : lts.getInEdges(e.source)) {
                    if (black.add(predEdge)) {
                        todo.push(predEdge);
                        debug++;
                    }
                }
                if (debug%100000 == 0)
                    writer.printComplete("blackColorerNoRec: inserted "+ debug  + " transitions",
                            false, true);
            }

            black.parallelStream().forEach(e -> {
                if (!e.isCorrect()) {
                    e.setAsNeutral();
                }
            });
        }
    }

    /**
     * Starts the blackColorer recursive function
     * @param lts lts colored in red and green
     */
    public void startBlackColorer(LTS lts) {
        long startTime = System.currentTimeMillis();
        // System.err.print("lts init: " + lts.getInitialNode()+"\nlts sink: "+lts.getSink());
        //blackColorer(lts, lts.getSink());
        blackColorerNoRec(lts, lts.getSink());
        long endTime =  System.currentTimeMillis();
        writer.printComplete("NeighbourhoodFinder.startblackColorer exec time: "+
                (endTime-startTime) + " ms", true, true);
    }

    /**
     * Detects the neighbourhood with only red and no green out transitions
     * @param lts
     */
    public void redNeighbourhoodSearch(LTS lts) {
        boolean hasRedInEdge, hasRedOutEdge;
        long startTime = System.currentTimeMillis();
        // TODO: add check on lts: if (!lts.isCounterexampleLTS) throw exception
        // searching neighbourhood, linear time
        for (GraphNode n : lts.getVertices()) {
            if (!n.isFrontier()) { // if a node is already a neighbourhood we do not check it
                // to be egilible as neighbourhood the node should have
                // only black incoming AND at least one red outcoming
                hasRedInEdge = false;
                for (GraphEdge e : lts.getInEdges(n)) { // search on the incoming edge if one is red
                    if (e.isIncorrect()) {  // TODO: can be changed to !e.isNeutral
                        hasRedInEdge = true;
                        break;
                    } // TODO: check if it works also with neighbourhood in initial node
                }
                if (!hasRedInEdge) {
                    hasRedOutEdge = false;
                    for (GraphEdge e : lts.getOutEdges(n)) { // search on the outcoming edge if one is red
                        if (e.isIncorrect()) {
                            hasRedOutEdge = true;
                            break;
                        }
                    }
                    if (hasRedOutEdge) {
                        n.setAsFrontier(); // TODO: we are not distinguishing the types of allNeighbourhoods
                        // TODO: anyway this is automatically a neighbourhood with only red, since we excluded the other three types with the first if condition
                        countOutTransSetType(lts, n);  //TODO: can be optimized, we already now that we are adding a neighbourhood of RED type
                    }
                }
            } else { // the node belongs to the froniter, thus has been detected with the first procedure
                countOutTransSetType(lts, n);
            }
        }
        long endTime =  System.currentTimeMillis();
        writer.printComplete("NeighbourhoodFinder.redNeighbourhoodSearch exec time: "+
                (endTime-startTime) + " ms", true, true);
    }

    /**
     * Retrieves a Map of (Neighbourhood, Set) of similar neighbourhoods
     * @return a map of similar sets of neighbourhood
     */
    public Map<Neighbourhood, Set<Neighbourhood>> setsOfSimilarNeighbourhoods() {
        Map<Neighbourhood, Set<Neighbourhood>> setOfSets = new HashMap<>();
        Neighbourhood theOriginalSimilar = null;
        boolean found = false;
        for (Neighbourhood nb : allNeighbourhoods) {
            for (Neighbourhood internal : setOfSets.keySet()) {
                if (nb.similar(internal)) {
                    found = true;
                    theOriginalSimilar = internal;
                    break;
                }
            }
            if (!found) {
                Set<Neighbourhood> tmpSet = new HashSet<>();
                tmpSet.add(nb);
                setOfSets.put(nb, tmpSet);
            }
            else {
                setOfSets.get(theOriginalSimilar).add(nb);
                found = false;
            }
        }
        return setOfSets;
    }

    private void checkNoUnknown() {
        // TODO: implement function
    }

    public void executor(LTS fullLts, LTS cLts) {
        long startTime = System.currentTimeMillis();
        nOfFirstStepNeighbourhoods = newFindDiff(fullLts, cLts); // first step for searching neighbourhoods (fsen17)
        ltsRedColourer(cLts);
        //recursiveDFSInitCaller(cLts); // not working properly
        startBlackColorer(cLts);
        redNeighbourhoodSearch(cLts);
        long endTime =  System.currentTimeMillis();

        writer.printComplete("Neighbourhood search total exec time: "+ (endTime-startTime) + " " +
                "ms", true, true);
        writer.printComplete(toString(), true, true);
        //System.out.println(printNeighbourhoods());
        writer.printComplete(printNumberNonSimilarNeighbourhood(), true, true);
    }

    /**
     * Liveness executor
     * @param fullLts the full LTS colored for liveness
     */
    public void livenessExecutor(LTS fullLts) {
        long startTime = System.currentTimeMillis();
        livenessNeighbourhoodSearch(fullLts);
        long endTime =  System.currentTimeMillis();
        writer.printComplete("Liveness neighbourhood search total exec time: "+
                (endTime-startTime) + " ms", true, true);
        writer.printComplete(toString(), true, true);
        System.out.println(printNeighbourhoods());
        writer.printComplete(printNumberNonSimilarNeighbourhood(), true, true);
    }

    /**
     * Detects the neighbourhood in case of liveness property (inevitability with only one action)
     * @param lts the full LTS colored for liveness
     */
    private void livenessNeighbourhoodSearch(LTS lts) {
        boolean hasNeutralInEdge, hasColoredOutEdge;
        long startTime = System.currentTimeMillis();

        for (GraphNode n : lts.getVertices()) {
            hasNeutralInEdge = false;
            for (GraphEdge e : lts.getInEdges(n)) {
                // if at least one incoming edge is neutral, then this is a neighbourhood
                if (e.isNeutral()) {  // fixed 07/02/2018
                    hasNeutralInEdge = true;
                    break;
                } // TODO: check if it works also with neighbourhood in initial node
            }
            if (hasNeutralInEdge || lts.getInEdges(n).isEmpty()) {
                // added fix for initial node (isEmpty), 07/02/2018)
                hasColoredOutEdge = false;
                for (GraphEdge e : lts.getOutEdges(n)) {
                    // search on the outgoing edge if one is colored
                    if (e.isIncorrect() || e.isCorrect()) {
                        hasColoredOutEdge = true;
                        break;
                    }
                }
                if (hasColoredOutEdge) {
                    n.setAsFrontier();
                    countOutTransSetType(lts, n);
                }
            }
        }
        long endTime =  System.currentTimeMillis();
        writer.printComplete("NeighbourhoodFinder.livenessNeighbourhoodSearch exec time: "+
                (endTime-startTime) + " ms", true, true);
    }

    @Override
    public String toString(){
        String string = "";
        string = "\nTotal Number of Neighbourhoods: " + allNeighbourhoods.size()
                + "\n(number of neighbourhood detected in first step: " + nOfFirstStepNeighbourhoods + ")"
                //+ "\nNumber of Green Neighbourhoods: " + onlyGreenNeighbourhoods.size()
                //+ "\nNumber of Red Neighbourhoods: " + onlyRedNeighbourhoods.size()
                //+ "\nNumber of GreenRed Neighbourhoods: " + onlyGreenRedNeighbourhoods.size()
                //+ "\nNumber of GreenRedBlack Neighbourhoods: " + onlyGreenRedBlackNeighbourhoods
                //.size()
                + "\nNumber of Green, Red, GreenRed, GreenRedBlack Neighbourhoods: " +
                onlyGreenNeighbourhoods.size() +", " + onlyRedNeighbourhoods.size() +", "+
                onlyGreenRedNeighbourhoods.size() +", " + onlyGreenRedBlackNeighbourhoods.size();
        return string;
    }

    public String printNeighbourhoods(){
        String str="";
        for (Neighbourhood nb : allNeighbourhoods) {
            str+="\n"+nb.toString()+"\n";
        }
        return str;
    }

    public String printNumberNonSimilarNeighbourhood() {
        String str="";
        Map<Neighbourhood, Set<Neighbourhood>> nonSimilar = setsOfSimilarNeighbourhoods();
        str += "Number of non-similar neighbourhoods: "+nonSimilar.size()+ " over "+allNeighbourhoods.size();
        return str;
    }

}
