import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.*;

public class GUIBoard extends Board {
	private GUI gui;
	private int rowSelected;
	private int colSelected;
	private List<Integer> positionsSelected;
	private List<Integer> possibleFinalLocations;
	private boolean moveMade;

	public GUIBoard(Player player1, Player player2) throws Exception {
		super(player1, player2);
		rowSelected = -1;
		colSelected = -1;
		moveMade = false;
		positionsSelected = new LinkedList<Integer>();
		possibleFinalLocations = new LinkedList<Integer>();
		gui = new GUI();
		gui.setPreferredSize(new Dimension(500, 500));
		gui.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int mouseX = e.getX();
				int mouseY = e.getY();
				rowSelected = mouseY * getSize() / gui.getHeight();
				colSelected = mouseX * getSize() / gui.getWidth();
				if (gameBoard.get(getPosition(rowSelected, colSelected)).getSide() == currentPlayer().getSide()) {
					positionsSelected = new LinkedList<Integer>();
					positionsSelected.add(getPosition(rowSelected, colSelected));
					List<Move> availableMoves = getJumpsForPiece(getPosition(rowSelected, colSelected));
					if(availableMoves.isEmpty()){
						availableMoves = getMovesForPiece(getPosition(rowSelected, colSelected));
					}
					possibleFinalLocations = new LinkedList<Integer>();
					for(Move move:availableMoves){
						possibleFinalLocations.add(move.getFinalPosition());
					}
					
				} else{
					positionsSelected.add(getPosition(rowSelected, colSelected));
				}
				if (positionsSelected.size()>1&&isValidMove(new Move(positionsSelected))) {
					moveMade = true;
				}
			}
		});
		gui.pack();
		gui.setVisible(true);
		gui.repaint();
	}

	@Override
	public void print() throws Exception {
		gui.repaint();
	}

	@Override
	public Move getHumanMove() throws Exception {
		while (positionsSelected.isEmpty()) {
			Thread.sleep(10);
		}
		while (!moveMade) {
			gui.repaint();
			Thread.sleep(10);
		}
		Move move = new Move(positionsSelected);
		if (isValidMove(move)) {
			moveMade = false;
			positionsSelected = new LinkedList<Integer>();
			possibleFinalLocations = new LinkedList<Integer>();
			return move;
		} else {
			return getHumanMove();
		}
	}

	@Override
	public void makeMove(Move move) throws Exception {
		super.makeMove(move);
		gui.repaint();
	}

	@Override
	public void close() {
		gui.setVisible(false);
	}

	private class GUI extends JFrame {
		GridLayout gridLayout;

		public GUI() {
			super();
			gridLayout = new GridLayout(size, size);
			this.setLayout(gridLayout);
			for (int row = 0; row < size; row++) {
				for (int col = 0; col < size; col++) {
					add(new Cell(row, col));
				}
			}
		}

		private class Cell extends JComponent {
			int row;
			int column;
			Color backgroundColor;

			public Cell(int ROW, int COL) {
				super();
				row = ROW;
				column = COL;
				this.setPreferredSize(new Dimension(50, 50));
				backgroundColor = (row + column) % 2 == 0 ? Color.DARK_GRAY : Color.LIGHT_GRAY;
			}

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(backgroundColor);
				g.fillRect(0, 0, getWidth(), getHeight());
				if (!isEmpty(row, column)) {
					g.setColor(getColor(row, column));
					g.fillOval(0, 0, getWidth(), getHeight());
					if(isKing(row,column))
					{
						g.setColor(Color.WHITE);
						g.drawOval(0, 0, getWidth(), getHeight());
						g.drawOval(10,10,getWidth() - 20, getHeight() - 20);
					}
				}

				if (positionsSelected.contains(getPosition(row, column))) {
					g.setColor(Color.yellow);
					g.fillOval(getWidth() / 4, getHeight() / 4, getWidth() / 2, getHeight() / 2);
				}
				if (possibleFinalLocations.contains(getPosition(row, column))) {
					g.setColor(Color.CYAN);
					g.fillOval(getWidth() / 4, getHeight() / 2, getWidth() / 2, getHeight() / 2);
				}

			}

			private Color getColor(int row, int column) {
				if (isBlack(row, column)) {
					return Color.BLACK;
				} else if (isRed(row, column)) {
					return Color.RED;
				} else {
					return Color.BLUE;
				}
			}
		}
	}
}
