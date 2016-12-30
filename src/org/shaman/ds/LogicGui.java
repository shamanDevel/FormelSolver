/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.shaman.ds;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 *
 * @author Sebastian Weiß
 */
public class LogicGui extends javax.swing.JFrame {

	/**
	 * the current phase of execution
	 * <ol>
	 * <li>Waiting for a correct formular</li>
	 * <li>Waiting for selecting an algorithm</li>
	 * <li>Executing an algorithm</li>
	 * </ol>
	 */
	private int phase = 1;
	
	private KNF knf;
	private Worker worker;
	
	/**
	 * Creates new form LogicGui
	 */
	public LogicGui() {
		initComponents();
		switchPhase(1);
	}

	private void switchPhase(int phase) {
		this.phase = phase;
		switch (phase) {
			case 1: //wait for formular
				formularButton.setEnabled(true);
				formularTextField.setEnabled(true);
				dpllButton.setEnabled(false);
				dpllExtButton.setEnabled(false);
				resolutionButton.setEnabled(false);
				stopButton.setEnabled(false);
				intermediateResultsCheckBox.setEnabled(false);
				break;
			case 2: //wait for algorithm
				formularButton.setEnabled(true);
				formularTextField.setEnabled(true);
				dpllButton.setEnabled(true);
				dpllExtButton.setEnabled(true);
				resolutionButton.setEnabled(true);
				stopButton.setEnabled(false);
				intermediateResultsCheckBox.setEnabled(true);
				break;
			case 3: //execute algorithm
				formularButton.setEnabled(false);
				formularTextField.setEnabled(false);
				dpllButton.setEnabled(false);
				dpllExtButton.setEnabled(false);
				resolutionButton.setEnabled(false);
				stopButton.setEnabled(true);
				intermediateResultsCheckBox.setEnabled(false);
				break;
		}
	}
	
	private class Worker extends Thread {
		/**
		 * the algorithm:
		 * <ol>
		 * <li>Resolution</li>
		 * <li>DPLL</li>
		 * <li>DPLL with all allocations</li>
		 * </ol>
		 */
		private final int algorithm;
		private final Output out;

		private Worker(int algorithm, Output out) {
			this.algorithm = algorithm;
			this.out = out;
		}

