package com.loovjo.friendquotes;

import javax.swing.JFrame;

import com.loovjo.loo2D.MainWindow;
import com.loovjo.loo2D.utils.FileLoader;
import com.loovjo.loo2D.utils.Vector;

public class Main extends MainWindow {
	public Main() {
		super("Friendship quotes", new FriendScene(), new Vector(100, 100), true);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		FileLoader.setLoaderClass(Main.class);

		new Main();
		
	}
}
