/**
 * 
 */
package edu.wsu.dase.controller;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;

/**
 * @author sarker
 *
 */
public class Suggestion {

	/**
	 * 
	 */
	public Suggestion() {
		// TODO Auto-generated constructor stub
		JFrame frame = new JFrame();

		frame.setLayout(new BorderLayout());

		JButton button = new JButton("OK");

		

		JTextPane textPane = new JTextPane();
		StyledDocument sDoc = textPane.getStyledDocument();

		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub

				String s = "";
				try {
					s += textPane.modelToView(textPane.getCaretPosition()).toString() + "\n";
					s += sDoc.getCharacterElement(textPane.getCaretPosition()).getStartOffset() + "\t";
					s += sDoc.getCharacterElement(textPane.getCaretPosition()).getEndOffset() + "\n";
					s += sDoc.getStartPosition()+"\n";
					System.out.println(s);
				} catch (BadLocationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		frame.add(button, BorderLayout.NORTH);
		frame.add(textPane, BorderLayout.CENTER);
		frame.setPreferredSize(new Dimension(500, 400));
		frame.setSize(new Dimension(500, 400));

		frame.setVisible(true);
	}

	public static void main(String[] args) {
		Suggestion sg = new Suggestion();
	}

}
