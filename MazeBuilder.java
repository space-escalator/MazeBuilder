import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.util.ArrayList;
import java.util.Random;

public class MazeBuilder {
	JFrame frame;
	Cell[][] cells;
	public static final int HORIZONTALCELLS = 30;
	public static final int VERTICALCELLS = 31;
	public static final int MILLISECONDSPERFRAME = 10;

	public static void main(String[] args) {
		MazeBuilder m = new MazeBuilder();
		m.run();
	}

	private void run() {
		frame = new JFrame("Maze");

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		GPanel panel = new GPanel();
		frame.getContentPane().add(BorderLayout.CENTER, panel);

		frame.setSize(HORIZONTALCELLS * Cell.width, VERTICALCELLS * Cell.height + 22); //weird cutoff from 20ish pixel header
		frame.setLocationByPlatform(true);
		frame.setResizable(false);
		frame.setVisible(true);

		cells = new Cell[VERTICALCELLS][HORIZONTALCELLS];
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				cells[i][j] = new Cell(j,i);
			}
		}

		move();
	}

	class GPanel extends JPanel{
		private static final long serialVersionUID = 1L;

		@Override
		public void paintComponent(Graphics g) {
			g.setColor(Color.GREEN);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());

			for (int i = 0; i < cells.length; i++) {
				for (int j = 0; j < cells[i].length; j++) {
					g.setColor(cells[i][j].getColor());
					g.fillRect(j*Cell.width, i*Cell.height, Cell.width, Cell.height);
				}
			}
		}
	}

	private void move() {
		cells[cells.length-2][1].setColor(Color.WHITE); // first spot on maze. walker seeks established (white) maze tiles

		Cell nextStart = cells[cells.length-2][3]; //the first start point. 
		while (nextStart != null) {
			erasedLoopWalk(nextStart.getx(), nextStart.gety());
			nextStart = findNextStartCell(nextStart);
		}
	}

	private void erasedLoopWalk(int x, int y) {
		ArrayList<Cell> trail = new ArrayList<Cell>();
		ArrayList<Integer> moveLog = new ArrayList<Integer>(); //should always have the same number of items as trail
		Random rand = new Random();

		trail.add(cells[y][x]);
		moveLog.add(-1);//placeholder that hopefully won't break anything
		while (true) {
			//decide candidates based on borders and previous move
			ArrayList<Integer> candidates = new ArrayList<Integer>();
			int prev = moveLog.get(trail.size()-1);
			if (x-1 != 0 && prev != 1) candidates.add(0);
			if (x+1 != cells[0].length-1 && prev != 0) candidates.add(1);
			if (y-1 != 0 && prev != 3) candidates.add(2);
			if (y+1 != cells.length-1 && prev != 2) candidates.add(3);

			//choose a direction and walk a step
			int move = candidates.get(rand.nextInt(candidates.size()));
			if (move == 0) x--;
			if (move == 1) x++;
			if (move == 2) y--;
			if (move == 3) y++;
			trail.add(cells[y][x]);
			moveLog.add(move);

			//color steps so far red and repaint
			for (Cell c : trail) {
				c.setColor(Color.RED);
			}
			frame.repaint();
			try {
				Thread.sleep(MILLISECONDSPERFRAME);
			} catch (InterruptedException e) {
				// Auto-generated catch block
				e.printStackTrace();
			}

			//look for loops and erase back if found
			boolean looped = false;
			if (cells[y][x+1].getColor().equals(Color.RED) && move != 0) {
				x++;
				looped = true;
			}
			else if (cells[y][x-1].getColor().equals(Color.RED) && move != 1) {
				x--;
				looped = true;
			}
			else if (cells[y+1][x].getColor().equals(Color.RED) && move != 2) {
				y++;
				looped = true;
			}
			else if (cells[y-1][x].getColor().equals(Color.RED) && move != 3) {
				y--;
				looped = true;
			}
			else if (cells[y-1][x-1].getColor().equals(Color.RED) && cells[y-1][x-1] != trail.get(trail.size()-3)) {
				x--;
				y--;
				looped = true;
			}
			else if (cells[y-1][x+1].getColor().equals(Color.RED) && cells[y-1][x+1] != trail.get(trail.size()-3)) {
				x++;
				y--;
				looped = true;
			}
			else if (cells[y+1][x-1].getColor().equals(Color.RED) && cells[y+1][x-1] != trail.get(trail.size()-3)) {
				x--;
				y++;
				looped = true;
			}
			else if (cells[y+1][x+1].getColor().equals(Color.RED) && cells[y+1][x+1] != trail.get(trail.size()-3)) {
				x++;
				y++;
				looped = true;
			}
			
			if (looped) {
				int retrace = trail.indexOf(cells[y][x]);
				for (int i = retrace + 1; i < trail.size(); i++) {
					trail.get(i).setColor(Color.BLACK);
				}
				trail.subList(retrace + 1, trail.size()).clear();
				moveLog.subList(retrace + 1, moveLog.size()).clear();
			}

			//look for connections and commit if found
			if (	cells[y][x-1].getColor().equals(Color.WHITE) ||
				cells[y][x+1].getColor().equals(Color.WHITE) ||
				cells[y-1][x].getColor().equals(Color.WHITE) ||
				cells[y+1][x].getColor().equals(Color.WHITE)) {
				for (Cell c : trail) c.setColor(Color.WHITE);
					frame.repaint();
				//				System.out.println("done!");
				return;
			}
		}
	}

	private Cell findNextStartCell(Cell lastStart) {
		//search bottom to top, left to right, for a black cell with two adjacent black cells

		boolean firstLoop = true;
		for (int y = cells.length-2; y > 0; y--) {
			for (int x = 1; x < cells[y].length-2; x++) {
				if (firstLoop) {
					x = lastStart.getx() + 1;
					y = lastStart.gety();
					firstLoop = false;
				}
				
				int adjacentBlackCounter = 0;

				if (cells[y][x-1].getColor().equals(Color.BLACK))// && x-1 != 0) 
adjacentBlackCounter++;
				if (cells[y][x+1].getColor().equals(Color.BLACK))// && x+1 != cells[0].length-1) 
adjacentBlackCounter++;
				if (cells[y-1][x].getColor().equals(Color.BLACK))// && y-1 != 0) 
adjacentBlackCounter++;
				if (cells[y+1][x].getColor().equals(Color.BLACK))// && y+1 != cells.length-1) 
adjacentBlackCounter++;
if (cells[y-1][x-1].getColor().equals(Color.BLACK))
	adjacentBlackCounter++;
if (cells[y-1][x+1].getColor().equals(Color.BLACK))
	adjacentBlackCounter++;
if (cells[y+1][x-1].getColor().equals(Color.BLACK))
	adjacentBlackCounter++;
if (cells[y+1][x+1].getColor().equals(Color.BLACK))
	adjacentBlackCounter++;

if (adjacentBlackCounter == 8) {
					// System.out.println(x + ", " + y);
	return cells[y][x];
}
}
}
return null;
}

	// private Cell findNextStartCell(Cell lastStart) {
	// 	boolean firstLoop = true; //pick up where it was left off
	// 	for (int y = cells.length-2; y > 0; y-= 2) {
	// 		for (int x = 2; x < cells[y].length-1; x++) {
	// 			if (firstLoop) {
	// 				x = lastStart.getx() + 1;
	// 				y = lastStart.gety();
	// 				firstLoop = false;
	// 			}

	// 			if (	//cells[y][x-2].getColor().equals(Color.WHITE) &&
	// 					cells[y][x-1].getColor().equals(Color.BLACK) &&
	// 					cells[y][x].getColor().equals(Color.BLACK)) {
	// 				// System.out.println(x + ", " + y);
	// 				return cells[y][x];
	// 			}
	// 		}
	// 	}
	// 	return null;
	// }

}
