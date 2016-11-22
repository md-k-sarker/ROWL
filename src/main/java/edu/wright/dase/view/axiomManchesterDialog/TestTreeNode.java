package edu.wright.dase.view.axiomManchesterDialog;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

public class TestTreeNode extends JFrame{

	public static void main(String[] args) {
		

		//1. Create the frame.
		JFrame frame = new JFrame("FrameDemo");
		
		frame.setSize(600,400);
	
		String s = "<html><b style=\"color:#624FDB;\">" + "colored" + "</b></html>";
		
		//create the root node
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        //create the child nodes
        DefaultMutableTreeNode vegetableNode = new DefaultMutableTreeNode(s);
        DefaultMutableTreeNode fruitNode = new DefaultMutableTreeNode("Fruits");
 
        //add the child nodes to the root node
        root.add(vegetableNode);
        root.add(fruitNode);
         
        //create the tree by passing in the root node
        JTree  tree = new JTree(root);
        frame.getContentPane().add(tree, BorderLayout.CENTER);
		
		//Ask for window decorations provided by the look and feel.
		JFrame.setDefaultLookAndFeelDecorated(true);

		//2. Optional: What happens when the frame closes?
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		

		//4. Size the frame.
		frame.pack();

		//5. Show it.
		frame.setVisible(true);
	}

}
