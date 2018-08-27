package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.io.IOException;
import java.util.Collections;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.CheckBoxList;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

public class MainCliFacete3 {
	public static void main(String[] args) throws IOException {
		
		// Setup terminal and screen layers
        Terminal terminal = new DefaultTerminalFactory().createTerminal();
        Screen screen = new TerminalScreen(terminal);
        screen.startScreen();
        
		TerminalSize size = new TerminalSize(14, 10);
		
		Panel mainPanel = new Panel();
		mainPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
		
		
		ActionListBox actionListBox = new ActionListBox(size);
		
		for(int i = 0; i < 10; ++i) {
			actionListBox.addItem("item" + i, new Runnable() {
				@Override
				public void run() {
					// Code to run when action activated
				}
			});
		}

		mainPanel.addComponent(actionListBox);
		
		CheckBoxList<String> checkBoxList = new CheckBoxList<String>(size);
		checkBoxList.addItem("item 1");
		checkBoxList.addItem("item 2");
		checkBoxList.addItem("item 3");

		mainPanel.addComponent(checkBoxList);

		
		 // Create window to hold the panel
        BasicWindow window = new BasicWindow();
        window.setComponent(mainPanel);
        window.setHints(Collections.singleton(Window.Hint.NO_POST_RENDERING));

        //Window.Hint.NO_POST_RENDERING
        // Create gui and start gui

		MultiWindowTextGUI gui = new MultiWindowTextGUI(
        		screen,
        		new DefaultWindowManager(),
        		new EmptySpace(TextColor.ANSI.BLACK));
		

//
//        MultiWindowTextGUI gui = new MultiWindowTextGUI(
//        		screen,
//        		new DefaultWindowManager(),
//                null, //new WindowShadowRenderer(),
//        		new EmptySpace(TextColor.ANSI.BLUE));
        gui.addWindowAndWait(window);

	}
}
