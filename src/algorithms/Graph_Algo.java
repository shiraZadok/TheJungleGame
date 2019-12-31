package algorithms;

import dataStructure.*;
import utils.Point3D;
import java.io.*;
import java.util.*;

/**
 * This empty class represents the set of graph-theory algorithms
 * which should be implemented as part of Ex2 - Do edit this class.
 * @author
 *
 */
public class Graph_Algo implements graph_algorithms, Serializable {

	public graph algo = new DGraph();

	@Override
	public void init(graph g) {
		this.algo=g;
	}

	@Override
	public void init(String file_name) {
		try {
			FileInputStream file = new FileInputStream(file_name);
			ObjectInputStream in = new ObjectInputStream(file);
			this.algo = (graph) in.readObject();
			in.close();
			file.close();

			System.out.println("Object has been deserialized");
		}
		catch(IOException ex) {
			System.out.println("IOException is caught");
		}
		catch(ClassNotFoundException ex) {
			System.out.println("ClassNotFoundException is caught");
		}
	}

	@Override
	public void save(String file_name) {
		try {
			FileOutputStream file = new FileOutputStream(file_name);
			ObjectOutputStream out = new ObjectOutputStream(file);
			out.writeObject(this.algo);
			out.close();
			file.close();

			System.out.println("Object has been serialized");
		}
		catch (IOException ex) {
			System.out.println("IOException is caught");
		}
	}

	@Override
	public boolean isConnected() {
		this.changeTagNode();
		Iterator it = this.algo.getV().iterator();
		dfs((node_data) it.next());
		it = this.algo.getV().iterator();
		while (it.hasNext()) {
			node_data temp = (node_data) it.next();
			if (temp.getTag() == 0) return false;
		}
		this.changeTagEdge();
		this.changeTagNode();
		oppositeDest(this.algo);
		it = this.algo.getV().iterator();
		dfs((node_data) it.next());
		it = this.algo.getV().iterator();
		while (it.hasNext()) {
			node_data temp = (node_data) it.next();
			if (temp.getTag() == 0) return false;
		}
		oppositeDest(this.algo);
		return true;
	}

	public void dfs(node_data n) {
		if (this.algo.getE(n.getKey()) != null) {
			Iterator it = this.algo.getE(n.getKey()).iterator();
			while (it.hasNext()) {
				edge_data e = (edge_data) it.next();
				node_data dest = this.algo.getNode(e.getDest());
				if (dest.getTag() == 0) {
					dest.setTag(1);
					dfs(dest);
				}
			}
		}
	}

	public void oppositeDest(graph d) {
		Iterator it = d.getV().iterator();
		while (it.hasNext()) {
			node_data n = (node_data) it.next();
			if (d.getE(n.getKey()) != null) {
				Iterator it1 = d.getE(n.getKey()).iterator();
				while (it1.hasNext()) {
					edge_data e = (edge_data) it1.next();
					if (e.getTag() == 0) {
						if (d.getEdge(e.getDest(), e.getSrc()) != null) {
							Edge temps = new Edge((Edge) d.getEdge(e.getSrc(), e.getDest()));
							double tempWeight1 = d.getEdge(e.getSrc(), e.getDest()).getWeight();
							double tempWeight2 = d.getEdge(e.getDest(), e.getSrc()).getWeight();
							d.connect(e.getSrc(), e.getDest(), tempWeight2);
							d.connect(temps.getDest(), temps.getSrc(), tempWeight1);
							d.getEdge(temps.getDest(), temps.getSrc()).setTag(1);
							d.getEdge(temps.getSrc(), temps.getDest()).setTag(1);
							it1 = d.getE(n.getKey()).iterator();
						}
						else {
							d.connect(e.getDest(), e.getSrc(), e.getWeight());
							d.removeEdge(e.getSrc(), e.getDest());
							d.getEdge(e.getDest(), e.getSrc()).setTag(1);
							it1 = d.getE(n.getKey()).iterator();
						}
					}
				}
			}
		}
	}



	public void changeTagNode() {
		Iterator it = this.algo.getV().iterator();
		while (it.hasNext()) {
			node_data n = (node_data) it.next();
			n.setTag(0);
		}
	}

	public void changeTagEdge() {
		Iterator it = this.algo.getV().iterator();
		while (it.hasNext()) {
			node_data n = (node_data) it.next();
			if (this.algo.getE(n.getKey()) != null) {
				Iterator itEdge = this.algo.getE(n.getKey()).iterator();
				while (itEdge.hasNext()) {
					edge_data e = (edge_data) itEdge.next();
					e.setTag(0);
				}
			}
		}
	}

