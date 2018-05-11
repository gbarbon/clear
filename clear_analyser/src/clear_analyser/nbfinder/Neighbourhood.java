package clear_analyser.nbfinder;

import clear_analyser.graph.GraphEdge;
import clear_analyser.graph.GraphNode;
import clear_analyser.graph.LTS;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by gbarbon.
 */
public class Neighbourhood {
    private NeighbourhoodType type;  // type of the transition, see below
    private int nOfCorrectTrans;
    private int nOfIncorrectTrans;
    private int nOfNeutralTrans;
    private int nOfIncomingTrans;
    private Collection<GraphEdge> correctTransitions;
    private Collection<GraphEdge> incorrectTransitions;
    private Collection<GraphEdge> neutralTransitions;
    private Collection<GraphEdge> incomingTrans; // in transitions
    private static final AtomicInteger idGen = new AtomicInteger();
    private final int id; // used to have univocal neighbourhoods

    // FIXME: double reference: a GraphNode refers to a neighbourhood, and a neighbourhood refers to the graph node???
    private GraphNode node;  // associated Node

    public Neighbourhood(GraphNode node) {
        type = NeighbourhoodType.UNKNOWN;
        nOfCorrectTrans = 0;
        nOfIncorrectTrans = 0;
        nOfNeutralTrans = 0;
        id = idGen.incrementAndGet();
        this.node = node;
    }

    public int getId() { return id;}
    public NeighbourhoodType getType() { return type;}

    /**
     * Sets the exiting transitions, divided by type, and count them
     * @param lts the Counterexample lts
     */
    public void setTransitions(LTS lts) {
        correctTransitions = lts.getOutCorrectTransitions(node);
        nOfCorrectTrans = correctTransitions.size();
        incorrectTransitions = lts.getOutIncorrectTransitions(node);
        nOfIncorrectTrans = incorrectTransitions.size();
        neutralTransitions = lts.getOutNeutralTransitions(node);
        nOfNeutralTrans = neutralTransitions.size();
        incomingTrans = lts.getInEdges(node);
        nOfIncomingTrans = incomingTrans.size();
    }

    public NeighbourhoodType hasType() {return type;}
    // public Collection<GraphEdge> getCorrectTransitions() {return correctTransitions;}
    // public Collection<GraphEdge> getIncorrectTransitions() {return incorrectTransitions;}
    // public Collection<GraphEdge> getNeutralTransitionsTransitions() {return neutralTransitions;}

    /**
     * Set the type of the neighbourhood by looking at the count values of
     */
    public NeighbourhoodType setType() {
        if (nOfCorrectTrans==0 && nOfIncorrectTrans==0 && nOfNeutralTrans==0) {
            // throw an exception, cannot setType if exiting transitions are all set to 0
        }
        if (nOfNeutralTrans>0) {
            if (nOfCorrectTrans>0 && nOfIncorrectTrans>0)
                type = NeighbourhoodType.GREENREDBLACK;
            else if (nOfCorrectTrans>0 && nOfIncorrectTrans==0)
                type = NeighbourhoodType.GREEN;
            else if (nOfCorrectTrans==0 && nOfIncorrectTrans>0)
                type = NeighbourhoodType.RED;
            else {} // TODO: throw an Exception, this case should not be possible
        } else if (nOfCorrectTrans>0 && nOfIncorrectTrans>0)
                type = NeighbourhoodType.GREENRED;
            else if (node.getId() == 0 && nOfIncorrectTrans>0)
                    // particular case, initial node has only red exiting transitions
                type = NeighbourhoodType.RED;
                // NOTE: to add changes to add the final i transition, one should colour the i
            // transition here with the red (not possible with these two 'else' statements)
        // has only
            else {} //TODO: throw an Exception, cause in this case the neighbourhood can only be GREENRED
        return type;
    }

