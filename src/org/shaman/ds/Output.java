/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.shaman.ds;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Interface for handling output messages.
 * They flush automatically.
 * @author Sebastian Weiß
 */
public abstract class Output {
	public abstract void print(Object obj);
	public abstract void println(Object obj);
	public abstract void close();
	
	public static Output create(final PrintStream stream) {
		return new Output() {

			@Override
			public void print(Object obj) {
				stream.print(obj);
			}

			@Override
			public void println(Object obj) {
				stream.println(obj);
			}

			@Override
			public void close() {
				stream.close();
			}
			
		};
	}
	
	public static Output create(final OutputStream stream) {
		return create(new PrintStream(stream));
	}
}
