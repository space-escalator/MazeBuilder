/*
Aaron Goldfogel, August 2015
Generates 2D mazes via an approximation of Wilson's Algorithm.

Dependant on Cell.java.

HORIZONTALCELLS controls the width of the maze in cells
VERITCALCELLS controls the height of the maze in cells
	These must be odd numbers for mathematical reasons.
MILLISECONDSPERFRAME controls animation speed. 
	Lower numbers for faster. 0 for instantaneous.
*/


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
	public static final int HORIZONTALCELLS = 61;
	public static final int VERTICALCELLS = 61;
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

		frame.setSize(HORIZONTALCELLS * Cell.WIDTH, VERTICALCELLS * Cell.HEIGHT + 22); //weird cutoff from 20ish pixel header
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
					g.fillRect(j*Cell.WIDTH, i*Cell.HEIGHT, Cell.WIDTH, Cell.HEIGHT);
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
		ArrayList<Integer> moveLog = new ArrayList<Integer>(); //should always have half as many items as trail
		Random rand = new Random();

		trail.add(cells[y][x]);
		cells[y][x].setColor(Color.RED);

		moveLog.add(-1);//placeholder that hopefully won't break anything
		while (true) {
			//decide candidates based on borders and previous move
			ArrayList<Integer> candidates = new ArrayList<Integer>();
			int prev = moveLog.get(moveLog.size()-1);
			if (x-2 > 0 && prev != 1) candidates.add(0);
			if (x+2 < cells[0].length-1 && prev != 0) candidates.add(1);
			if (y-2 > 0 && prev != 3) candidates.add(2);
			if (y+2 < cells.length-1 && prev != 2) candidates.add(3);

			int move = candidates.get(rand.nextInt(candidates.size()));

			for (int i = 0; i < 2; i++) { //do twice so that each move moves 2 spaces
				//choose a direction and walk a step
				if (move == 0) x--;
				if (move == 1) x++;
				if (move == 2) y--;
				if (move == 3) y++;

				trail.add(cells[y][x]); //add the new piece of the trail
				cells[y][x].setColor(Color.RED); //change its color for collision and visual purposes

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

					for (int j = retrace + 1; j < trail.size(); j++) {
						trail.get(j).setColor(Color.BLACK);
					}

					trail.subList(retrace + 1, trail.size()).clear();
					moveLog.subList(retrace/2 + 1, moveLog.size()).clear();
					frame.repaint();

					break; //breaks the for loop to avoid more collision detection
				}

				//look for connections and commit if found
				if ( i == 0 && (	
					cells[y][x-1].getColor().equals(Color.WHITE) ||
					cells[y][x+1].getColor().equals(Color.WHITE) ||
					cells[y-1][x].getColor().equals(Color.WHITE) ||
					cells[y+1][x].getColor().equals(Color.WHITE))) {
					for (Cell c : trail) {
						c.setColor(Color.WHITE);
					}
					frame.repaint();
					return;
				}

				//look for diagonal connections to white and backtrack 1 if found
				// if (
				// 	cells[y-1][x-1].getColor().equals(Color.WHITE) ||
				// 	cells[y-1][x+1].getColor().equals(Color.WHITE) ||
				// 	cells[y+1][x-1].getColor().equals(Color.WHITE) ||
				// 	cells[y+1][x+1].getColor().equals(Color.WHITE)) 
				// {
				// 	trail.get(trail.size() - 1).setColor(Color.BLACK);
				// 	trail.remove(trail.size()-1);
				// 	moveLog.remove(trail.size()-1);
				// 	frame.repaint();
				// }
			} //end for

			moveLog.add(move);

				//repaint and create animation from delay
			frame.repaint();
			try {
				Thread.sleep(MILLISECONDSPERFRAME);
			} catch (InterruptedException e) {
				// Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	private Cell findNextStartCell(Cell lastStart) {
		//search bottom to top, left to right, for a black cell with two adjacent black cells

		boolean firstLoop = true;
		for (int y = cells.length-2; y >= 1; y--) {
			for (int x = 1; x <= cells[y].length-2; x++) {
				if (firstLoop) {
					x = lastStart.getx();
					y = lastStart.gety();
					firstLoop = false;
				}

				int adjacentBlackCounter = 0;

				if (cells[y][x-1].getColor().equals(Color.BLACK)) adjacentBlackCounter++;
				if (cells[y][x+1].getColor().equals(Color.BLACK)) adjacentBlackCounter++;
				if (cells[y-1][x].getColor().equals(Color.BLACK)) adjacentBlackCounter++;
				if (cells[y+1][x].getColor().equals(Color.BLACK)) adjacentBlackCounter++;
				if (cells[y-1][x-1].getColor().equals(Color.BLACK)) adjacentBlackCounter++;
				if (cells[y-1][x+1].getColor().equals(Color.BLACK)) adjacentBlackCounter++;
				if (cells[y+1][x-1].getColor().equals(Color.BLACK)) adjacentBlackCounter++;
				if (cells[y+1][x+1].getColor().equals(Color.BLACK)) adjacentBlackCounter++;

				if (adjacentBlackCounter == 8) {
					return cells[y][x];
				}
			}
		}
		return null;
	}
}
