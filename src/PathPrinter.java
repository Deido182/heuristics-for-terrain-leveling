import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class PathPrinter extends JFrame {
	private class Printer extends JComponent {
		@Override
		public void paint(Graphics g) {
			Graphics2D g2D = (Graphics2D) g;
			
		}
	}
	
	public PathPrinter() {
		setSize(1000, 1000);
		setTitle("Truck movements");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		add(new Printer());
	}
}
