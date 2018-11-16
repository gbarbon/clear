/*
package graphSimp.graph;

import graphSimp.nbfinder.Neighbourhood;

public class GraphNode_old {
	private final int id;
	private GraphNode_old equivalentInSpec;  // correspondence between the Full LTS and the Counterexample LTS, used only in the COunterexample LTS
	private boolean isFrontier; // isFrontier identifies a neighbourhood
	private boolean dfsDiscovered; // used by recursive DFS search to label discovered nddes
	private Neighbourhood neighbourhood;  // a node can have a neighbourhood only if is in the Frontier (isFrontier == true)
	private boolean belongsToSCC;

	public GraphPrefix commonPrefix; // used in the liveness cascade
	public GraphSuffix commonSuffix; // used in the liveness cascade
	public GraphPrefix maxPrefix; // used in the liveness cascade
	public GraphSuffix maxSuffix; // used in the liveness cascade
	// TODO: checks it makes sense to used Set instead of other data structures

	public GraphNode_old(int id) {
		super();
		this.id = id;
		this.equivalentInSpec = null;
		this.isFrontier = false;
		this.dfsDiscovered = false;
		this.neighbourhood = null;
		this.belongsToSCC = false;
	}

	public int getId() {
		return this.id;
	}

	public boolean isFrontier() {
		return this.isFrontier;
	}

	public void setAsFrontier() {
		isFrontier = true;
		neighbourhood = new Neighbourhood(this);  // FIXME: 'this' is awful!!
	}

	public Neighbourhood getNeighbourhood() { return neighbourhood; }

	public void setDfsDiscovered() { dfsDiscovered = true; }

	public boolean isDfsDiscovered() { return dfsDiscovered; }

	public void setBelongsToSCC() { belongsToSCC = true; }

	public boolean belongsToSCC() { return belongsToSCC;}

	// FIXME: what is this???
	public boolean isBelongsToSCC() { return dfsDiscovered; }

	// following methods are discarded, now implemented by class Affix
	*/
/*
	public void setCommonPrefix(List<String> actions) {
		commonPrefix = actions;
	}

	public void intersectCommonPrefix(List<String> actions) {
		if (commonPrefix==null)
			commonPrefix = new ArrayList<>();
		else
			commonPrefix.retainAll(actions);
	}

	public void unionCommonPrefix(List<String> actions) {
		if (commonPrefix==null)
			commonPrefix = new ArrayList<>();
		commonPrefix.addAll(actions);
	}

	public void unionCommonPrefix(String action) {
		if (commonPrefix==null)
			commonPrefix = new ArrayList<>();
		commonPrefix.add(action);
	}

	public List<String> getCommonPrefix() {
		if (commonPrefix==null)
			commonPrefix = new ArrayList<>();
		return commonPrefix;
	}

	public boolean addToCommonPrefix(String action) {
		if (commonPrefix==null)
			commonPrefix = new ArrayList<>();
		return commonPrefix.add(action);
	}

	public void setMaxPrefix(List<String> actions) {
		maxPrefix = actions;
	}

	public void unionMaxPrefix(List<String> actions) {
		if (maxPrefix==null)
			maxPrefix = new ArrayList<>();
		maxPrefix.addAll(actions);
	}

	public void unionMaxPrefix(String action) {
		if (maxPrefix==null)
			maxPrefix = new ArrayList<>();
		maxPrefix.add(action);
	}

	public List<String> getMaxPrefix() {
		if (maxPrefix==null)
			maxPrefix = new ArrayList<>();
		return maxPrefix;
	}

	public boolean addToMaxPrefix(String action) {
		if (maxPrefix==null)
			maxPrefix = new ArrayList<>();
		return maxPrefix.add(action);
	}

	public void setCommonSuffix(List<String> actions) {
		commonSuffix = actions;
	}

	public void intersectCommonSuffix(List<String> actions) {
		if (commonSuffix==null)
			commonSuffix = new ArrayList<>();
		else
			commonSuffix.retainAll(actions);
	}

	public void unionCommonSuffix(List<String> actions) {
		if (commonSuffix==null)
			commonSuffix = new ArrayList<>();
		commonSuffix.addAll(actions);
	}

	public void unionCommonSuffix(String action) {
		if (commonSuffix==null)
			commonSuffix = new ArrayList<>();
		commonSuffix.add(action);
	}

	public List<String> getCommonSuffix() {
		if (commonSuffix==null)
			commonSuffix = new ArrayList<>();
		return commonSuffix;
	}

	public void addToCommonSuffix(String action) {
		if (commonSuffix==null)
			commonSuffix = new ArrayList<>();
		commonSuffix.add(0, action);
	}

	public void setMaxSuffix(List<String> actions) {
		maxSuffix = actions;
	}

	public void unionMaxSuffix(List<String> actions) {
		if (maxSuffix==null)
			maxSuffix = new ArrayList<>();
		maxSuffix.addAll(actions);
	}

	public void unionMaxSuffix(String action) {
		if (maxSuffix==null)
			maxSuffix = new ArrayList<>();
		maxSuffix.add(action);
	}

	public List<String> getMaxSuffix() {
		if (maxSuffix==null)
			maxSuffix = new ArrayList<>();
		return maxSuffix;
	}

	public void addToMaxSuffix(String action) {
		if (maxSuffix==null)
			maxSuffix = new ArrayList<>();
		maxSuffix.add(0, action);
	} *//*


	@Override
	public String toString() {
		return "GraphNode [id=" + id + "]";
	}

	public String toStringShort() { return "[" + id + "]"; }

	public String prefixSuffixToString() { return "commonPrefix: "+ commonPrefix +", maxPrefix: " +
			maxPrefix + ", commonSuffix: " + commonSuffix + ", maxSuffix: " + maxSuffix; }

	public String printCorrespondence() {
		return "[Node:" + id + " differs from NodeInSpec:" + equivalentInSpec.getId() + "]";
	}

	public final GraphNode_old getEquivalentInSpec() {
		return equivalentInSpec;
	}

	public final void setEquivalentInSpec(GraphNode_old equivalentInSpec) {
		this.equivalentInSpec = equivalentInSpec;
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
		GraphNode_old other = (GraphNode_old) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
*/