	@Override
	public double shortestPathDist(int src, int dest) {
		this.changeTagNode();
		Iterator it = this.algo.getV().iterator();
		while (it.hasNext()) {
			node_data n = (node_data) it.next();
			n.setWeight(Integer.MAX_VALUE);
		}
		this.algo.getNode(src).setWeight(0);
		shortestPathDistRec(this.algo.getNode(src), this.algo.getNode(dest));
		return this.algo.getNode(dest).getWeight();
	}

	public void shortestPathDistRec(node_data n, node_data dest) {
		if (n.getTag() == 1 || n.getKey() == dest.getKey()) {
			return;
		}
		if (this.algo.getE(n.getKey()) != null) {
			Iterator it = this.algo.getE(n.getKey()).iterator();
			while (it.hasNext()) {
				edge_data e = (edge_data) it.next();
				node_data d = this.algo.getNode(e.getDest());
				if (n.getWeight() + e.getWeight() < d.getWeight()) {
					d.setWeight(n.getWeight() + e.getWeight());
					n.setTag(1);
					d.setInfo(""+n.getKey());
					shortestPathDistRec(this.algo.getNode(e.getDest()), dest);
				}
			}
		}
	}

	@Override
	public List<node_data> shortestPath(int src, int dest) {
		List<node_data> ans = new LinkedList<>();
		this.shortestPathDist(src,dest);
		graph tempGraph = this.copy();
		node_data min = tempGraph.getNode(dest);
		oppositeDest(tempGraph);
		ans.add(min);
		while (min.getKey()!=src){
			Collection<edge_data> coll =tempGraph.getE(min.getKey());
			if(coll!=null) {
				for (edge_data e : coll) {
					node_data temp = tempGraph.getNode(e.getDest());
					if (temp.getWeight() + e.getWeight() == min.getWeight()) {
						min = temp;
					}
				}
			}
			ans.add(min);
		}
		List<node_data> temp = new LinkedList<>();
		for (int i = ans.size()-1; i >=0 ; i--) {
			temp.add(ans.get(i));
		}
		ans=temp;
		return ans;
	}

	@Override
	public List<node_data> TSP(List<Integer> targets) {
		List<node_data> ans = new LinkedList<>();
		double tempshortestPath = 0;
		int tempk1 = 0;
		int tempk2 = 0;
		int k1 = targets.get(0);
		while (targets.size()!=0) {
			for (int j = 0; j < targets.size(); j++) {
				double minshortestPath = Integer.MAX_VALUE;
				int k2 = targets.get(j);
				if (k1 != k2) {
					tempshortestPath = minshortestPath;
					if (this.shortestPathDist(k1, k2) == Integer.MAX_VALUE) return null;
					minshortestPath = Math.min(minshortestPath, this.shortestPathDist(k1, k2));
					if (tempshortestPath != minshortestPath) {
						tempk1 = k1;
						tempk2 = k2;
					}
				}
			}
			List<node_data> add = this.shortestPath(tempk1, tempk2);
			k1 = tempk2;
			for (int j = 0; j < add.size(); j++) {
				node_data n = add.get(j);
				if (ans.size() == 0 || !ans.get(ans.size() - 1).equals(n)) {
					ans.add(n);
				}
				for (int k = 0; k < targets.size(); k++) {
					int k4 = targets.get(k);
					if (n.getKey() != tempk2 && n.getKey() == k4) {
						targets.remove(k);
					}
				}
			}
		}
		return ans;
}

	@Override
	public graph copy() {
		graph ans = new DGraph();
		for (node_data n : this.algo.getV()) {
			node_data temp = new Node((Node) n);
			ans.addNode(temp);
		}
		for (node_data n : ans.getV()) {
			Collection<edge_data> coll = this.algo.getE(n.getKey());
			if(coll!=null) {
				for (edge_data e : this.algo.getE(n.getKey())) {
					edge_data temp = new Edge((Edge) e);
					ans.connect(temp.getSrc(), temp.getDest(), temp.getWeight());
				}
			}
		}
		return ans;
	}

