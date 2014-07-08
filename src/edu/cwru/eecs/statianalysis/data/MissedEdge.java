package edu.cwru.eecs.statianalysis.data;

public class MissedEdge <V extends Vertex, E extends Edge<V>> extends Edge<V> {
	
	boolean srcMissed = false;
	boolean tarMissed = false;
	V matchedSrc;
	V matchedTar;
	E matchedEdgeE;
	public boolean isSrcMissed() {
		return srcMissed;
	}
	public void setSrcMissed(boolean srcMissed) {
		this.srcMissed = srcMissed;
	}
	public boolean isTarMissed() {
		return tarMissed;
	}
	public void setTarMissed(boolean tarMissed) {
		this.tarMissed = tarMissed;
	}
	public V getMatchedSrc() {
		return matchedSrc;
	}
	public void setMatchedSrc(V matchedSrc) {
		this.matchedSrc = matchedSrc;
	}
	public V getMatchedTar() {
		return matchedTar;
	}
	public void setMatchedTar(V matchedTar) {
		this.matchedTar = matchedTar;
	}
	public E getMatchedEdgeE() {
		return matchedEdgeE;
	}
	public void setMatchedEdgeE(E matchedEdgeE) {
		this.matchedEdgeE = matchedEdgeE;
	}
	
	

}
