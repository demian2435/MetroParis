package it.polito.tdp.metroparis.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import it.polito.tdp.metroparis.db.MetroDAO;

public class Model {

	private Graph<Fermata, DefaultEdge> graph;
	private List<Fermata> fermate;
	private Map<Integer, Fermata> fermateIdMap;

	public Model() {
		this.graph = new SimpleDirectedGraph<>(DefaultEdge.class);

		// INSERIMENTO VERTICI GRAFO
		MetroDAO dao = new MetroDAO();
		this.fermate = dao.getAllFermate();
		this.fermateIdMap = new HashMap<>();

		for (Fermata f : this.fermate) {
			fermateIdMap.put(f.getIdFermata(), f);
		}

		Graphs.addAllVertices(this.graph, this.fermate);

		// INSERIMENTO ARCHI GRAFO - metodo 1 (controllo ogni possibile incorcio)
//		for (Fermata fp : this.fermate) {
//			for (Fermata fa : this.fermate) {
//				if (dao.fermateConnesse(fp, fa)) {
//					this.graph.addEdge(fa, fa);
//				}
//			}
//		}

		// INSERIMENTO ARCHI GRAFO - metodo 2 (fermate a partire da ogni fermata)
//		for (Fermata fp : fermate) {
//			List<Fermata> connesse = dao.fermateConnesseFromStazione(fp, fermateIdMap);
//			for (Fermata fa : connesse) {
//				this.graph.addEdge(fp, fa);
//			}
//		}

		// INSERIMENTO ARCHI GRAFO - metodo 3 (chiedo direttamente alla tabella)
		List<CoppiaFermate> coppie = dao.coppieFermate(fermateIdMap);
		for (CoppiaFermate c : coppie) {
			this.graph.addEdge(c.getFp(), c.getFa());
		}
	}
}
