package clear_analyser.utils.isomorphismtester;
import java.io.BufferedReader;
import java.io.FileReader;

import edu.ucla.sspace.graph.DirectedGraph;
import edu.ucla.sspace.graph.DirectedMultigraph;
import edu.ucla.sspace.graph.DirectedTypedEdge;
import edu.ucla.sspace.graph.SimpleDirectedTypedEdge;

public class AutReaderSspace {
	private int initialNode;
	private DirectedGraph<DirectedTypedEdge<String>> graph;

	public AutReaderSspace(String baseDir, String testName) throws Exception {
		graph = new DirectedMultigraph<>();
		reader(baseDir, testName);
	}

	private void reader(String baseDir, String testName) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(baseDir + "/" + testName + ".aut"));
		String line;
		int specStartNodeId = -1;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("des")) {
				specStartNodeId = Integer.parseInt(line.substring(5, line.length() - 1).split(",")[0].trim());
			} else {

				String[] data = new String[] { "", "", "" };
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
				this.graph.add(new SimpleDirectedTypedEdge<String>(action, fromNodeId + 1, toNodeId + 1));
			}
		}
		br.close();
		this.initialNode = specStartNodeId;
	}

	public int getInitialNode() {
		return this.initialNode;
	}

	public DirectedGraph<DirectedTypedEdge<String>> getGraph() {
		return this.graph;
	}
}
