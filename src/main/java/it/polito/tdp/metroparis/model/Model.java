package it.polito.tdp.metroparis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

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

		System.out.println(
				String.format("Grafo creato: vertici %d - archi %d", graph.vertexSet().size(), graph.edgeSet().size()));

	}

	// VISITA IN AMPIEZZA A PARTIRE DA UN VERTICE (BFS)
	public List<Fermata> visitaAmpiezza(Fermata source) {
		GraphIterator<Fermata, DefaultEdge> bfs = new BreadthFirstIterator<Fermata, DefaultEdge>(graph, source);
		List<Fermata> result = new ArrayList<Fermata>();

		while (bfs.hasNext()) {
			result.add(bfs.next());
		}

		return result;
	}

	// VISITA IN PROFONDITA A PARTIRE DA UN VERTICE (DFS)
	public List<Fermata> visitaProfondita(Fermata source) {
		GraphIterator<Fermata, DefaultEdge> dfs = new DepthFirstIterator<Fermata, DefaultEdge>(graph, source);
		List<Fermata> result = new ArrayList<Fermata>();

		while (dfs.hasNext()) {
			result.add(dfs.next());
		}

		return result;
	}

	public Map<Fermata, Fermata> alberoVisitaAmpiezza(Fermata source) {
		GraphIterator<Fermata, DefaultEdge> bfs = new BreadthFirstIterator<Fermata, DefaultEdge>(graph, source);
		Map<Fermata, Fermata> result = new HashMap<Fermata, Fermata>();
		bfs.addTraversalListener(new TraversalListener<Fermata, DefaultEdge>() {
			@Override
			public void vertexTraversed(VertexTraversalEvent<Fermata> e) {
			}

			@Override
			public void vertexFinished(VertexTraversalEvent<Fermata> e) {
			}

			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultEdge> e) {
				DefaultEdge edge = e.getEdge();
				Fermata a = graph.getEdgeSource(edge);
				Fermata b = graph.getEdgeTarget(edge);
				if (result.containsKey(a)) {
					result.put(b, a);
				} else {
					result.put(a, b);
				}
			}

			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
			}

			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
			}
		});

		while (bfs.hasNext()) {
			bfs.next();
		}

		return result;
	}

	public List<Fermata> percorsoMinimo(Fermata partenza, Fermata arrivo) {
		DijkstraShortestPath<Fermata, DefaultEdge> d = new DijkstraShortestPath<Fermata, DefaultEdge>(graph);

		GraphPath<Fermata, DefaultEdge> result = d.getPath(partenza, arrivo);
		return result.getVertexList();
	}

	public static void main(String args[]) {
		Model m = new Model();

		// List<Fermata> r = m.visitaAmpiezza(m.fermate.get(0));
		// Map<Fermata, Fermata> r2 = m.alberoVisitaAmpiezza(m.fermate.get(0));

		List<Fermata> res = m.percorsoMinimo(m.fermate.get(0), m.fermate.get(10));
		res.forEach(x -> System.out.println(x.getNome()));
	} 
}
