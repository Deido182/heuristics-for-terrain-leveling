import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;


public class PathPrinter extends JFrame {
	public final int B = 1000;
	public final int H = 1000;
	
	private class Printer extends JComponent {

		Field field;
		Path path;
		double multiplierX, multiplierY, shiftX, shiftY;
		Color color;
		
		public Printer(Field field, Path path, double multiplierX, double multiplierY, double shiftX, double shiftY, Color color) {
			this.field = field;
			this.path = path;
			this.multiplierX = multiplierX;
			this.multiplierY = multiplierY;
			this.shiftX = shiftX;
			this.shiftY = shiftY;
			this.color = color;
		}
		
		public void grid(Graphics2D g2D) {
			g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2D.setPaint(Color.RED);
			
			ArrayList <Coordinates> coordinates = new ArrayList <> ();
			coordinates.addAll(field.cells.keySet());
			
			coordinates.sort((Coordinates c1, Coordinates c2) -> {
				if(c1.x < c2.x)
					return -1;
				if(c1.x > c2.x)
					return 1;
				return Double.compare(c1.y, c2.y);
			});
			
			for(int i = 0; i < coordinates.size(); i ++) {
				for(int j = i + 1; j < coordinates.size(); j ++, i ++) {
					if(!coordinates.get(j).sameX(coordinates.get(j - 1)))
						break;
					g2D.draw(new Line2D.Double((coordinates.get(j - 1).x - field.deltaX / 2) * multiplierX + shiftX, 
												H - ((coordinates.get(j - 1).y - field.deltaY / 2) * multiplierY + shiftY), 
												(coordinates.get(j).x - field.deltaX / 2) * multiplierX + shiftX, 
												H - ((coordinates.get(j).y + field.deltaY / 2) * multiplierY + shiftY)));
					g2D.draw(new Line2D.Double((coordinates.get(j - 1).x + field.deltaX / 2) * multiplierX + shiftX, 
												H - ((coordinates.get(j - 1).y - field.deltaY / 2) * multiplierY + shiftY), 
												(coordinates.get(j).x + field.deltaX / 2) * multiplierX + shiftX, 
												H - ((coordinates.get(j).y + field.deltaY / 2) * multiplierY + shiftY)));
				}
			}
			
			coordinates.sort((Coordinates c1, Coordinates c2) -> {
				if(c1.y < c2.y)
					return -1;
				if(c1.y > c2.y)
					return 1;
				return Double.compare(c1.x, c2.x);
			});
			
			for(int i = 0; i < coordinates.size(); i ++) {
				for(int j = i + 1; j < coordinates.size(); j ++, i ++) {
					if(!coordinates.get(j).sameY(coordinates.get(j - 1)))
						break;
					g2D.draw(new Line2D.Double((coordinates.get(j - 1).x - field.deltaX / 2) * multiplierX + shiftX, 
												H - ((coordinates.get(j - 1).y - field.deltaY / 2) * multiplierY + shiftY), 
												(coordinates.get(j).x + field.deltaX / 2) * multiplierX + shiftX, 
												H - ((coordinates.get(j).y - field.deltaY / 2) * multiplierY + shiftY)));
					g2D.draw(new Line2D.Double((coordinates.get(j - 1).x - field.deltaX / 2) * multiplierX + shiftX, 
												H - ((coordinates.get(j - 1).y + field.deltaY / 2) * multiplierY + shiftY), 
												(coordinates.get(j).x + field.deltaX / 2) * multiplierX + shiftX, 
												H - ((coordinates.get(j).y + field.deltaY / 2) * multiplierY + shiftY)));
				}
			}
		}
		
		public void hull(Graphics2D g2D) {
			g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2D.setPaint(Color.BLUE);
			
			ArrayList <Coordinates> coordinates = new ArrayList <> ();
			for(Coordinates c : field.cells.keySet()) {
				Coordinates c1 = new Coordinates(c.x - field.deltaX / 2, c.y - field.deltaY / 2);
				if(!coordinates.contains(c1))
					coordinates.add(c1);
				Coordinates c2 = new Coordinates(c.x + field.deltaX / 2, c.y - field.deltaY / 2);
				if(!coordinates.contains(c2))
					coordinates.add(c2);
				Coordinates c3 = new Coordinates(c.x - field.deltaX / 2, c.y + field.deltaY / 2);
				if(!coordinates.contains(c3))
					coordinates.add(c3);
				Coordinates c4 = new Coordinates(c.x + field.deltaX / 2, c.y + field.deltaY / 2);
				if(!coordinates.contains(c4))
					coordinates.add(c4);
			}
			
			ArrayList <Coordinates> hull = ConvexHull.build(coordinates);
			for(int i = 0; i < hull.size(); i ++) {
				int j = (i + 1) % hull.size();
				g2D.draw(new Line2D.Double(hull.get(i).x * multiplierX + shiftX, 
											H - (hull.get(i).y * multiplierY + shiftY), 
											hull.get(j).x * multiplierX + shiftX, 
											H - (hull.get(j).y * multiplierY + shiftY)));
			}
		}
		
		public void field(Graphics2D g2D) {
			grid(g2D);
			hull(g2D);
		}
		
		public void path(Graphics2D g2D) {
			g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2D.setPaint(color);
			
			if(path.size() == 1) {
				g2D.draw((Shape) new Point2D.Double(path.getCoordinates(0).x, path.getCoordinates(0).y));
				return;
			}
			
			for(int i = 1; i < path.size(); i ++)
				g2D.draw(new Line2D.Double(path.getCoordinates(i - 1).x * multiplierX + shiftX, 
											H - (path.getCoordinates(i - 1).y * multiplierY + shiftY), 
											path.getCoordinates(i).x * multiplierX + shiftX, 
											H - (path.getCoordinates(i).y * multiplierY + shiftY)));
		}
		
		@Override
		public void paint(Graphics g) {
			Graphics2D g2D = (Graphics2D) g;
			field(g2D);
			path(g2D);
		}
	}
	
	public PathPrinter(String title) {
		setSize(B, H);
		setTitle(title);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
	}
	
	public void print(Field field, Path path, double multiplierX, double multiplierY, double shiftX, double shiftY, Color color) {
		setVisible(true);
		add(new Printer(field, path, multiplierX, multiplierY, shiftX, shiftY, color));
	}
	
	public void print(Field field, Path path, double multiplierX, double multiplierY, double shiftX, double shiftY, String fileName, Color color) throws IOException {
		print(field, path, multiplierX, multiplierY, shiftX, shiftY, color);
		BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = img.createGraphics();
		printAll(g2d);
		g2d.dispose();
		ImageIO.write(img, "png", new File(fileName));
	}
	
	public void printChains(Field field, ArrayList <Truck> chains, double multiplierX, double multiplierY, double shiftX, double shiftY, String fileName, Color color) throws IOException {
		for(Truck t : chains)
			print(field, t.path, multiplierX, multiplierY, shiftX, shiftY, color);
		BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = img.createGraphics();
		printAll(g2d);
		g2d.dispose();
		ImageIO.write(img, "png", new File(fileName));
	}
}
