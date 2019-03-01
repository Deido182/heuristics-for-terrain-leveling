import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;


public class PathPrinter extends JFrame {
	private class Printer extends JComponent {
		
		Path path;
		double multiplierX, multiplierY;
		
		public Printer(Path path, double multiplierX, double multiplierY) {
			this.path = path;
			this.multiplierX = multiplierX;
			this.multiplierY = multiplierY;
		}
		
		@Override
		public void paint(Graphics g) {
			Graphics2D g2D = (Graphics2D) g;
			
			g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2D.setPaint(Color.BLACK);
			
			for(int i = 1; i < path.length(); i ++)
				g2D.draw(new Line2D.Double(path.getCoordinates(i - 1).x * multiplierX, path.getCoordinates(i - 1).y * multiplierY, path.getCoordinates(i).x * multiplierX, path.getCoordinates(i).y * multiplierY));
		}
	}
	
	public PathPrinter() {
		setSize(1000, 1000);
		setTitle("Truck movements");
		setDefaultCloseOperation(HIDE_ON_CLOSE);
	}
	
	public void print(Path path, double multiplierX, double multiplierY) {
		setVisible(true);
		add(new Printer(path, multiplierX, multiplierY));
	}
	
	public void print(Path path, double multiplierX, double multiplierY, String fileName) throws IOException {
		print(path, multiplierX, multiplierY);
		BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = img.createGraphics();
		printAll(g2d);
		g2d.dispose();
		ImageIO.write(img, "png", new File(fileName));
	}
}