    /**
     * Allows to compare two neighbourhood, without looking at associated nodes
     * but concentrating on type, number of transitions for each type, and label
     * of transitions.
     * @param other the other Neighbourhood to compare to this
     * @return true if similar (following the above criteria), false otherwise
     */
    public boolean similar(Neighbourhood other) {
        if (type != other.type )
            return false;
        // if all transitions are null, return tru (should not be possible, this means the lts has only one node
        if ((correctTransitions == null && other.correctTransitions == null)
                && (incorrectTransitions == null && other.incorrectTransitions == null)
                && (neutralTransitions == null && other.neutralTransitions == null)
                && (incomingTrans == null && other.incomingTrans == null)
                ){
            return true;
        }
        // I first check null and size, to gain in performances
        if((correctTransitions == null && other.correctTransitions != null)
                || correctTransitions != null && other.correctTransitions == null
                || nOfCorrectTrans != other.nOfCorrectTrans){
            return false;
        }
        if((incorrectTransitions == null && other.incorrectTransitions != null)
                || incorrectTransitions != null && other.incorrectTransitions == null
                || nOfIncorrectTrans != other.nOfIncorrectTrans){
            return false;
        }
        if((neutralTransitions == null && other.neutralTransitions != null)
                || neutralTransitions != null && other.neutralTransitions == null
                || nOfNeutralTrans != other.nOfNeutralTrans){
            return false;
        }
        if((incomingTrans == null && other.incomingTrans != null)
                || incomingTrans != null && other.incomingTrans == null
                || nOfIncomingTrans != other.nOfIncomingTrans){
            return false;
        }
        // now I can compare list of strings
        // I use the function edgesToLabels to rpoduced a alphabetically ordered array of strings
        // I cannot compare directly edges, since they are different even if they have the same label
        // arraylist allows to maintain also duplicated labels
        if (!edgesToLabels(correctTransitions).equals(edgesToLabels(other.correctTransitions))) {
            return false;
        }
        if (!edgesToLabels(incorrectTransitions).equals(edgesToLabels(other.incorrectTransitions))) {
            return false;
        }
        if (!edgesToLabels(neutralTransitions).equals(edgesToLabels(other.neutralTransitions))) {
            return false;
        }
        if (!edgesToLabels(incomingTrans).equals(edgesToLabels(other.incomingTrans))) {
            return false;
        }
        return true;
    }

    /**
     * Convert a Collection<GraphEdge> into an alphabetically ordered
     * ArrayList<String>
     * Arraylists allow to maintain duplicated labels.
     * @param edges the Collection of edges
     * @return the ordered ArrayList of labels (Strings)
     */
    private ArrayList<String> edgesToLabels(Collection<GraphEdge> edges) {
        ArrayList<String> labelsList = new ArrayList<>();
        for (GraphEdge edge : edges) {
            labelsList.add(edge.getAction());
        }
        java.util.Collections.sort(labelsList);
        return labelsList;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + node.getId();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass()) // checking same class
            return false;
        Neighbourhood other = (Neighbourhood) obj;
        if (id != other.id)
            return false;
        return true;
    }

    @Override
    public String toString(){
        String str="";
        //str+="Neighbourhood ID: "+id+"\n";
        str+="In node: "+ node.toStringShort() + "\n";
        str+="Type: "+type + "\n";
        str+="# out Correct transitions (green): "+nOfCorrectTrans + "\n";
        str+="# out Incorrect transitions (red): "+nOfIncorrectTrans + "\n";
        str+="# out Neutral transitions (black): "+nOfNeutralTrans + "\n";
        if (nOfCorrectTrans>0) {
            str += "Correct Out Transitions: ";
            for (GraphEdge edge : correctTransitions) {
                str+=edge.actionToString() + "  ";
            }
            str+="\n";
        }
        if (nOfIncorrectTrans>0) {
            str += "Incorrect Out Transitions: ";
            for (GraphEdge edge : incorrectTransitions) {
                str+=edge.actionToString() + "  ";
            }
            str+="\n";
        }
        if (nOfNeutralTrans>0) {
            str += "Neutral Out Transitions: ";
            for (GraphEdge edge : neutralTransitions) {
                str+=edge.actionToString() + "  ";
            }
            str+="\n";
        }
        if (nOfIncomingTrans>0) { // can be 0 only in initial node
            str += "Entering transitions: ";
            for (GraphEdge edge : incomingTrans) {
                str+=edge.actionToString() + "  ";
            }
            str+="\n";
        }
        return str;
    }

    /**
     * Short string method
     * @return the first letters of the neighbourhood type
     */
    public String toStringShort() {
        String str = "";
        switch (type) {
            case GREEN:
                str = "G";
                break;
            case RED:
                str="R";
                break;
            case GREENRED:
                str="GR";
                break;
            case GREENREDBLACK:
                str="GRB";
                break;
            case UNKNOWN:
                str="U";
                break;
            default:
                str="";
                break;
        }
        return str;
    }
}
