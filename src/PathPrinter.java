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

		Field field;
		Path path;
		double multiplierX, multiplierY, shiftX, shiftY;
		
		public Printer(Field field, Path path, double multiplierX, double multiplierY, double shiftX, double shiftY) {
			this.field = field;
			this.path = path;
			this.multiplierX = multiplierX;
			this.multiplierY = multiplierY;
			this.shiftX = shiftX;
			this.shiftY = shiftY;
		}
		
		public void field(Graphics2D g2D) {
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
					g2D.draw(new Line2D.Double(coordinates.get(j - 1).x * multiplierX + shiftX - field.deltaX / 2, 
												coordinates.get(j - 1).y * multiplierY + shiftY - field.deltaY / 2, 
												coordinates.get(j).x * multiplierX + shiftX - field.deltaX / 2, 
												coordinates.get(j).y * multiplierY + shiftY - field.deltaY / 2));
					g2D.draw(new Line2D.Double(coordinates.get(j - 1).x * multiplierX + shiftX + field.deltaX / 2, 
												coordinates.get(j - 1).y * multiplierY + shiftY + field.deltaY / 2, 
												coordinates.get(j).x * multiplierX + shiftX + field.deltaX / 2, 
												coordinates.get(j).y * multiplierY + shiftY + field.deltaY / 2));
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
					g2D.draw(new Line2D.Double(coordinates.get(j - 1).x * multiplierX + shiftX - field.deltaX / 2, 
												coordinates.get(j - 1).y * multiplierY + shiftY - field.deltaY / 2, 
												coordinates.get(j).x * multiplierX + shiftX - field.deltaX / 2, 
												coordinates.get(j).y * multiplierY + shiftY - field.deltaY / 2));
					g2D.draw(new Line2D.Double(coordinates.get(j - 1).x * multiplierX + shiftX + field.deltaX / 2, 
												coordinates.get(j - 1).y * multiplierY + shiftY + field.deltaY / 2, 
												coordinates.get(j).x * multiplierX + shiftX + field.deltaX / 2, 
												coordinates.get(j).y * multiplierY + shiftY + field.deltaY / 2));
				}
			}
		}
		
		public void path(Graphics2D g2D) {
			g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2D.setPaint(Color.BLACK);
			
			for(int i = 1; i < path.length(); i ++)
				g2D.draw(new Line2D.Double(path.getCoordinates(i - 1).x * multiplierX + shiftX, 
											path.getCoordinates(i - 1).y * multiplierY + shiftY, 
											path.getCoordinates(i).x * multiplierX + shiftX, 
											path.getCoordinates(i).y * multiplierY + shiftY));
		}
		
		@Override
		public void paint(Graphics g) {
			Graphics2D g2D = (Graphics2D) g;
			field(g2D);
			path(g2D);
		}
	}
	
	public PathPrinter() {
		setSize(1000, 1000);
		setTitle("Truck movements");
		setDefaultCloseOperation(HIDE_ON_CLOSE);
	}
	
	public void print(Field field, Path path, double multiplierX, double multiplierY, double shiftX, double shiftY) {
		setVisible(true);
		add(new Printer(field, path, multiplierX, multiplierY, shiftX, shiftY));
	}
	
	public void print(Field field, Path path, double multiplierX, double multiplierY, double shiftX, double shiftY, String fileName) throws IOException {
		print(field, path, multiplierX, multiplierY, shiftX, shiftY);
		BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = img.createGraphics();
		printAll(g2d);
		g2d.dispose();
		ImageIO.write(img, "png", new File(fileName));
	}
}
