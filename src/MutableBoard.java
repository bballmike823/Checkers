import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class MutableBoard extends Board {
	private Stack<Move> mutableMovesMade;
	private Stack<List<PiecePositionPair>> changedPieces;
	private Stack<Integer> lastImportantTurn;
	private CalculatedValue<Boolean> gameOverStatus;

	public MutableBoard(Player p1, Player p2) throws Exception {
		super(p1, p2);
		mutableMovesMade = new Stack<Move>();
		changedPieces = new Stack<List<PiecePositionPair>>();
		lastImportantTurn = new Stack<Integer>();
		lastImportantTurn.push(0);
		gameOverStatus = new CalculatedValue<Boolean>();
	}

	public MutableBoard(Board board) {
		super();
		player1 = board.getPlayer1();
		player2 = board.getPlayer2();
		turn = board.getTurn();
		gameBoard = new ArrayList<Piece>();
		for (int i = 0; i < size * size; i++) {
			gameBoard.add(new Piece(board.gameBoard.get(i)));
		}
		mutableMovesMade = new Stack<Move>();
		changedPieces = new Stack<List<PiecePositionPair>>();
		lastImportantTurn = new Stack<Integer>();
		lastImportantTurn.push(board.turnOfLastCaptureOrKing);
		gameOverStatus = new CalculatedValue<Boolean>();
	}

	public void makeMove(Move move) throws Exception {
		List<PiecePositionPair> changedThisTurn = new LinkedList<PiecePositionPair>();
		Player currentPlayer;
		if (turn % 2 == 1) {
			currentPlayer = player1;
		} else {
			currentPlayer = player2;
		}
		if (move == null) {

		} else {
			int finalPosition = move.getFinalPosition();
			int initialPosition = move.getInitialPosition();
			changedThisTurn.add(new PiecePositionPair(gameBoard.get(finalPosition), finalPosition));
			changedThisTurn.add(new PiecePositionPair(gameBoard.get(initialPosition), initialPosition));
			gameBoard.set(move.getFinalPosition(), gameBoard.get(move.getInitialPosition()));
			gameBoard.set(move.getInitialPosition(), new Piece(Side.EMPTY));
			if (getRow(move.getFinalPosition()) == 0 || getRow(move.getFinalPosition()) == 7) {
				gameBoard.get(move.getFinalPosition()).makeKing();
				if (lastImportantTurn.peek() != turn) {
					lastImportantTurn.push(turn);
				}
			}
			if (move.makesJump()) {
				for (int i = 0; i + 1 < move.getSize(); i++) {
					int row = (getRow(move.getPosition(i)) + getRow(move.getPosition(i + 1))) / 2;
					int col = (getColumn(move.getPosition(i)) + getColumn(move.getPosition(i + 1))) / 2;
					Side capSide = gameBoard.get(getPosition(row, col)).getSide();
					if (capSide.isEmpty()) {
						throw new Exception("cant capture empty space");
					} else if (capSide.equals(currentPlayer.getSide())) {
						throw new Exception("cant capture own piece");
					} else {
						changedThisTurn.add(
								new PiecePositionPair(gameBoard.get(getPosition(row, col)), getPosition(row, col)));
						gameBoard.set(getPosition(row, col), new Piece(Side.EMPTY));
					}
				}
				if (lastImportantTurn.peek() != turn) {
					lastImportantTurn.push(turn);
				}

			}
		}
		turn++;
		changedPieces.add(changedThisTurn);
		mutableMovesMade.add(move);
		availableMoves.reset();
		gameOverStatus.reset();
	}

	public boolean gameOver() {
		if (gameOverStatus.calculated()) {
			return gameOverStatus.getValue();
		} else if (numberOfRedPieces() == 0 || numberOfBlackPieces() == 0) {
			gameOverStatus.setValue(true);
			return true;
		} else if (turn - lastImportantTurn.peek() > 50) {
			gameOverStatus.setValue(true);
			return true;
		} else if (turn < 20) {
			gameOverStatus.setValue(false);
			return false;
		}
		try {
			gameOverStatus.setValue(availableMoves(Side.RED).isEmpty() && availableMoves(Side.BLACK).isEmpty());
			return gameOverStatus.getValue();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return true;
		}
	}
	public boolean softGameOver(){
		if (gameOverStatus.calculated()) {
			return gameOverStatus.getValue();
		} else if (numberOfRedPieces() == 0 || numberOfBlackPieces() == 0) {
			gameOverStatus.setValue(true);
			return true;
		} else if (turn - lastImportantTurn.peek() > 50) {
			gameOverStatus.setValue(true);
			return true;
		} else if (turn < 20) {
			gameOverStatus.setValue(false);
			return false;
		} else {
			return false;
		}
	}

	public Move getLastMove() {
		return mutableMovesMade.peek();
	}

	public void undoLastMove() {
		for (PiecePositionPair oldPiece : changedPieces.pop()) {
			gameBoard.set(oldPiece.getPosition(), oldPiece.getPiece());
		}
		if (lastImportantTurn.peek() == turn - 1) {
			lastImportantTurn.pop();
		}
		gameOverStatus.setValue(false);;
		availableMoves.reset();
		turn--;
	}
}
