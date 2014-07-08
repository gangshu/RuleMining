package edu.cwru.eecs.statianalysis.data;

import java.io.Serializable;

public class Vertex implements Serializable{
	/**
	 * File name where the vertex is located
	 */
	protected String filename;
	/**
	 * Class id where the vertex is located. This may be NULL in a non-objec
	 * oriented program
	 */
	protected String classId;
	/**
	 * Procedure/function id where the vertex is located.
	 */
	protected String pdgId;
	/**
	 * The ID of the vertex within the procedure. This is a global ID
	 */
	protected String pdgVertexId;
	/**
	 * Vertex kind can be one of the following: actual-in actual-out auxiliary
	 * body call-site control-point declaration entry exit expression formal-in
	 * formal-out global-actual-in global-actual-out global-formal-in
	 * global-formal-out indirect-call jump label return switch-case
	 * variable-initialization exceptional-exit exceptional-return normal-exit
	 * normal-return
	 */
	protected String pdgVertexKind;
	/**
	 * The key of this vertex. It is a global ID
	 */
	protected int vertexKey;
	/**
	 * Each of the pdg vertex kind is assigned an unique ID as follows actual-in
	 * 1 actual-out 2 auxiliary 3 body 4 call-site 5 control-point 6 declaration
	 * 7 entry 8 exit 9 expression 10 formal-in 11 formal-out 12
	 * global-actual-in 13 global-actual-out 14 global-formal-in 15
	 * global-formal-out 16 indirect-call 17 jump 18 label 19 return 20
	 * switch-case 21 variable-initialization 22 exceptional-exit 23
	 * exceptional-return 24 normal-exit 25 normal-return 26
	 * 
	 * @author Boya Sun
	 * 
	 */
	protected String vertexKindId;
	/**
	 * The abstract syntax tree of the vertex. The tree is converted to a string
	 * which corresponds to the pre-order traversal of the tree
	 */
	protected String vertexAst;
	/**
	 * The label of the vertex. Label indicates whether the vertex belongs to
	 * the same group or having the same characteristics
	 */
	protected String vertexLabel;
	/**
	 * The code which the vertex represents
	 */
	protected String vertexCharacters;
	/**
	 * Starting line of the code the vertex represents.
	 */
	protected int startline;
	/**
	 * Ending line of the code the vertex represents.
	 */
	protected int endline;
	
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getClassId() {
		return classId;
	}

	public void setClassId(String classId) {
		this.classId = classId;
	}

	public String getPdgId() {
		return pdgId;
	}

	public void setPdgId(String pdgId) {
		this.pdgId = pdgId;
	}

	public String getPdgVertexId() {
		return pdgVertexId;
	}

	public void setPdgVertexId(String pdgVertexId) {
		this.pdgVertexId = pdgVertexId;
	}

	public String getPdgVertexKind() {
		return pdgVertexKind;
	}

	public void setPdgVertexKind(String pdgVertexKind) {
		this.pdgVertexKind = pdgVertexKind;
	}

	public int getVertexKey() {
		return vertexKey;
	}

	public void setVertexKey(int vertexKey) {
		this.vertexKey = vertexKey;
	}

	public String getVertexKindId() {
		return vertexKindId;
	}

	public void setVertexKindId(String vertexKindId) {
		this.vertexKindId = vertexKindId;
	}

	public String getVertexAst() {
		return vertexAst;
	}

	public void setVertexAst(String vertexAst) {
		this.vertexAst = vertexAst;
	}

	public String getVertexLabel() {
		return vertexLabel;
	}

	public void setVertexLabel(String vertexLabel) {
		this.vertexLabel = vertexLabel;
	}

	public String getVertexCharacters() {
		return vertexCharacters;
	}

	public void setVertexCharacters(String vertexCharacters) {
		this.vertexCharacters = vertexCharacters;
	}

	public int getStartline() {
		return startline;
	}

	public void setStartline(int startline) {
		this.startline = startline;
	}

	public int getEndline() {
		return endline;
	}

	public void setEndline(int endline) {
		this.endline = endline;
	}

}
