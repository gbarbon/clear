package clear_analyser.graph;

import java.util.concurrent.atomic.AtomicInteger;

public class GraphEdge {
    private static final AtomicInteger idGen = new AtomicInteger();
    private final String action;
    private final int id;
    private boolean visited;  // TODO: remove? Not used?
    private boolean loopDetection;
    private boolean isRelevant; // TODO: remove? Used only by the cexp abstraction ?
    private TransitionType type;  // type of the transition, see below
    private int colorIter;  // used for cascade liveness checking

    public GraphNode source; // TODO: remove this AWFUL thing (info already contained in the LTS)!!
    public GraphNode dest; // TODO: remove this AWFUL thing (info already contained in the LTS)!!

    /**
     * GREEN used for Correct Transition
     * RED used for Incorrect Transition
     * BLACK is for the rest of transitions (Neutral Transition)
     * UNSET is when the transitions have not been set yet (used only for testing correct behaviour)
     */
    public enum TransitionType {
        GREEN, RED, BLACK, UNSET
    }

    public void setAsCorrect() {
        type = TransitionType.GREEN;
    }

    public void setAsIncorrect() {
        type = TransitionType.RED;
    }

    public void setAsNeutral() {
        type = TransitionType.BLACK;
    }

    public void setAsUnset() {
        type = TransitionType.UNSET;
    }

    public void setAsCorrect(int colorIter) {
        type = TransitionType.GREEN;
        this.colorIter = colorIter;
    }

    public void setAsIncorrect(int colorIter) {
        type = TransitionType.RED;
        this.colorIter = colorIter;
    }

    public void setAsNeutral(int colorIter) {
        type = TransitionType.BLACK;
        this.colorIter = colorIter;
    }

    public void setAsUnset(int colorIter) {
        type = TransitionType.UNSET;
        this.colorIter = colorIter;
    }

    public boolean isCorrect() {
        return type.equals(TransitionType.GREEN);
    }

    public boolean isIncorrect() {
        return type.equals(TransitionType.RED);
    }

    public boolean isNeutral() {
        return type.equals(TransitionType.BLACK);
    }

    public boolean isUnset() {
        return type.equals(TransitionType.UNSET);
    }

    public TransitionType getType() {
        return type;
    }

    public final String getAction() {
        return action;
    }

    public GraphEdge(String action) {
        super();
        this.action = action;
        id = idGen.incrementAndGet();
        visited = false;
        loopDetection = false;
        isRelevant = false;  // used when abstracting a counterexample
        type = TransitionType.UNSET;
        colorIter = -1; // setting colouring iteration counter for cascade liveness checking
    }

    public void setNodes(GraphNode source, GraphNode dest) {
        this.source = source;
        this.dest = dest;
    }


    public void setVisited() {
        this.visited = true;
    }

    public boolean isVisited() {
        return this.visited;
    }

    public void setLoop(boolean val) {
        this.loopDetection = val;
    }

    public boolean isLoop() {
        return this.loopDetection;
    }

    public boolean isRelevant() {
        return this.isRelevant;
    }

    public void setRelevant() {
        this.isRelevant = true;
    }

    public int getId() {
        return id;
    }

    // added for SCC colouring
    public void setType(TransitionType type) {
        this.type = type;
    }

    // edge coloring
    public void setColorIter(int i) {
        colorIter = i;
    }

    public int getColorIter() {
        return colorIter;
    }

    /**
     * Checks wether it is possible to color in liveness analysis
     * @param currentIter the current color iteration number
     * @return True if colorIter is equal to currentIter or if it is -1, False otherwise
     */
    public boolean canColor(int currentIter) {
        return ((colorIter == currentIter) || colorIter==-1);
    }

    @Override
    public String toString() {
        return action;
    }

    public String toStringSmall() {
        return " -- " + action + " -> ";
    }

    public String actionToString() {
        return action;
    }

    public String toStringLong() {
        return "GraphEdge [action=" + action + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GraphEdge other = (GraphEdge) obj;
        if (id != other.id)
            return false;
        return true;
    }

}
