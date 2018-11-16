package clear_analyser.utils.isomorphismtester;
import edu.ucla.sspace.graph.DirectedGraph;
import edu.ucla.sspace.graph.DirectedTypedEdge;
import edu.ucla.sspace.graph.isomorphism.SubGraphIsomorphismTester;

public class IsomorphismTest {
	private static String testName = "test";
	private static String baseDir = "/Users/vleroy/Desktop/graphsimp_tests/";// +testName;

	public static void main(String[] args) throws Exception {
		AutReaderSspace specReader = new AutReaderSspace(baseDir, testName); // complete
		// spec
		int specStartNode = specReader.getInitialNode();
		DirectedGraph<DirectedTypedEdge<String>> specGraph = specReader.getGraph();

		System.out.println(
				"spec graph " + specGraph.vertices().size() + " edges, " + specGraph.edges().size() + " vertices");

		AutReaderSspace badReader = new AutReaderSspace(baseDir, "bad" + testName); // bad
		// graph
		int badStartNode = badReader.getInitialNode();
		DirectedGraph<DirectedTypedEdge<String>> badGraph = badReader.getGraph();

		final int vertToAdd = specGraph.vertices().size()-badGraph.vertices().size();
		for(int i = 0; i <vertToAdd; i++){
			badGraph.add(Integer.MAX_VALUE-i);
		}
		
		System.out.println(
				"bad graph " + badGraph.vertices().size() + " edges, " + badGraph.edges().size() + " vertices");

		
		SubGraphIsomorphismTester isoFinder = new SubGraphIsomorphismTester();

		System.out.println(isoFinder.findIsomorphism(badGraph, specGraph));

		// DirectedGraph<DirectedTypedEdge<String>> g1 = new
		// DirectedMultigraph<String>();
		// g1.add(new SimpleDirectedTypedEdge<String>("hello", 11, 12));
		// g1.add(new SimpleDirectedTypedEdge<String>("hello2", 11, 12));
		// g1.add(new SimpleDirectedTypedEdge<String>("hi", 11, 3));
		// g1.add(new SimpleDirectedTypedEdge<String>("bye", 3, 4));
		// DirectedGraph<DirectedTypedEdge<String>> g2 = new
		// DirectedMultigraph<String>();
		// g2.add(new SimpleDirectedTypedEdge<String>("hello", 2, 1));
		// // g2.add(new SimpleDirectedTypedEdge<String>("hello2", 2, 1));
		// g2.add(new SimpleDirectedTypedEdge<String>("hi", 2, 3));
		// // g2.add(4);
		// System.out.println(isoFinder.findIsomorphism(g2, g1));
		// // System.out.println(isoFinder.areIsomorphic(g2, g1));
	}

}
