import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;


public class PathPrinter extends JFrame {
	private class Printer extends JComponent {
		ArrayList <Movement> path;
		double multiplierX, multiplierY;
		
		public Printer(ArrayList <Movement> path, double multiplierX, double multiplierY) {
			this.path = path;
			this.multiplierX = multiplierX;
			this.multiplierY = multiplierY;
		}
		
		@Override
		public void paint(Graphics g) {
			Graphics2D g2D = (Graphics2D) g;
			
			g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2D.setPaint(Color.BLACK);
			
			for(Movement m : path)
				g2D.draw(new Line2D.Double(m.from.x * multiplierX, m.from.y * multiplierY, m.to.x * multiplierX, m.to.y * multiplierY));
		}
	}
	
	public PathPrinter() {
		setSize(1000, 1000);
		setTitle("Truck movements");
		setDefaultCloseOperation(HIDE_ON_CLOSE);
	}
	
	public void print(ArrayList <Movement> path, double multiplierX, double multiplierY) {
		setVisible(true);
		add(new Printer(path, multiplierX, multiplierY));
	}
	
	public void print(ArrayList <Movement> path, double multiplierX, double multiplierY, String fileName) throws IOException {
		print(path, multiplierX, multiplierY);
		BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = img.createGraphics();
		printAll(g2d);
		g2d.dispose();
		ImageIO.write(img, "png", new File(fileName));
	}
}
