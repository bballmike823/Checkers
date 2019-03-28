import java.util.ArrayList;
import java.util.Scanner;

public class Player {
	protected Side side;
	protected Board board;

	public Player(Side playerSide) throws Exception {
		if(playerSide.equals(Side.EMPTY)){
			throw new Exception("player must be red or black");
		} else {
			side = playerSide;
		}
	}

	public Side getSide() {
		return side;
	}

	public void setBoard(Board gameBoard) {
		board = gameBoard;
	}

	public Move getMove() throws Exception {
		return board.getHumanMove();

	}

}
