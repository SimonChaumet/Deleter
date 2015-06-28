package fr.scarex.updater.deleter;

import java.awt.Color;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class Main
{
	public static String displayString;
	public static Pane pane;
	public static volatile long totalSize = 1;
	public static volatile long currentSize = 1;
	public static Color color = Color.BLUE;
	public static long time;

	public static void main(String[] args) {
		time = System.currentTimeMillis();
		JFrame frame = new JFrame("SCAREX deleter");
		frame.setSize(400, 100);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(true);
		frame.setContentPane(pane = new Pane());
		frame.setVisible(true);

		displayString = "Searching for mods to delete...";

		BufferedReader br = null;
		try {
			String dir = args.length > 0 ? args[0] : System.getProperty("user.dir");
			File list = new File(dir, "to_delete.updater");

			if (list.exists()) {
				br = new BufferedReader(new FileReader(list));
				String s;
				List<File> files = new ArrayList<File>();
				while ((s = br.readLine()) != null && s.length() > 1) {
					File f = new File(dir, s);
					if (f.exists()) {
						files.add(f);
						totalSize += f.length();
					}
				}
				br.close();

				Thread t = new Thread() {
					@Override
					public void run() {
						while (true) {
							pane.updateBar(StrictMath.round((currentSize * 100) / totalSize));
							pane.repaint();
						}
					}
				};
				t.start();

				for (File f : files) {
					displayString = f.getCanonicalPath();
					currentSize += f.length();
					f.delete();
				}

				color = Color.GREEN;
				displayString = "All the files have been deleted in " + (System.currentTimeMillis() - time) + "ms.";
				list.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
			color = Color.RED;
			displayString = "Couldn't delete files, run this with the console for more details : " + e.getMessage();
		} finally {
			try {
				br.close();
			} catch (Exception e) {}
		}
		System.exit(0);
	}

	public static class Pane extends JPanel
	{
		private static final long serialVersionUID = 2927193679563813586L;

		public JProgressBar jbar;

		public Pane() {
			jbar = new JProgressBar();
			jbar.setMinimum(0);
			jbar.setMaximum(100);
			jbar.setStringPainted(true);
			add(jbar);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(color);
			g.drawString(displayString, 10, 50);
		}

		public void updateBar(int value) {
			jbar.setValue(value >= 98 ? 100 : value);
		}
	}
}