	public static void main(String[] args) {
//		Graph_Algo G = new Graph_Algo();
//		Point3D x = new Point3D(1,4,0);
//		Point3D y = new Point3D(2,5,0);
//		Point3D q = new Point3D(3,6,0);
//		Point3D z = new Point3D(4,7,0);
//		Point3D s = new Point3D(5,8,0);
//		Point3D t = new Point3D(6,9,0);
//		node_data a = new Node(1,2,3, "asf", x);
//		node_data b =new Node(3,4,6,"gik",y);
//		node_data c = new Node(5,50,50,"sf",q);
//		node_data d = new Node(6,50,50,"sf",z);
//		node_data e = new Node(7,50,50,"sf",s);
//		node_data f = new Node(8,50,50,"sf",t);
//		DGraph g =new DGraph();
//		g.addNode(a);
//		g.addNode(b);
//		g.addNode(c);
//		g.addNode(d);
//		g.addNode(e);
//		g.addNode(f);
//		g.connect(a.getKey(),b.getKey(),5);
//		g.connect(b.getKey(),f.getKey(),3);
//		g.connect(a.getKey(),c.getKey(),4);
//		g.connect(c.getKey(),d.getKey(),5);
//		g.connect(c.getKey(),e.getKey(),1);
//		g.connect(e.getKey(),f.getKey(),1);
//		G.algo=g;
//		boolean be =G.isConnected();
//		System.out.println(G.shortestPathDist(a.getKey(),c.getKey()));
//		System.out.println(G.shortestPathDist(a.getKey(),f.getKey()));
//		Graph_Algo g = new Graph_Algo();
//		Point3D x = new Point3D(1, 4, 0);
//		Point3D y = new Point3D(2, 5, 0);
//		Point3D q = new Point3D(4, 3, 0);
//		node_data a = new Node(1, 2, 3, "asf", x);
//		node_data b = new Node(3, 4, 6, "gik", y);
//		node_data c = new Node(5, 50, 50, "sf", q);
//		DGraph d = new DGraph();
//		d.addNode(a);
//		d.addNode(b);
//		d.addNode(c);
//		d.connect(a.getKey(), b.getKey(), 4);
//		d.connect(b.getKey(), c.getKey(), 50);
//		d.connect(b.getKey(), a.getKey(), 4);
//		d.connect(c.getKey(), b.getKey(), 4);
//		g.algo = d;
//		boolean f = g.isConnected();
//		System.out.println(f);
//		g.save("test1");
//		g.init("test1");

		Point3D x = new Point3D(14,4,0);
		Point3D x2 = new Point3D(-75,14,0);
		Point3D x3 = new Point3D(80,5,0);
		Point3D x4 = new Point3D(1,4,0);
		Point3D x5 = new Point3D(-5,1,0);
		Point3D x6 = new Point3D(8,3,0);
		Point3D x7 = new Point3D(4,1,0);
		Point3D x8 = new Point3D(75,14,0);
		node_data a1 = new Node(x);
		node_data a2 = new Node(x2);
		node_data a3 = new Node(x3);
		node_data a4 = new Node(x4);
		node_data a5 = new Node(x5);
		node_data a6 = new Node(x6);
		node_data a7 = new Node(x7);
		node_data a8 = new Node(x8);
		DGraph d = new DGraph();
		d.addNode(a1);
		d.addNode(a2);
		d.addNode(a3);
		d.addNode(a4);
		d.addNode(a5);
		d.addNode(a6);
		d.addNode(a7);
		d.addNode(a8);
		d.connect(a1.getKey(),a2.getKey(),5);
		d.connect(a1.getKey(),a5.getKey(),2);
		d.connect(a1.getKey(),a3.getKey(),6);
		d.connect(a1.getKey(),a6.getKey(),5);
		d.connect(a3.getKey(),a4.getKey(),7);
		d.connect(a2.getKey(),a8.getKey(),8);
		d.connect(a2.getKey(),a7.getKey(),3);
		d.connect(a5.getKey(),a1.getKey(),5);
		d.connect(a5.getKey(),a6.getKey(),2);
		d.connect(a6.getKey(),a1.getKey(),3);
		d.connect(a6.getKey(),a5.getKey(),3);
		d.connect(a6.getKey(),a7.getKey(),3);
		d.connect(a7.getKey(),a6.getKey(),3);
		Graph_Algo p = new Graph_Algo();
		p.algo=d;
		List<Integer> r = new LinkedList<>();
		r.add(a1.getKey());
		r.add(a6.getKey());
		r.add(a5.getKey());
		List<node_data> ans = p.TSP(r);
		//List<node_data> ans1 = p.shortestPath(1,6);
		//List<node_data> ans2 = p.shortestPath(5,6);
		//List<Integer> ans3 = new LinkedList<>();
		//ans3.add(1);
		//ans3.add(5);
		//System.out.println(d.getNode(7).getWeight());
		System.out.println(ans);
		//System.out.println(ans1);
		//System.out.println(ans2);
		//System.out.println(ans3);

//        Point3D x = new Point3D(1,4,0);
//        Point3D y = new Point3D(2,5,0);
//        Point3D q = new Point3D(4,3,0);
//        node_data a = new Node(x);
//        node_data b =new Node(y);
//        node_data c = new Node(q);
//        DGraph d =new DGraph();
//        Graph_Algo p = new Graph_Algo();
//        d.addNode(a);
//        d.addNode(b);
//        d.addNode(c);
//        d.connect(a.getKey(),b.getKey(),4);
//        d.connect(a.getKey(),c.getKey(),50);
//        d.connect(b.getKey(),c.getKey(),4);
//        p.algo = d;
//		boolean f = p.isConnected();
//		System.out.println(f);
	}
}
