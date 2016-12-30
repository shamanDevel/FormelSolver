/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.shaman.ds;

import java.util.*;

/**
 * A KNF-Formular representation.
 * @author Sebastian Weiß
 */
public class KNF implements Iterable<KNF.Clause>{
	private final Set<Clause> clauses;
	
	/**
	 * Constructs an empty formular.
	 */
	public KNF() {
		clauses = new LinkedHashSet<>(); //Linked, to store order
	}
	
	/**
	 * Parses the specified formular string. <br>
	 * The syntax is as follows: { {1. Clause}, {2. Clause}, ...}.
	 * Each clause has this syntax: {a, b, c, -d, -e, ...}.
	 * Each variable can be negated by a minus sign (-), has consist of one
	 * letter. Each elements (a literal in a clause, a clause in the formular)
	 * can be seperated by a comma (,) or a semicolon (;). Groups (the formular
	 * and the clauses) are opened and claused with { and }. Each Clause has to 
	 * be enclosed by { and }, even if it contains only one literal. 
	 * Whitespaces are ignored. <br>
	 * Example: {@code { {a,b}, {-b, c}, {a, -c},{-a} }.
	 * @param formular 
	 */
	public KNF(String formular) {
		this();
		//build char queue
		formular = formular.trim();
		Queue<Character> chars = new ArrayDeque<>(formular.length());
		for (int i=0; i<formular.length(); i++) {
			chars.offer(formular.charAt(i));
		}
		if (!isOpenBrace(chars.poll())) {
			throw new IllegalArgumentException("Eine Formel muss mit einer öffnenden geschweiften Klammer { beginnens");
		}
		boolean clauseAdded = false;
		while(!chars.isEmpty()) {
			char c = chars.poll();
			if (isWhitespace(c)) {
				continue;
			} else if (isOpenBrace(c)) {
				if (clauseAdded) {
					throw new IllegalArgumentException("Zwei Klauseln müssen mit , oder ; getrennt werden");
				}
				clauses.add(new Clause(chars)); //reads the input until the next }
				clauseAdded = true;
			} else if (isElementSeperator(c)) {
				if (clauseAdded) {
					clauseAdded = false; //everything is fine
				} else {
					throw new IllegalArgumentException("Die Trennzeichen , und ; dürfen nicht direkt hintereinander stehen");
				}
			} else if (isClosingBrace(c)) {
				if (!chars.isEmpty()) {
					throw new IllegalArgumentException("Formel wurde geschlossen, obwohl noch Zeichen übrig sind");
				}
				return; //end
			} else {
				throw new IllegalArgumentException("Unerlaubtes Zeichen: "+c);
			}
		}
		throw new IllegalArgumentException("Ende der Formel ohne eine schließende Klammer erreicht");
	}
	
	private static boolean isOpenBrace(char c) {
		return c=='{';
	}
	private static boolean isClosingBrace(char c) {
		return c=='}';
	}
	private static boolean isWhitespace(char c) {
		return Character.isWhitespace(c);
	}
	private static boolean isElementSeperator(char c) {
		return c==',' || c==';';
	}
	private static boolean isVariable(char c) {
		return Character.isLetter(c);
	}
	private static boolean isNegation(char c) {
		return c=='-';
	}
	
	/**
	 * Adds a new clause to this formular
	 * @param clause the new clause
	 * @return {@code true} if this formular did not already contain this clause
	 */
	public boolean addClause(Clause clause) {
		return clauses.add(clause);
	}
	
	public boolean containsClause(Clause clause) {
		return clauses.contains(clause);
	}
	
	/**
	 * @return The count of clauses in this formular
	 */
	public int getCount() {
		return clauses.size();
	}
	
	public Clause[] toArray() {
		return clauses.toArray(new Clause[clauses.size()]);
	}

	@Override
	public Iterator<Clause> iterator() {
		return clauses.iterator();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append('{');

		Clause[] array = toArray();
		for (int i=0; i<array.length-1; i++) {
			str.append(array[i]);
			str.append(", ");
		}
		if (array.length>0) {
			str.append(array[array.length-1]);
		}

		str.append('}');
		return str.toString();
	}
	
	public static class Clause implements Iterable<KNF.Literal> {
		private final Set<Literal> literals;

		public Clause() {
			this.literals = new LinkedHashSet<>();
		}
		
		private Clause(Queue<Character> text) {
			this();
			boolean literalAdded = false;
			while(!text.isEmpty()) {
				char c = text.poll();
				if (isWhitespace(c)) {
					continue;
				} else if (isClosingBrace(c)) {
					return; //close this clause
				} else if (isNegation(c) || isVariable(c)) {
					if (literalAdded) {
						throw new IllegalArgumentException("Zwei Literale müssen mit , oder ; getrennt werden");
					}
					literals.add(new Literal(c, text));
					literalAdded = true;
				} else if (isElementSeperator(c)) {
					if (literalAdded) {
						literalAdded = false; //everything is fine
					} else {
						throw new IllegalArgumentException("Die Trennzeichen , und ; dürfen nicht direkt hintereinander stehen");
					}
				} else {
					throw new IllegalArgumentException("Unerlaubtes Zeichen: "+c);
				}
			}
		}
		
		/**
		 * Adds a new literal
		 * @param literal the literal to add
		 * @return {@code true} if this clause did not already contain the literal
		 */
		public boolean addLiteral(Literal literal) {
			return literals.add(literal);
		}
		
		public boolean hasLiteral(Literal literal) {
			return literals.contains(literal);
		}
		
		public boolean removeLiteral(Literal literal) {
			return literals.remove(literal);
		}
		
		/**
		 * @return the count of literals
		 */
		public int getCount() {
			return literals.size();
		}
		
		public Literal[] toArray() {
			return literals.toArray(new Literal[literals.size()]);
		}

		@Override
		public Iterator<Literal> iterator() {
			return literals.iterator();
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 11 * hash + Objects.hashCode(this.literals);
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Clause other = (Clause) obj;
			if (!Objects.equals(this.literals, other.literals)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder str = new StringBuilder();
			str.append('{');
			
			Literal[] array = toArray();
			for (int i=0; i<array.length-1; i++) {
				str.append(array[i]);
				str.append(", ");
			}
			if (array.length>0) {
				str.append(array[array.length-1]);
			}
			
			str.append('}');
			return str.toString();
		}
		
		public Clause copy() {
			Clause c = new Clause();
			for (Literal l : this) {
				c.addLiteral(l);
			}
			return c;
		}
	}

	/**
	 * One Literal in a Clause.
	 * Unmodificable.
	 */
	public static class Literal {
		private final String variable;
		private final boolean negated;

		public Literal(String variable, boolean negated) {
			this.variable = variable;
			this.negated = negated;
		}
		
		private Literal(char c1, Queue<Character> text) {
			char c2;
			if (isNegation(c1)) {
				negated = true;
				c2 = text.poll();
			} else {
				negated = false;
				c2 = c1;
			}
			if (!isVariable(c2)) {
				throw new IllegalArgumentException("Variablenname erwartet, stattdessen war es "+c2);
			}
			variable = String.valueOf(c2);
		}

		public String getVariable() {
			return variable;
		}

		public boolean isNegated() {
			return negated;
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 71 * hash + this.variable.hashCode();
			hash = 71 * hash + (this.negated ? 1 : 0);
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Literal other = (Literal) obj;
			if (!this.variable.equals(other.variable)) {
				return false;
			}
			if (this.negated != other.negated) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			if (negated) {
				return "-" + variable;
			} else {
				return String.valueOf(variable);
			}
		}
		
	}
}
