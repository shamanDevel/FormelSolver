/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.shaman.ds;

import java.io.PrintStream;
import java.util.Deque;
import java.util.LinkedList;

/**
 *
 * @author Sebastian Weiß
 */
public class Resolution {
	
	/**
	 * Executes the resolution algorithm on the KNF-formular.
	 * @param knf the knf forumal
	 * @param debug a debug stream or {@null}
	 * @return true, if the formular is unfullfillable
	 */
	public static boolean doResolution(KNF knf, Output debug) {
		//check if knf already contains an empty clause
		for (KNF.Clause c : knf) {
			if (c.getCount()==0) {
				return true;
			}
		}
		//do resolution
		while(true) {
			//create clause
			ResolutionClause c = newClause(knf);
			if (c==null) {
				if (debug!=null) {
					debug.println("Es können keine neuen Resolventen erzeugt werden.");
					debug.println("Gesammtanzahl der Klauseln: "+knf.getCount());
				}
				return false;
			} else if (c.getCount()==0) {
				if (debug!=null) {
					debug.println(c.print());
					debug.println("Leere Klausel gefunden!\n");
					debug.println("Gesamtanzahl der Klauseln: "+knf.getCount());
					printResults(c, debug);
				}
				return true;
			} else {
				if (debug!=null) {
					debug.println(c.print());
				}
				knf.addClause(c);
				continue;
			}
		}
	}
	
	private static ResolutionClause newClause(KNF knf) {
		//iterate through all clauses and literals
		for (KNF.Clause c : knf) {
			for (KNF.Literal l1 : c) {
				//search for a negation of this literal
				KNF.Literal l2 = new KNF.Literal(l1.getVariable(), !l1.isNegated());
				for (KNF.Clause c2 : knf) {
					if (c!=c2) {
						if (c2.hasLiteral(l2)) {
							//we have found our resolution pair, create it
							ResolutionClause res = new ResolutionClause(c, c2, l1, l2);
							//check for equals true
							if (checkIfTrue(res)) {
								continue; //equals true
							}
							//check, if it not already in the knf
							if (!knf.containsClause(res)) {
								return res;
							}
						}
					}
				}
			}
		}
		return null; //no clause created
	}
	
	//checks, if this clause is equals to true (contains e.g. -r and r), then it returns true
	private static boolean checkIfTrue(KNF.Clause clause) {
		for (KNF.Literal l : clause) {
			KNF.Literal l2 = new KNF.Literal(l.getVariable(), !l.isNegated());
			if (clause.hasLiteral(l2)) {
				return true;
			}
		}
		return false;
	}
	
	private static void printResults(ResolutionClause emptyClause, Output out) {
		Deque<String> str = new LinkedList<>();
		printResults(emptyClause, str);
		while(!str.isEmpty()) {
			String s = str.removeFirst();
			out.println(s);
		}
	}
	private static void printResults(ResolutionClause clause, Deque<String> str) {
		str.addFirst(clause.print());
		
		if (clause.getParent1() instanceof ResolutionClause) {
			printResults((ResolutionClause) clause.getParent1(), str);
		}
		if (clause.getParent2() instanceof ResolutionClause) {
			printResults((ResolutionClause) clause.getParent2(), str);
		}
	}
	
	private static class ResolutionClause extends KNF.Clause {
		private final KNF.Clause parent1, parent2;
		
		private ResolutionClause(KNF.Clause p1, KNF.Clause p2, KNF.Literal l1, KNF.Literal l2) {
			this.parent1 = p1;
			this.parent2 = p2;
			//merge clauses
			for (KNF.Literal lit : p1) {
				if (!lit.equals(l1)) {
					addLiteral(lit);
				}
			}
			for (KNF.Literal lit : p2) {
				if (!lit.equals(l2)) {
					addLiteral(lit);
				}
			}
		}

		public KNF.Clause getParent1() {
			return parent1;
		}

		public KNF.Clause getParent2() {
			return parent2;
		}
		
		private String print() {
			return parent1 + " + " + parent2 + " --> " + this;
		}
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
//		String formular = "{ {a,b}, {-b, c}, {a, -c},{-a}}";
		String formular = "{ "
				+ "{g,h,n,d,r,m,p}, "
				+ "{-h,-g}, "
				+ "{-g,r,d}, "
				+ "{n,g,m,d}, "
				+ "{p,-d}, "
				+ "{-g,p,h},"
				+ "{-g,-p,-h},"
				+ "{-r,h,g},"
				+ "{-r,-h,-g},"
				+ "{-m,r},"
				+ "{-p,n},"
				+ "{-n,-r},"
				+ "{-n,g},"
				
				+ "{-d} }";

//		String formular = "{ "
//				+ "{g,h,n,d,r,m,p}, "
//				+ "{-h,-g}, "
//				+ "{-g,r,d}, "
//				+ "{n,g,m,d}, "
//				+ "{p,-d}, "
//				+ "{-g,p,h},"
//				+ "{-g,-p,-h},"
//				+ "{-r,n,g},"
//				+ "{-r,-n,-g},"
//				+ "{-m,r},"
//				+ "{-p,n},"
//				+ "{-n,-r},"
//				+ "{-n,g},"
//				
//				+ "{-d} }";
				
		KNF knf = new KNF(formular);
		System.out.println("Eingegebene Formel: "+formular);
		System.out.println("Gelesene Formel: "+knf);
		System.out.println();
		
		Output out = Output.create(System.out);
		boolean ret = Resolution.doResolution(knf, out);
		if (ret==true) {
			System.out.println("Formel unerfüllbar");
		} else {
			System.out.println("Formel erfüllbar");
		}
	}
	
}
