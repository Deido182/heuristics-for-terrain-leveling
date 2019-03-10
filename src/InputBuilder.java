import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class InputBuilder {
	String path;
	int rows, columns;
	Coordinates bottomLeft;
	double deltaX, deltaY;
	double maxValue;
	
	public InputBuilder(String path, int rows, int columns, Coordinates bottomLeft, double deltaX, double deltaY, double maxValue) {
		this.path = path;
		this.rows = rows;
		this.columns = columns;
		this.bottomLeft = bottomLeft;
		this.deltaX = deltaX;
		this.deltaY = deltaY;
		this.maxValue = maxValue;
	}
	
	private Integer getNumber() {
		Integer number = 1;
		while(new File(path + number).exists())
			number ++;
		return number;
	}
	
	private double getX(int row) {
		return row * deltaX + deltaX / 2;
	}
	
	private double getY(int column) {
		return column * deltaY + deltaY / 2;
	}
	
	private int randomSign() {
		return (int)(Math.random() * 2) - 1;
	}
	
	public void build(int n) throws IOException {
		while(n -- > 0) {
			PrintWriter out = new PrintWriter(new FileWriter(new File(path + getNumber())));
			
			out.println(rows * columns);
			for(int i = 0; i < rows; i ++)
				for(int j = 0; j < columns; j ++) 
					out.println(getX(i) + " " + getY(j) + " " + (randomSign() * maxValue));	
			out.close();
		}
	}
}
