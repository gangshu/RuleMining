package edu.cwru.eecs.statianalysis.pattern;

import java.io.Serializable;

import edu.cwru.eecs.statianalysis.data.Edge;
import edu.cwru.eecs.statianalysis.data.Vertex;
import edu.cwru.eecs.statianalysis.graph.PDGGraph;

public class PDG <V extends Vertex, E extends Edge<V>> implements Serializable{
	/**
	 * The ID of this procedure dependence graph
	 */
	private String pdgId;
	/**
	 * The name of this procedure
	 */
	private String pdgName;
	/**
	 * The name of the file where the procedure is located
	 */
	private String fileName;
	/**
	 * The starting line of this procedure in the file
	 */
	private int startLine;
	/**
	 * The ending line of this procedure in the file
	 */
	private int endLine;
	/**
	 * The dependence graph of this procedure
	 */
	private PDGGraph<V, E> pdgGraph;

	public PDG(String pdgId) {
		this.pdgId = pdgId;
	}

	public PDG(String pdgId, String pdgName, String fileName, int startLine,
			int endLine, PDGGraph<V, E> pdgGraph) {
		this.pdgId = pdgId;
		this.pdgName = pdgName;
		this.fileName = fileName;
		this.startLine = startLine;
		this.endLine = endLine;
		this.pdgGraph = pdgGraph;
	}

	public String getPdgId() {
		return pdgId;
	}

	public void setPdgId(String pdgId) {
		this.pdgId = pdgId;
	}

	public String getPdgName() {
		return pdgName;
	}

	public void setPdgName(String pdgName) {
		this.pdgName = pdgName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getStartLine() {
		return startLine;
	}

	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}

	public int getEndLine() {
		return endLine;
	}

	public void setEndLine(int endLine) {
		this.endLine = endLine;
	}

	public PDGGraph<V, E> getPdgGraph() {
		return pdgGraph;
	}

	public void setPdgGraph(PDGGraph<V, E> pdgGraph) {
		this.pdgGraph = pdgGraph;
	}

}
