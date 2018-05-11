package clear_analyser.graph;

import clear_analyser.affix.*;
import clear_analyser.nbfinder.Neighbourhood;
import clear_analyser.sccfinder.SCC;

import java.util.Comparator;

public class GraphNode {
	private final int id;
	private GraphNode equivalentInSpec;  // correspondence between the Full LTS and the Counterexample LTS, used only in the COunterexample LTS
	private boolean isFrontier; // isFrontier identifies a neighbourhood
	private boolean dfsDiscovered; // used by recursive DFS search to label discovered nddes
	private Neighbourhood neighbourhood;  // a node can have a neighbourhood only if is in the Frontier (isFrontier == true)

	// SCC class related
	// private boolean belongsToSCC;  // this field has been discarded
	private SCC refSCC;

	// Affix class related
	private CommonPrefix commonPrefix; // used in the liveness cascade
	private CommonSuffix commonSuffix; // used in the liveness cascade
	private MaxPrefix maxPrefix; // used in the liveness cascade
	private MaxSuffix maxSuffix; // used in the liveness cascade
	private CommonSuffix btCommonSuffix; // used in backtracking scc
	private MaxSuffix btMaxSuffix; // used in backtracking scc

	public GraphNode(int id) {
		super();
		this.id = id;
		this.equivalentInSpec = null;
		this.isFrontier = false;
		this.dfsDiscovered = false;
		this.neighbourhood = null;
		// this.belongsToSCC = false;
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

	// FIXME: what is this???
	public boolean isBelongsToSCC() { return dfsDiscovered; }

	public CommonPrefix getCommonPrefix() {
		return commonPrefix;
	}

	public void setCommonPrefix(CommonPrefix commonPrefix) {
		this.commonPrefix = commonPrefix;
	}

	public CommonSuffix getCommonSuffix() {
		return commonSuffix;
	}

	public void setCommonSuffix(CommonSuffix commonSuffix) {
		this.commonSuffix = commonSuffix;
	}

	public CommonSuffix getBTCommonSuffix() {
		return btCommonSuffix;
	}

	public void setBTCommonSuffix(CommonSuffix btCommonSuffix) {
		this.btCommonSuffix = btCommonSuffix;
	}

	public MaxPrefix getMaxPrefix() {
		return maxPrefix;
	}

	public void setMaxPrefix(MaxPrefix maxPrefix) {
		this.maxPrefix = maxPrefix;
	}

	public MaxSuffix getMaxSuffix() {
		return maxSuffix;
	}

	public void setMaxSuffix(MaxSuffix maxSuffix) {
		this.maxSuffix = maxSuffix;
	}

	public MaxSuffix getBTMaxSuffix() {
		return btMaxSuffix;
	}

	public void setBTMaxSuffix(MaxSuffix btMaxSuffix) {
		this.btMaxSuffix = btMaxSuffix;
	}

	public static Comparator<GraphNode> COMPARE_BY_COMMON_PREFIX = new Comparator<GraphNode>() {
		public int compare(GraphNode one, GraphNode other) {
			// note that this is ascending order
			return one.commonPrefix.compareTo(other.getCommonPrefix());
		}
	};

	public static Comparator<GraphNode> COMPARE_BY_COMMON_SUFFIX = new Comparator<GraphNode>() {
		public int compare(GraphNode one, GraphNode other) {
			// note that this is ascending order
			return one.commonSuffix.compareTo(other.getCommonSuffix());
		}
	};

	public static Comparator<GraphNode> COMPARE_BY_ID = new Comparator<GraphNode>() {
		public int compare(GraphNode one, GraphNode other) {
			// note that this is ascending order
			return ((Integer) one.getId()).compareTo(other.getId());
		}
	};

	// following methods are discarded, now implemented by class Affix
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
	} */

	// Here I add some methods for SCC (06/11/2017)

	/**
	 * Used by the SCC.addNode method to set the reference to the SCC.
	 * @param scc the SCC instance to which the node belongs
     */
	public void setSCC(SCC scc) {
		if (scc==null)
			throw new IllegalArgumentException();
		refSCC = scc;
	}

	/**
	 *
	 * @return the SCC to which the node belong (if it belongs to an scc)
     */
	public SCC getSCC() {
		if (refSCC==null)
			throw new NullPointerException();
		return refSCC;
	}

	/**
	 *
	 * @return true if this node belongs to an SCC
     */
	public boolean belongsToSCC() {
		return (refSCC!=null);
	}

	// public void setBelongsToSCC() { belongsToSCC = true; }

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

	public final GraphNode getEquivalentInSpec() {
		return equivalentInSpec;
	}

	public final void setEquivalentInSpec(GraphNode equivalentInSpec) {
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
		GraphNode other = (GraphNode) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
