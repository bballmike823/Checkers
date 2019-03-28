import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Board {
	protected CalculatedValue<List<Move>> availableMoves;
	protected List<Move> movesMade;
	protected List<Piece> gameBoard;
	protected Player player1;
	protected Player player2;
	protected int turn;
	protected int turnOfLastCaptureOrKing;
	protected static final int size = 8;
	public static final int seed = 1;

	public Board() {
		initialize();
	}

	public Board(Player p1, Player p2) throws Exception {
		initialize();
		if (!p1.getSide().equals(p2.getSide())) {
			player1 = p1;
			player2 = p2;
		} else {
			throw new Exception("Players are on the same side");
		}

	}

	private void initialize() {
		turn = 1;
		turnOfLastCaptureOrKing = 0;
		movesMade = new ArrayList<Move>();
		try{
		availableMoves = new CalculatedValue<List<Move>>();
		}catch (Exception e){
			System.err.println("fuck");
			System.exit(0);
		}
		gameBoard = new ArrayList<Piece>(getSize() * getSize());
		for (int i = 0; i < size * size; i++) {
			gameBoard.add(new Piece(Side.EMPTY));
		}
		gameBoard.set(1, new Piece(Side.BLACK));
		gameBoard.set(3, new Piece(Side.BLACK));
		gameBoard.set(5, new Piece(Side.BLACK));
		gameBoard.set(7, new Piece(Side.BLACK));
		gameBoard.set(8, new Piece(Side.BLACK));
		gameBoard.set(10, new Piece(Side.BLACK));
		gameBoard.set(12, new Piece(Side.BLACK));
		gameBoard.set(14, new Piece(Side.BLACK));
		gameBoard.set(17, new Piece(Side.BLACK));
		gameBoard.set(19, new Piece(Side.BLACK));
		gameBoard.set(21, new Piece(Side.BLACK));
		gameBoard.set(23, new Piece(Side.BLACK));

		gameBoard.set(40, new Piece(Side.RED));
		gameBoard.set(42, new Piece(Side.RED));
		gameBoard.set(44, new Piece(Side.RED));
		gameBoard.set(46, new Piece(Side.RED));
		gameBoard.set(49, new Piece(Side.RED));
		gameBoard.set(51, new Piece(Side.RED));
		gameBoard.set(53, new Piece(Side.RED));
		gameBoard.set(55, new Piece(Side.RED));
		gameBoard.set(56, new Piece(Side.RED));
		gameBoard.set(58, new Piece(Side.RED));
		gameBoard.set(60, new Piece(Side.RED));
		gameBoard.set(62, new Piece(Side.RED));
	}

	// TODO
	public void makeMove(Move move) throws Exception {
		Player currentPlayer = currentPlayer();
		if (move == null) {
			if (!availableMoves().isEmpty()) {
				printToConsole();
				System.out.println(currentPlayer.getSide());
				for(Move m: availableMoves.getValue()){
					System.out.println(m.toString());
				}
				throw new Exception("Player has available moves");
			}
			turn++;
		} else if (isValidMove(move)) {
			gameBoard.set(move.getFinalPosition(), gameBoard.get(move.getInitialPosition()));
			gameBoard.set(move.getInitialPosition(), new Piece(Side.EMPTY));
			if (getRow(move.getFinalPosition()) == 0 || getRow(move.getFinalPosition()) == 7) {
				if(!gameBoard.get(move.getFinalPosition()).isKing()){
					gameBoard.get(move.getFinalPosition()).makeKing();
					turnOfLastCaptureOrKing = turn;
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
						gameBoard.set(getPosition(row, col), new Piece(Side.EMPTY));
						turnOfLastCaptureOrKing = turn;
					}
				}
			}
			turn++;
			availableMoves.reset();
		}

	}

	public void nextMove() throws Exception {
		makeMove(currentPlayer().getMove());
	}

	public Player currentPlayer() {
		if (turn % 2 == 1) {
			return player1;
		} else {
			return player2;
		}
	}

	public void print() throws Exception {
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				if (col != 0) {
					System.out.print("|");
				}
				System.out.print(readable(row * size + col));
			}
			System.out.println();
			if (row != size - 1) {
				for (int i = 0; i < size * 2 - 1; i++) {
					System.out.print("-");
				}
				System.out.println();
			}
		}
	}

	public void printToConsole() throws Exception {
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				if (col != 0) {
					System.out.print("|");
				}
				System.out.print(readable(row * size + col));
			}
			System.out.println();
			if (row != size - 1) {
				for (int i = 0; i < size * 2 - 1; i++) {
					System.out.print("-");
				}
				System.out.println();
			}
		}

	}

	private String readable(int position) throws Exception {
		return gameBoard.get(position).toString();
	}

	// TODO
	public boolean gameOver() {
		if (numberOfRedPieces() == 0 || numberOfBlackPieces() == 0) {
			return true;
		} else if (turn - turnOfLastCaptureOrKing > 50){
			return true;
		}
		try {
			return availableMoves(Side.RED).isEmpty() && availableMoves(Side.BLACK).isEmpty();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return true;
		}
	}

	public boolean redWins() {
		if(!gameOver()){
			return false;
		}
		scoreDifferential();
		if (scoreDifferential() > 0){
			return true;
		} else {
			return kingDifferential() > 0;
		}
	}

	public boolean blackWins() {
		if(!gameOver()){
			return false;
		}
		scoreDifferential();
		if (scoreDifferential() < 0){
			return true;
		} else {
			return kingDifferential() < 0;
		}
	}

	public boolean tie() {
		return (gameOver() && scoreDifferential() == 0 && kingDifferential() == 0);
	}

	public int scoreDifferential() {
		int redScore = 0;
		int blackScore = 0;
		for (int i = 0; i < getSize() * getSize(); i++) {
			if (isRed(i)) {
				redScore++;
			} else if (isBlack(i)) {
				blackScore++;
			}
		}
		return redScore - blackScore;
	}
	
	public int kingDifferential() {
		int redScore = 0;
		int blackScore = 0;
		for (int i = 0; i < getSize() * getSize(); i++) {
			if (isRed(i) && isKing(i)) {
				redScore++;
			} else if (isBlack(i) && isKing(i)) {
				blackScore++;
			}
		}
		return redScore - blackScore;
	}

	public String winner() {
		if (redWins()) {
			return "RED";
		} else if (blackWins()) {
			return "BLACK";
		} else if (tie()) {
			return "TIE";
		} else {
			return "game not over";
		}
	}

	public List<Move> availableMoves(Side side) {
		if (side == Side.EMPTY) {
			try {
				throw new Exception("cant get moves for null side");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (availableMoves.calculated() && side.equals(currentPlayer().getSide())) {
			return availableMoves.getValue();
		}
		List<Move> moves = new ArrayList<Move>();
		List<Integer> startingPoints = new LinkedList<Integer>();
		for (int position = 0; position < getSize() * getSize(); position++) {
			if (isSame(position, side)) {
				startingPoints.add(position);
			}
		}
		for (int position : startingPoints) {
			moves.addAll(getJumpsForPiece(position));
		}
		if (moves.isEmpty()) {
			for (int position : startingPoints) {
				moves.addAll(getMovesForPiece(position));
			}
		}
		if (side.equals(currentPlayer().getSide())) {
			availableMoves.setValue(moves);
		}
		return moves;
	}

	protected List<Move> getJumpsForPiece(int startingPosition) {
		Piece piece = gameBoard.get(startingPosition);
		if (!piece.isKing()) {
			if (piece.isRed()) {
				return redJumps(startingPosition);
			} else if (piece.isBlack()) {
				return blackJumps(startingPosition);
			}
		} else {
			return kingJumps(startingPosition);
		}
		return null;
	}

	protected List<Move> getMovesForPiece(int startingPosition) {
		Piece piece = gameBoard.get(startingPosition);
		if (!piece.isKing()) {
			if (piece.isRed()) {
				return upStandardMoves(startingPosition);
			} else if (piece.isBlack()) {
				return downStandardMoves(startingPosition);
			}
		} else {
			return kingStandardMoves(startingPosition);

		}
		return null;
	}

	private List<Move> redJumps(int startingPosition) {
		List<List<Integer>> intJumps = redIntegerJumps(startingPosition);
		List<Move> results = new LinkedList<Move>();
		for (List<Integer> moveList : intJumps) {
			List<Integer> temp = new LinkedList<Integer>();
			temp.add(startingPosition);
			temp.addAll(moveList);
			results.add(new Move(temp));
		}
		return results;

	}

	private List<List<Integer>> redIntegerJumps(int startingPosition) {
		List<List<Integer>> resultMoves = new LinkedList<List<Integer>>();
		int row = getRow(startingPosition);
		int column = getColumn(startingPosition);
		if (isInBounds(row - 1, column - 1) && isBlack(row - 1, column - 1)) {
			int newRow = row - 2;
			int newCol = column - 2;
			if (isInBounds(newRow, newCol) && isEmpty(newRow, newCol)) {
				List<List<Integer>> secondaryJumps = redIntegerJumps(getPosition(newRow, newCol));
				if (secondaryJumps.isEmpty()) {
					List<Integer> upLeft = new LinkedList<Integer>();
					upLeft.add(getPosition(newRow, newCol));
					resultMoves.add(upLeft);
				} else {
					for (List<Integer> moveList : secondaryJumps) {
						List<Integer> temp = new LinkedList<Integer>();
						temp.add(getPosition(newRow, newCol));
						temp.addAll(moveList);
						resultMoves.add(temp);
					}
				}
			}
		}
		if (isInBounds(row - 1, column + 1) && isBlack(row - 1, column + 1)) {
			int newRow = row - 2;
			int newCol = column + 2;
			if (isInBounds(newRow, newCol) && isEmpty(newRow, newCol)) {
				List<List<Integer>> secondaryJumps = redIntegerJumps(getPosition(newRow, newCol));
				if (secondaryJumps.isEmpty()) {
					List<Integer> upRight = new LinkedList<Integer>();
					upRight.add(getPosition(newRow, newCol));
					resultMoves.add(upRight);
				} else {
					for (List<Integer> moveList : secondaryJumps) {
						List<Integer> temp = new LinkedList<Integer>();
						temp.add(getPosition(newRow, newCol));
						temp.addAll(moveList);
						resultMoves.add(temp);
					}
				}
			}
		}
		return resultMoves;
	}

	private List<Move> blackJumps(int startingPosition) {
		List<List<Integer>> intJumps = blackIntegerJumps(startingPosition);
		List<Move> results = new LinkedList<Move>();
		for (List<Integer> moveList : intJumps) {
			List<Integer> temp = new LinkedList<Integer>();
			temp.add(startingPosition);
			temp.addAll(moveList);
			results.add(new Move(temp));
		}
		return results;

	}

	private List<List<Integer>> blackIntegerJumps(int startingPosition) {
		List<List<Integer>> resultMoves = new LinkedList<List<Integer>>();
		int row = getRow(startingPosition);
		int column = getColumn(startingPosition);
		if (isInBounds(row + 1, column - 1) && isRed(row + 1, column - 1)) {
			int newRow = row + 2;
			int newCol = column - 2;
			if (isInBounds(newRow, newCol) && isEmpty(newRow, newCol)) {
				List<List<Integer>> secondaryJumps = blackIntegerJumps(getPosition(newRow, newCol));
				if (secondaryJumps.isEmpty()) {
					List<Integer> downLeft = new LinkedList<Integer>();
					downLeft.add(getPosition(newRow, newCol));
					resultMoves.add(downLeft);
				} else {
					for (List<Integer> moveList : secondaryJumps) {
						List<Integer> temp = new LinkedList<Integer>();
						temp.add(getPosition(newRow, newCol));
						temp.addAll(moveList);
						resultMoves.add(temp);
					}
				}
			}
		}
		if (isInBounds(row + 1, column + 1) && isRed(row + 1, column + 1)) {
			int newRow = row + 2;
			int newCol = column + 2;
			if (isInBounds(newRow, newCol) && isEmpty(newRow, newCol)) {
				List<List<Integer>> secondaryJumps = blackIntegerJumps(getPosition(newRow, newCol));
				if (secondaryJumps.isEmpty()) {
					List<Integer> downRight = new LinkedList<Integer>();
					downRight.add(getPosition(newRow, newCol));
					resultMoves.add(downRight);
				} else {
					for (List<Integer> moveList : secondaryJumps) {
						List<Integer> temp = new LinkedList<Integer>();
						temp.add(getPosition(newRow, newCol));
						temp.addAll(moveList);
						resultMoves.add(temp);
					}
				}
			}
		}
		return resultMoves;
	}

	// TODO
	private List<Move> kingJumps(int startingPosition) {
		List<Integer> alreadyCaptured = new LinkedList<Integer>();
		List<List<Integer>> intJumps = kingIntegerJumps(startingPosition, alreadyCaptured,
				gameBoard.get(startingPosition).getSide());
		List<Move> results = new LinkedList<Move>();
		for (List<Integer> moveList : intJumps) {
			List<Integer> temp = new LinkedList<Integer>();
			temp.add(startingPosition);
			temp.addAll(moveList);
			results.add(new Move(temp));
		}
		return results;
	}

	private List<List<Integer>> kingIntegerJumps(int startingPosition, List<Integer> alreadyCaptured, Side kingSide) {
		List<List<Integer>> resultMoves = new LinkedList<List<Integer>>();
		int row = getRow(startingPosition);
		int column = getColumn(startingPosition);
		int capturedRow = row - 1;
		int capturedColumn = column - 1;
		if (isInBounds(capturedRow, capturedColumn) && isOpponent(kingSide, capturedRow, capturedColumn)
				&& !alreadyCaptured.contains(getPosition(capturedRow, capturedColumn))) {
			int newRow = row - 2;
			int newCol = column - 2;
			if (isInBounds(newRow, newCol) && isEmpty(newRow, newCol)) {
				alreadyCaptured.add(getPosition(capturedRow, capturedColumn));
				List<List<Integer>> secondaryJumps = kingIntegerJumps(getPosition(newRow, newCol), alreadyCaptured,
						kingSide);
				alreadyCaptured.remove(new Integer(getPosition(capturedRow, capturedColumn)));
				if (secondaryJumps.isEmpty()) {
					List<Integer> upLeft = new LinkedList<Integer>();
					upLeft.add(getPosition(newRow, newCol));
					resultMoves.add(upLeft);
				} else {
					for (List<Integer> moveList : secondaryJumps) {
						List<Integer> temp = new LinkedList<Integer>();
						temp.add(getPosition(newRow, newCol));
						temp.addAll(moveList);
						resultMoves.add(temp);
					}
				}
			}
		}
		capturedRow = row - 1;
		capturedColumn = column + 1;
		if (isInBounds(capturedRow, capturedColumn) && isOpponent(kingSide, capturedRow, capturedColumn)
				&& !alreadyCaptured.contains(getPosition(capturedRow, capturedColumn))) {
			int newRow = row - 2;
			int newCol = column + 2;
			if (isInBounds(newRow, newCol) && isEmpty(newRow, newCol)) {
				alreadyCaptured.add(getPosition(capturedRow, capturedColumn));
				List<List<Integer>> secondaryJumps = kingIntegerJumps(getPosition(newRow, newCol), alreadyCaptured,
						kingSide);
				alreadyCaptured.remove(new Integer(getPosition(capturedRow, capturedColumn)));
				if (secondaryJumps.isEmpty()) {
					List<Integer> upRight = new LinkedList<Integer>();
					upRight.add(getPosition(newRow, newCol));
					resultMoves.add(upRight);
				} else {
					for (List<Integer> moveList : secondaryJumps) {
						List<Integer> temp = new LinkedList<Integer>();
						temp.add(getPosition(newRow, newCol));
						temp.addAll(moveList);
						resultMoves.add(temp);
					}
				}
			}
		}
		capturedRow = row + 1;
		capturedColumn = column - 1;
		if (isInBounds(capturedRow, capturedColumn) && isOpponent(kingSide, capturedRow, capturedColumn)
				&& !alreadyCaptured.contains(getPosition(capturedRow, capturedColumn))) {
			int newRow = row + 2;
			int newCol = column - 2;
			if (isInBounds(newRow, newCol) && isEmpty(newRow, newCol)) {
				alreadyCaptured.add(getPosition(capturedRow, capturedColumn));
				List<List<Integer>> secondaryJumps = kingIntegerJumps(getPosition(newRow, newCol), alreadyCaptured,
						kingSide);
				alreadyCaptured.remove(new Integer(getPosition(capturedRow, capturedColumn)));
				if (secondaryJumps.isEmpty()) {
					List<Integer> downLeft = new LinkedList<Integer>();
					downLeft.add(getPosition(newRow, newCol));
					resultMoves.add(downLeft);
				} else {
					for (List<Integer> moveList : secondaryJumps) {
						List<Integer> temp = new LinkedList<Integer>();
						temp.add(getPosition(newRow, newCol));
						temp.addAll(moveList);
						resultMoves.add(temp);
					}
				}
			}
		}
		capturedRow = row + 1;
		capturedColumn = column + 1;
		if (isInBounds(capturedRow, capturedColumn) && isOpponent(kingSide, capturedRow, capturedColumn)
				&& !alreadyCaptured.contains(getPosition(capturedRow, capturedColumn))) {
			int newRow = row + 2;
			int newCol = column + 2;
			if (isInBounds(newRow, newCol) && isEmpty(newRow, newCol)) {
				alreadyCaptured.add(getPosition(capturedRow, capturedColumn));
				List<List<Integer>> secondaryJumps = kingIntegerJumps(getPosition(newRow, newCol), alreadyCaptured,
						kingSide);
				alreadyCaptured.remove(new Integer(getPosition(capturedRow, capturedColumn)));
				if (secondaryJumps.isEmpty()) {
					List<Integer> downRight = new LinkedList<Integer>();
					downRight.add(getPosition(newRow, newCol));
					resultMoves.add(downRight);
				} else {
					for (List<Integer> moveList : secondaryJumps) {
						List<Integer> temp = new LinkedList<Integer>();
						temp.add(getPosition(newRow, newCol));
						temp.addAll(moveList);
						resultMoves.add(temp);
					}
				}
			}
		}
		return resultMoves;
	}

	private boolean isOpponent(Side side, int row, int column) {
		if (side.isEmpty() || gameBoard.get(getPosition(row, column)).isEmpty()) {
			return false;
		}
		return !side.equals(gameBoard.get(getPosition(row, column)).getSide());
	}

	private List<Move> upStandardMoves(int startingPosition) {
		List<Move> resultMoves = new LinkedList<Move>();
		int row = getRow(startingPosition);
		int column = getColumn(startingPosition);
		if (isInBounds(row - 1, column - 1) && isEmpty(row - 1, column - 1)) {
			List<Integer> upLeft = new LinkedList<Integer>();
			upLeft.add(startingPosition);
			upLeft.add(getPosition(row - 1, column - 1));
			resultMoves.add(new Move(upLeft));
		}
		if (isInBounds(row - 1, column + 1) && isEmpty(row - 1, column + 1)) {
			List<Integer> upRight = new LinkedList<Integer>();
			upRight.add(startingPosition);
			upRight.add(getPosition(row - 1, column + 1));
			resultMoves.add(new Move(upRight));
		}
		return resultMoves;
	}

	private List<Move> downStandardMoves(int startingPosition) {
		List<Move> resultMoves = new LinkedList<Move>();
		int row = getRow(startingPosition);
		int column = getColumn(startingPosition);
		if (isInBounds(row + 1, column - 1) && isEmpty(row + 1, column - 1)) {
			List<Integer> upLeft = new LinkedList<Integer>();
			upLeft.add(startingPosition);
			upLeft.add(getPosition(row + 1, column - 1));
			resultMoves.add(new Move(upLeft));
		}
		if (isInBounds(row + 1, column + 1) && isEmpty(row + 1, column + 1)) {
			List<Integer> downRight = new LinkedList<Integer>();
			downRight.add(startingPosition);
			downRight.add(getPosition(row + 1, column + 1));
			resultMoves.add(new Move(downRight));
		}
		return resultMoves;
	}

	private List<Move> kingStandardMoves(int startingPosition) {
		List<Move> resultMoves = new LinkedList<Move>();
		int row = getRow(startingPosition);
		int column = getColumn(startingPosition);
		if (isInBounds(row - 1, column - 1) && isEmpty(row - 1, column - 1)) {
			List<Integer> upLeft = new LinkedList<Integer>();
			upLeft.add(startingPosition);
			upLeft.add(getPosition(row - 1, column - 1));
			resultMoves.add(new Move(upLeft));
		}
		if (isInBounds(row - 1, column + 1) && isEmpty(row - 1, column + 1)) {
			List<Integer> upRight = new LinkedList<Integer>();
			upRight.add(startingPosition);
			upRight.add(getPosition(row - 1, column + 1));
			resultMoves.add(new Move(upRight));
		}
		if (isInBounds(row + 1, column - 1) && isEmpty(row + 1, column - 1)) {
			List<Integer> downLeft = new LinkedList<Integer>();
			downLeft.add(startingPosition);
			downLeft.add(getPosition(row + 1, column - 1));
			resultMoves.add(new Move(downLeft));
		}
		if (isInBounds(row + 1, column + 1) && isEmpty(row + 1, column + 1)) {
			List<Integer> downRight = new LinkedList<Integer>();
			downRight.add(startingPosition);
			downRight.add(getPosition(row + 1, column + 1));
			resultMoves.add(new Move(downRight));
		}
		return resultMoves;
	}

	public List<Move> availableMoves() {
		return availableMoves(currentPlayer().getSide());
	}

	// TODO
	public boolean isValidMove(Move move, Side side) {
		if (gameBoard.get(move.getInitialPosition()).getSide() != side) {
			return false;
		} else if (isEmpty(move.getFinalPosition())) {
			try {
				List<Move> availableMoves = availableMoves();
				for (Move availableMove : availableMoves) {
					if (move.equals(availableMove)) {
						return true;
					}
				}
				return false;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

	public boolean isValidMove(Move move) {
		return isValidMove(move, currentPlayer().getSide());
	}

	protected Player getPlayer1() {
		return player1;
	}

	protected Player getPlayer2() {
		return player2;
	}

	protected int getTurn() {
		return turn;
	}

	public boolean isEmpty(int position) {
		return gameBoard.get(position).isEmpty();
	}

	public boolean isRed(int position) {
		return gameBoard.get(position).isRed();
	}

	public boolean isBlack(int position) {
		return gameBoard.get(position).isBlack();
	}

	public boolean isSame(int position, Side side) {
		return gameBoard.get(position).getSide().equals(side);
	}

	public boolean isEmpty(int row, int col) {
		return gameBoard.get(row * getSize() + col).isEmpty();
	}

	public boolean isRed(int row, int col) {
		return gameBoard.get(row * getSize() + col).isRed();
	}

	public boolean isBlack(int row, int col) {
		return gameBoard.get(row * getSize() + col).isBlack();
	}

	public boolean isKing(int row, int col) {
		return gameBoard.get(getPosition(row, col)).isKing();
	}
	public boolean isKing(int position){
		return gameBoard.get(position).isKing();
	}

	protected boolean isInBounds(int row, int column) {
		return (row >= 0 && row < getSize()) && (column >= 0 && column < getSize());
	}

	public static int getRow(int position) {
		return position / getSize();
	}

	public static int getColumn(int position) {
		return position % getSize();
	}

	public static int getPosition(int row, int column) {
		return row * getSize() + column;
	}

	public static int getSize() {
		return size;
	}

	public Move getHumanMove() throws Exception {
		System.out.println("Please enter your move");
		print();
		Scanner scanner = new Scanner(System.in);
		String moveString = scanner.nextLine();
		while (true) {
			if (Move.isProperFormat(moveString) && isValidMove(new Move(moveString))) {
				return new Move(moveString);
			} else {
				System.out.println("Invalid Move. Please enter another move. Available moves:");
				List<Move> availableMoves = availableMoves();
				for (Move m : availableMoves) {
					System.out.println(m.toString());
				}
				moveString = scanner.nextLine();
			}
		}
	}

	public int numberOfRedPieces() {
		int redTotal = 0;
		for (Piece piece : gameBoard) {
			if (piece.isRed()) {
				redTotal++;
			}
		}
		return redTotal;
	}

	public int numberOfBlackPieces() {
		int blackTotal = 0;
		for (Piece piece : gameBoard) {
			if (piece.isBlack()) {
				blackTotal++;
			}
		}
		return blackTotal;
	}
	
	public Piece getPiece(int position){
		return gameBoard.get(position);
	}
	public Move getLastMove() {
		return movesMade.get(movesMade.size() - 1);
	}

	public int getSeed() {
		return seed;
	}

	public void close() {

	}

}
