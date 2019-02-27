import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JFrame;


public class PathPrinter extends JFrame {
	private class Printer extends JComponent {
		ArrayList <Movement> path;
		double multiplier;
		
		public Printer(ArrayList <Movement> path, double scale) {
			this.path = path;
			this.multiplier = scale;
		}
		
		@Override
		public void paint(Graphics g) {
			Graphics2D g2D = (Graphics2D) g;
			
			g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2D.setPaint(Color.BLACK);
			
			for(Movement m : path)
				g2D.draw(new Line2D.Double(m.from.x * multiplier, m.from.y * multiplier, m.to.x * multiplier, m.to.y * multiplier));
		}
	}
	
	public PathPrinter() {
		setSize(1000, 1000);
		setTitle("Truck movements");
		setDefaultCloseOperation(HIDE_ON_CLOSE);
	}
	
	public void print(ArrayList <Movement> path, double multiplier) {
		setVisible(true);
		add(new Printer(path, multiplier));
	}
}
