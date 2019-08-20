package org.aksw.facete3.cli.main;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class MainFacete3Swing {
	public static void main(String[] args) {
		// 1. Create the frame.
		JFrame frame = new JFrame("FrameDemo");

		// 2. Optional: What happens when the frame closes?
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// 3. Create components and put them in the frame.
		// ...create emptyLabel...
		frame.getContentPane().add(new JLabel("<html><h1>foobar</h1><br /><ul><input type=\"button\">button</input><li>test</li></ul><img src=\"file:///home/raven/Projects/Eclipse/AKSWPapers/2018/LSWT_LDatScale/images/gears.png\"></html>"), BorderLayout.CENTER);

		// 4. Size the frame.
		frame.pack();

		// 5. Show it.
		frame.setVisible(true);
	}
}
