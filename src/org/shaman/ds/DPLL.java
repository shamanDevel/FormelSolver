/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.shaman.ds;

import java.io.PrintStream;
import java.util.*;

/**
 * DPLL algorithm
 * @author Sebastian Weiß
 */
public class DPLL {
	public static final String F = "F";
	public static final String SPACES = "                                                                                                           ";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final KNF.Literal TRUE_LITERAL = new KNF.Literal(TRUE, false);
	public static final KNF.Literal FALSE_LITERAL = new KNF.Literal(FALSE, false);
	
	private final Deque<KNF.Literal> allocation;
	private final Set<Set<KNF.Literal>> allocations;
	private Output debug;
	private boolean goOn;

	public DPLL() {
		allocation = new ArrayDeque<>();
		allocations = new LinkedHashSet<>();
	}

	/**
	 * Executes the DPLL algorithm
	 * @param knf the knf formular
	 * @param debug a debug stream or {@code null}
	 * @param goOn set to true, if the algoritm should go on after it detects a
	 * fullfilling allocation. It then prints all allocations.
	 * @return a set with all fullfilling allocations. Is empty or contains one
	 * allocation if {@code goOn} is {@code false}. If goOn is set to true,
	 * this list can contain multiple elements. Each allocation is described by
	 * a set of literals.
	 */
	public synchronized Set<Set<KNF.Literal>> doDPLL(KNF knf, Output debug, boolean goOn) {
		allocation.clear();
		allocations.clear();
		this.debug = debug;
		this.goOn = goOn;
		run(knf);
		return allocations;
	}
	
	private boolean run(KNF knf) {
		//find next literal
		//search for one literal clauses
		for (KNF.Clause c : knf) {
			if (c.getCount()==1) {
				//we found an one literal clause
				KNF.Literal l = c.iterator().next();
				int ret = doStep(knf, l);
				if (ret==0) {
					return false; //equals false
				} else if (ret==1) {
					return true; //equals true
				}
				//continue
			}
		}
		//pick the first literal
		KNF.Clause c = knf.iterator().next();
		KNF.Literal l1 = c.iterator().next();
		KNF.Literal l2 = new KNF.Literal(l1.getVariable(), !l1.isNegated());
		//set to true
		int ret1 = doStep(knf, l1);
		//set to false
		int ret2 = doStep(knf, l2);
		
		return (ret1==1) || (ret2==1);

	}
	
	private int doStep(KNF knf, KNF.Literal l) {
		SubFormular f = createSubFormular(knf, l, allocation.size());
		//push literal
		allocation.addLast(l);
		//evaluate results
		if (isTrue(f)) {
			if (debug!=null) {
				KNF.Literal[] literals = allocation.toArray(new KNF.Literal[allocation.size()]);
				System.out.println("Erfüllende Belegung gefunden: "+Arrays.toString(literals));
			}
			allocations.add(new LinkedHashSet<>(allocation));
			if (goOn) {
				allocation.removeLast();
				return 0; //simulate false, so the algorithm will go on with other allocations
			}
			return 1; //success
		} else if (isFalse(f)) {
			//not fullfillable
			//pop literal
			allocation.removeLast();
			return 0; //fail
		} else {
			//recursion
			boolean ret = run(f);
			if (ret==true) {
				return 1; //success
			}
			//pop literal
			allocation.removeLast();
		}
		return 2; //continue
	}
	
	private static boolean isTrue(KNF knf) {
		return knf.getCount()==0;
	}
	
	private static boolean isFalse(KNF knf) {
		for (KNF.Clause c : knf) {
			if (c.getCount()==0) {
				return true;
			}
		}
		return false;
	}
	
	private SubFormular createSubFormular(KNF knf, KNF.Literal l, int layer) {
		//replace any existes of the literal with true, false
		KNF.Literal nl = new KNF.Literal(l.getVariable(), !l.isNegated());
		KNF k1 = new KNF();
		for (KNF.Clause c : knf) {
			KNF.Clause c2 = new KNF.Clause();
			k1.addClause(c2);
			for (KNF.Literal lit : c) {
				if (lit.equals(l)) {
					//replace with true
					c2.addLiteral(TRUE_LITERAL);
				} else if (lit.equals(nl)) {
					//replace with false
					c2.addLiteral(FALSE_LITERAL);
				} else {
					//keep it
					c2.addLiteral(lit);
				}
			}
		}
		
		//simplify it
		SubFormular k2 = new SubFormular(l, knf);
		for (KNF.Clause c : k1) {
			KNF.Clause c2 = c.copy();
			//remove false literal
			c2.removeLiteral(FALSE_LITERAL);
			//check if one clause is true
			if (c2.hasLiteral(TRUE_LITERAL)) {
				//delete it
				continue;
			}
			k2.addClause(c2);
		}
		
		if (debug!=null) {
			debug.print(SPACES.substring(0, layer*2));
			debug.print(F+"["+l.getVariable()+"/"+(l.isNegated() ? FALSE : TRUE)+"]: ");
			debug.println(k1);
			debug.print(SPACES.substring(0, (layer+1)*2));
			debug.print("= ");
			debug.println(k2);
		}
		
		return k2;
	}
	
	private static class SubFormular extends KNF {
		private KNF.Literal removedLiteral;
		private final KNF parent;

		public SubFormular(Literal removedLiteral, KNF parent) {
			this.removedLiteral = removedLiteral;
			this.parent = parent;
		}

	}
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
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
//				+ "{d} }";
		String formular = "{{-p,q,-r,s},{-q,-r,s},{r},{-p,s},{-p,r}}";
		
		KNF knf = new KNF(formular);
		System.out.println("Eingabeformel: "+formular);
		System.out.println("Gelesene Formel: "+knf);
		System.out.println();
		
		final boolean goOn = true;
		DPLL dpll = new DPLL();
		Set<Set<KNF.Literal>> allocations = dpll.doDPLL(knf, Output.create(System.out), goOn);
		
		System.out.println("\nErfüllende Belegungen ("+allocations.size()+" Stück):");
		for (Set<KNF.Literal> literals : allocations) {
			System.out.println(Arrays.toString(literals.toArray()));
		}
	}
	
}
