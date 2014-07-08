package edu.cwru.eecs.statianalysis.to;

public class PatternEdgesPo {

	private int patternKey;
	private int srcVertexKey;
	private int srcVertexIdx;
	private int tarVertexKey;
	private int tarVertexIdx;
	private String edgeType;

	public int getPatternKey() {
		return patternKey;
	}

	public void setPatternKey(int patternKey) {
		this.patternKey = patternKey;
	}

	public int getSrcVertexKey() {
		return srcVertexKey;
	}

	public void setSrcVertexKey(int srcVertexKey) {
		this.srcVertexKey = srcVertexKey;
	}

	public int getSrcVertexIdx() {
		return srcVertexIdx;
	}

	public void setSrcVertexIdx(int srcVertexIdx) {
		this.srcVertexIdx = srcVertexIdx;
	}

	public int getTarVertexKey() {
		return tarVertexKey;
	}

	public void setTarVertexKey(int tarVertexKey) {
		this.tarVertexKey = tarVertexKey;
	}

	public int getTarVertexIdx() {
		return tarVertexIdx;
	}

	public void setTarVertexIdx(int tarVertexIdx) {
		this.tarVertexIdx = tarVertexIdx;
	}

	public String getEdgeType() {
		return edgeType;
	}

	public void setEdgeType(String edgeType) {
		this.edgeType = edgeType;
	}

}
