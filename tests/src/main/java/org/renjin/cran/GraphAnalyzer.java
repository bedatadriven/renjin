package org.renjin.cran;

import java.util.Collections;
import java.util.List;

import org.apache.commons.collections15.comparators.ComparatorChain;
import org.renjin.cran.PackageDescription.PackageDependency;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import edu.uci.ics.jung.algorithms.scoring.EigenvectorCentrality;
import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class GraphAnalyzer extends CranVisitor {

	public static void main(String[] args) {
		CranLocalExplorer explorer = new CranLocalExplorer();
		explorer.accept(CranIntegrationTests.getPackageRoot(), new GraphAnalyzer());
	}
	
	private DirectedGraph<PackageNode, Edge> graph = 
			new DirectedSparseGraph<GraphAnalyzer.PackageNode, GraphAnalyzer.Edge>();
	
	@Override
	public CranPackageVisitor visitPackage(String name) {
		return new PackageVisitor();
	}
	
	@Override
	public void visitComplete() {
		final PageRank<PackageNode, Edge> scorer = new PageRank<GraphAnalyzer.PackageNode, GraphAnalyzer.Edge>(graph, 0.15);
		scorer.evaluate();
		List<PackageNode> packages = Lists.newArrayList(graph.getVertices());
		Collections.sort(packages, Ordering.natural().reverse().onResultOf(new Function<PackageNode, Double>() {

			@Override
			public Double apply(PackageNode input) {
				return scorer.getVertexScore(input);
			}
		}));	
		
		for(PackageNode p : packages) {
			System.out.println(p.name + " = " + scorer.getVertexScore(p));
		}
	}



	private class PackageNode {
		private String name;

		public PackageNode(String name) {
			super();
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((name == null) ? 0 : name.hashCode());
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
			PackageNode other = (PackageNode) obj;
			return Objects.equal(name, other.name);
		}

		private GraphAnalyzer getOuterType() {
			return GraphAnalyzer.this;
		}
	}
	
	private class Edge {
		
	}
	
	private class Depends extends Edge {
		@Override
		public String toString() {
			return "depends";
		}
	}
	
	private class Suggests extends Edge {
		@Override
		public String toString() {
			return "suggests";
		}
	}
	
	private class PackageVisitor extends CranPackageVisitor {
		
		@Override
		protected void visitDescription(PackageDescription description) {
			for(PackageDependency dependency : description.getDepends()) {
				if(!dependency.getName().equals("R")) {
					graph.addEdge(new Depends(), new PackageNode(description.getPackage()),
							new PackageNode(dependency.getName()));
				}
			}
		}
	}

}
