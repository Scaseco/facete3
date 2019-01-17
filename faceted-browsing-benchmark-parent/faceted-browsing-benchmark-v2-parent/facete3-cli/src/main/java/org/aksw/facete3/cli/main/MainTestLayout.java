package org.aksw.facete3.cli.main;

import java.util.Arrays;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.GridLayout.Alignment;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.MouseCaptureMode;
import com.googlecode.lanterna.terminal.Terminal;

public class MainTestLayout {
	public static void main(String[] args) throws Exception {

        Terminal terminal = new DefaultTerminalFactory().setMouseCaptureMode(MouseCaptureMode.CLICK).createTerminal();
        Screen screen = new TerminalScreen(terminal);
        screen.startScreen();
        
        WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);

//		TerminalSize size = new TerminalSize(80, 25);
		

		MultiWindowTextGUI gui = new MultiWindowTextGUI(
        		screen,
        		new DefaultWindowManager(),
        		new EmptySpace(TextColor.ANSI.BLACK));

		
		Panel mainPanel = new Panel();
		
		ActionListBox l1 = new ActionListBox();
		for(int i = 0; i < 100; ++i) {
			l1.addItem("l1-item#" + i, () -> {});
		}

		ActionListBox l2 = new ActionListBox();
		for(int i = 0; i < 1000; ++i) {
			l2.addItem("l2-item#" + i, () -> {});
		}

		ActionListBox l3 = new ActionListBox();
		for(int i = 0; i < 10000; ++i) {
			l3.addItem("l3-item#" + i, () -> {});
		}

		
		Panel l1p = new Panel();
//		l1.setLayoutData(GridLayout.createLayoutData(Alignment.FILL, Alignment.BEGINNING, true, false, 1, 1));
//		l2.setLayoutData(GridLayout.createLayoutData(Alignment.FILL, Alignment.BEGINNING, true, false, 1, 1));
//		l3.setLayoutData(GridLayout.createLayoutData(Alignment.FILL, Alignment.BEGINNING, true, false, 1, 1));

		//mainPanel.setLayoutManager(new GridLayout(1));
		int layoutMode = 1;
		switch(layoutMode) {
		case 0:
			mainPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
			mainPanel.setLayoutData(GridLayout.createLayoutData(Alignment.FILL, Alignment.FILL, true, true, 1, 1));
			mainPanel.addComponent(l1.withBorder(Borders.singleLine("List 1")), LinearLayout.createLayoutData(LinearLayout.Alignment.Fill));
			mainPanel.addComponent(l2.withBorder(Borders.singleLine("List 2")), LinearLayout.createLayoutData(LinearLayout.Alignment.Fill));
			mainPanel.addComponent(l3.withBorder(Borders.singleLine("List 3")), LinearLayout.createLayoutData(LinearLayout.Alignment.Fill));
			break;
		default:
			mainPanel.setLayoutManager(new GridLayout(1));
			//mainPanel.setLayoutData(GridLayout.createLayoutData(Alignment.FILL, Alignment.FILL, true, true, 1, 1));
			mainPanel.addComponent(l1.withBorder(Borders.singleLine("List 1")), GridLayout.createLayoutData(GridLayout.Alignment.FILL, GridLayout.Alignment.BEGINNING, true, false, 1, 1));
			mainPanel.addComponent(l2.withBorder(Borders.singleLine("List 2")), GridLayout.createLayoutData(GridLayout.Alignment.FILL, GridLayout.Alignment.BEGINNING, true, false, 1, 1));
			mainPanel.addComponent(l3.withBorder(Borders.singleLine("List 3")), GridLayout.createLayoutData(GridLayout.Alignment.FILL, GridLayout.Alignment.BEGINNING, true, false, 1, 1));
		}
        BasicWindow window = new BasicWindow();
        window.setComponent(mainPanel);     
        window.setHints(Arrays.asList(Window.Hint.NO_POST_RENDERING, Window.Hint.EXPANDED, Window.Hint.FIT_TERMINAL_WINDOW));

        gui.addWindowAndWait(window);
	}
}