		@Override
		public void run() {
			Output debug = null;
			if (intermediateResultsCheckBox.isSelected()) {
				debug = out;
			}
			out.println("Formel: "+knf+"\n");
			try {
				switch (algorithm) {
					case 1:
						boolean ret = Resolution.doResolution(knf, debug);
						if (ret) {
							out.println("\nFormel unerfüllbar");
						} else {
							out.println("\nFormel erfüllbar");
						}
						break;
					case 2:
						doDPLL(knf, out, debug, false);
						break;
					case 3:
						doDPLL(knf, out, debug, true);
						break;
				}
			} finally {
				out.close();
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						switchPhase(2);
					}
				});
				
			}
		}
		
		private void doDPLL(KNF knf, Output ps, Output debug, boolean goOn) {
			DPLL dpll = new DPLL();
			Set<Set<KNF.Literal>> allocations = dpll.doDPLL(knf, debug, goOn);
			if (allocations.isEmpty()) {
				ps.println("\nKeine erfüllende Belegung gefunden,");
				ps.println("Formel ist unerfüllbar");
			}
			ps.println("\nErfüllende Belegungen ("+allocations.size()+" Stück):");
			for (Set<KNF.Literal> literals : allocations) {
				ps.println(Arrays.toString(literals.toArray()));
			}
		}
	}
	
	private class TextAreaOutput extends Output {

		private final JTextArea textArea;

		public TextAreaOutput(final JTextArea textArea) {
			this.textArea = textArea;
		}

		@Override
		public void close() {
		}

		@Override
		public void print(final Object obj) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					textArea.append(String.valueOf(obj));
				}
			});
		}

		@Override
		public void println(final Object obj) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					textArea.append(String.valueOf(obj));
					textArea.append("\n");
				}
			});
		}
	}
	
	private void start(int algorithm) {
		textArea.setText("");
		textArea.setForeground(Color.BLACK);
		switchPhase(3);
		worker = new Worker(algorithm, new TextAreaOutput(textArea));
		worker.start();
	}
	
	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();
        formularTextField = new javax.swing.JTextField();
        formularButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        resolutionButton = new javax.swing.JButton();
        dpllButton = new javax.swing.JButton();
        dpllExtButton = new javax.swing.JButton();
        intermediateResultsCheckBox = new javax.swing.JCheckBox();
        stopButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Logic Solver");
        setMinimumSize(new java.awt.Dimension(635, 462));

        jLabel1.setText("KNF-Formel:");
        jLabel1.setToolTipText("<html>\nSyntax: geschweifte Klammern { und } öffnen, bzw. schließen die Formel, bzw. die Klauseln. <br>\nEin Komma (,) oder ein Semikolon (;) trennt die Literale und Klauseln voneinander. <br>\nLiterale dürfen nur aus einem Zeichen (ein Buchstabe) bestehen, Negation wird durch ein Minus (-) gesetzt.<br>\nWhitespaces werden ignoriert.\n</html>");

        textArea.setEditable(false);
        textArea.setColumns(20);
        textArea.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        textArea.setRows(5);
        jScrollPane1.setViewportView(textArea);

        formularTextField.setToolTipText("<html> Syntax: geschweifte Klammern { und } öffnen, bzw. schließen die Formel, bzw. die Klauseln. <br> Ein Komma (,) oder ein Semikolon (;) trennt die Literale und Klauseln voneinander. <br> Literale dürfen nur aus einem Zeichen (ein Buchstabe) bestehen, Negation wird durch ein Minus (-) gesetzt.<br> Whitespaces werden ignoriert. </html>");

        formularButton.setText("Formel einlesen");
        formularButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                readFormularEvent(evt);
            }
        });

        jLabel2.setText("Beispiel: { {a, b, c}, {-b, d}, {e, b} }");

        resolutionButton.setText("Resolution");
        resolutionButton.setToolTipText("Test auf Unerfüllbarkeit");
        resolutionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resolutionEvent(evt);
            }
        });

        dpllButton.setText("DPLL");
        dpllButton.setToolTipText("Test auf Erfüllbarkeit");
        dpllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dpllEvent(evt);
            }
        });

        dpllExtButton.setText("DPLL mit allen Belegungen");
        dpllExtButton.setToolTipText("Test auf Erfüllbarkeit, gibt alle erfüllenden Belegungen aus");
        dpllExtButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dpllExtEvent(evt);
            }
        });

        intermediateResultsCheckBox.setSelected(true);
        intermediateResultsCheckBox.setText("Zwischenergebnisse ausgeben");

        stopButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/shaman/ds/stop.png"))); // NOI18N
        stopButton.setToolTipText("Beendet die Ausführung des Algorithmus");
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopEvent(evt);
            }
        });

        jLabel3.setText("<html>&copy 2013, Sebastian Weiß</html>");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(formularTextField)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(formularButton))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(resolutionButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dpllButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dpllExtButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(intermediateResultsCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 33, Short.MAX_VALUE)
                        .addComponent(stopButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(formularTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(formularButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(resolutionButton)
                    .addComponent(dpllButton)
                    .addComponent(dpllExtButton)
                    .addComponent(intermediateResultsCheckBox)
                    .addComponent(stopButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void readFormularEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_readFormularEvent
        textArea.setText("");
		try {
			knf = new KNF(formularTextField.getText());
			textArea.setForeground(Color.BLACK);
			textArea.append("Eingegebene Formel: "+formularTextField.getText()+"\n");
			textArea.append("Verarbeitete Formel: "+knf);
			switchPhase(2);
		} catch (NullPointerException ex) {
			textArea.setForeground(Color.RED);
			textArea.append("Formel muss mindestens eine öffnende { und schließende } Klammer beinhalten");
			switchPhase(1);
		} catch (IllegalArgumentException ex) {
			textArea.setForeground(Color.RED);
			textArea.append(ex.getLocalizedMessage());
			switchPhase(1);
		}
    }//GEN-LAST:event_readFormularEvent

    private void resolutionEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resolutionEvent
        start(1);
    }//GEN-LAST:event_resolutionEvent

    private void dpllEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dpllEvent
        start(2);
    }//GEN-LAST:event_dpllEvent

    private void dpllExtEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dpllExtEvent
        start(3);
    }//GEN-LAST:event_dpllExtEvent

    private void stopEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopEvent
        if (worker!=null) {
			worker.stop();
			worker = null;
			switchPhase(2);
			textArea.append("\n\nAbbruch!");
		}
    }//GEN-LAST:event_stopEvent

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
		 * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
		 */
		try {
//			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//				if ("Nimbus".equals(info.getName())) {
//					javax.swing.UIManager.setLookAndFeel(info.getClassName());
//					break;
//				}
//			}
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(LogicGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(LogicGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(LogicGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(LogicGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
        //</editor-fold>

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				LogicGui gui = new LogicGui();
				gui.setVisible(true);
				gui.setLocationRelativeTo(null);
			}
		});
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton dpllButton;
    private javax.swing.JButton dpllExtButton;
    private javax.swing.JButton formularButton;
    private javax.swing.JTextField formularTextField;
    private javax.swing.JCheckBox intermediateResultsCheckBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton resolutionButton;
    private javax.swing.JButton stopButton;
    private javax.swing.JTextArea textArea;
    // End of variables declaration//GEN-END:variables
}