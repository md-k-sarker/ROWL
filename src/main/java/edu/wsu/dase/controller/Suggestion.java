/**
 * 
 */
package edu.wsu.dase.controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
 * @author sarker
 *
 */
public class Suggestion {

	StyleContext sc = StyleContext.getDefaultStyleContext();
	AttributeSet redC = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.RED);
	AttributeSet blackC = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.BLACK);
	
	AttributeSet setUnderLine = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Underline, true);
	AttributeSet clearUnderLine = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Underline, false);

	boolean isJagged = false;

	/**
	 * 
	 */
	public Suggestion() {
		// TODO Auto-generated constructor stub
		JFrame frame = new JFrame();

		frame.setLayout(new BorderLayout());

		JButton button = new JButton("OK");

		JTextPane textPane = new JTextPane();
		//textPane.setEditorKit(new NewEditorKit());
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
					s += sDoc.getStartPosition() + "\n";

					if (sDoc.getText(0, sDoc.getLength()).contains("hello")) {
						//sDoc.setCharacterAttributes(0, sDoc.getLength(), redC, false);
						//sDoc.setCharacterAttributes(0, sDoc.getLength(), strikeOK, false);
						isJagged = true;
					} else {
						//sDoc.setCharacterAttributes(0, sDoc.getLength(), blueC, false);
						//sDoc.setCharacterAttributes(0, sDoc.getLength(), strikeNO, false);
						isJagged = false;
					}

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

//	class NewEditorKit extends StyledEditorKit {
//		public ViewFactory getViewFactory() {
//			return new NewViewFactory();
//		}
//	}
//
//	class NewViewFactory implements ViewFactory {
//		public View create(Element elem) {
//			String kind = elem.getName();
//			if (kind != null) {
//				if (kind.equals(AbstractDocument.ContentElementName)) {
//					return new JaggedLabelView(elem);
//				} else if (kind.equals(AbstractDocument.ParagraphElementName)) {
//					return new ParagraphView(elem);
//				} else if (kind.equals(AbstractDocument.SectionElementName)) {
//					return new BoxView(elem, View.Y_AXIS);
//				} else if (kind.equals(StyleConstants.ComponentElementName)) {
//					return new ComponentView(elem);
//				} else if (kind.equals(StyleConstants.IconElementName)) {
//					return new IconView(elem);
//				}
//			}
//
//			// default to text display
//			return new LabelView(elem);
//		}
//	}
//
//	class JaggedLabelView extends LabelView {
//
//		public JaggedLabelView(Element elem) {
//			super(elem);
//		}
//
//		public void paint(Graphics g, Shape allocation) {
//			super.paint(g, allocation);
//			paintJaggedLine(g, allocation);
//		}
//
//		public void paintJaggedLine(Graphics g, Shape a) {
//			int y = (int) (a.getBounds().getY() + a.getBounds().getHeight());
//			int x1 = (int) a.getBounds().getX();
//			int x2 = (int) (a.getBounds().getX() + a.getBounds().getWidth());
//
//			Color old = g.getColor();
//			if (isJagged) {
//				g.setColor(Color.red);
//				for (int i = x1; i <= x2; i += 6) {
//					g.drawArc(i + 3, y - 3, 3, 3, 0, 180);
//					g.drawArc(i + 6, y - 3, 3, 3, 180, 181);
//				}
//				g.setColor(old);
//			}
//		}
//	}

}
