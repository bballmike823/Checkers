import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

public class AiPlayer extends Player {
	protected Random randomGenerator;
	protected int depthOfSearch;
	private int maxDepth;

	public AiPlayer(Side SIDE) throws Exception {
		super(SIDE);
		depthOfSearch = 5;
		maxDepth = 5;
		randomGenerator = new Random();

	}

	public AiPlayer(Side SIDE, int DEPTH) throws Exception {
		super(SIDE);
		randomGenerator = new Random();
		depthOfSearch = DEPTH;
		maxDepth = DEPTH;
	}

	public AiPlayer(Side SIDE, int DEPTH, int MAXDEPTH) throws Exception {
		super(SIDE);
		randomGenerator = new Random();
		depthOfSearch = DEPTH;
		maxDepth = MAXDEPTH;
	}

	public Move getMove() throws Exception {
		MutableBoard b = new MutableBoard(board);
		MoveScorePair bestMove = miniMax(depthOfSearch, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, b, false);
		// System.out.println(b.getTurn()+" "+evaluate(b)+"
		// "+bestMove.getScore());
		return bestMove.getMove();
	}

	private MoveScorePair miniMax(int depth, double alpha, double beta, MutableBoard b, boolean deepened)
			throws Exception {
		boolean isCurrentPlayer = b.currentPlayer().getSide() == side;
		double bestScore = isCurrentPlayer ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
		Move bestMove = null;
		if (depth == 0 || b.gameOver()) {
			bestScore = evaluate(b);
		} else {
			List<Move> availableMoves = b.availableMoves();
			Collections.shuffle(availableMoves, randomGenerator);
			availableMoves = reorderedMoves(availableMoves, b);
			if (availableMoves.isEmpty()) {
				bestScore = miniMax(depth - 1, alpha, beta, b, deepened).getScore();
			}
			for (Move move : availableMoves) {
				b.makeMove(move);
				double score = miniMax(depth - 1, alpha, beta, b, deepened).getScore();
				b.undoLastMove();

				if (isCurrentPlayer) {
					if (score > bestScore) {
						bestScore = score;
						bestMove = move;
					}
					if (bestScore > alpha) {
						alpha = bestScore;
					}
				} else {
					if (score < bestScore) {
						bestScore = score;
						bestMove = move;
					}
					if (bestScore < beta) {
						beta = bestScore;
					}
				}
				if (alpha >= beta) {
					break;
				}
			}
		}
		return new MoveScorePair(bestMove, bestScore);
	}

	protected List<Move> reorderedMoves(List<Move> availableMoves, MutableBoard b) {
		List<Move> topPriority = new ArrayList<Move>();
		List<Move> lowPriority = new ArrayList<Move>();
		List<Move> reOrdered = new ArrayList<Move>();
		for (Move i : availableMoves) {
			if (isHighPriority(i, b)) {
				topPriority.add(i);
			} else {
				lowPriority.add(i);
			}
		}
		reOrdered.addAll(topPriority);
		reOrdered.addAll(lowPriority);
		return reOrdered;
	}

	protected boolean isHighPriority(Move move, MutableBoard b) {
		int row = Board.getRow(move.getFinalPosition());
		return (!b.isKing(move.getInitialPosition())) && (row == 0) || (row == b.getSize() - 1);
	}

	// this is shit
	protected double evaluate(MutableBoard evalBoard) {
		double score = 0;
		int redPieces = 0;
		int blackPieces = 0;
		int redKings = 0;
		int blackKings = 0;
		double pawnAdvancementScore = 0;
		for (int i = 0; i < evalBoard.getSize() * evalBoard.getSize(); i++) {
			Piece p = evalBoard.getPiece(i);
			if (p.isRed()) {
				redPieces++;
				if (p.isKing()) {
					redKings++;
				} else {
					pawnAdvancementScore += (7 - Board.getRow(i));
				}
			} else if (p.isBlack()) {
				blackPieces++;
				if (p.isKing()) {
					blackKings++;
				} else {
					pawnAdvancementScore += Board.getRow(i);
				}
			}
		}
		if (evalBoard.gameOver()) {
			int diff = redPieces - blackPieces;
			if (diff == 0) {
				score = redKings - blackKings;
			}
			return getSide() == Side.RED ? diff * 100000 :  diff * -100000;
		}
		double pieceValue = 1000;
		double kingValue = 200;
		double pawnAdvancementMultiplier = 1;
		double fortifiedScore = fortifiedSquares(evalBoard);
		double fortifiedMultiplier = 1;
		double centralityScore = centralityScore(evalBoard);
		double centralityMultiplier = 1;
		double vulnerabilityScore = vulnerablePieces(evalBoard);
		double vulnerabilityMultipler = 1;
		double blockedScore = blockedPieces(evalBoard);
		double blockedMultiplier = 1;
		int earlyGameTurn = 15;
		int midGameTurn = 35;
		
		if(evalBoard.getTurn() < earlyGameTurn){
			fortifiedMultiplier = 1;
			centralityMultiplier = 5;
			vulnerabilityMultipler = -15;
			pawnAdvancementMultiplier = 5;
			blockedMultiplier = 10;
		} else if(evalBoard.getTurn() < midGameTurn){
			fortifiedMultiplier = 0;
			centralityMultiplier = 0;
			vulnerabilityMultipler = -10;
			pawnAdvancementMultiplier = 10;
			blockedMultiplier = 1;
		} else {
			fortifiedMultiplier = 0;
			centralityMultiplier = 1;
			vulnerabilityMultipler = -10;
			pawnAdvancementMultiplier = 1;
			blockedMultiplier = 0;
		}
		score += pieceValue*redPieces;
		score -= pieceValue*blackPieces;
		score += kingValue*redKings;
		score -= kingValue*blackKings;
		score += fortifiedScore * fortifiedMultiplier;
		score += centralityScore * centralityMultiplier;
		score += vulnerabilityScore * vulnerabilityMultipler;
		score += pawnAdvancementScore * pawnAdvancementMultiplier;
		score += blockedScore * blockedMultiplier;
		
		return getSide() == Side.RED ? score : -1 * score;
	}

	private double vulnerablePieces(MutableBoard evalBoard) {
		int vulnerableReds = 0;
		int vulnerableBlacks = 0;
		for (int row = 1; row < evalBoard.getSize() - 1; row++) {
			for (int col = 1; col < evalBoard.getSize() - 1; col++) {
				Piece p = evalBoard.getPiece(evalBoard.getPosition(row, col));
				if (p.isRed()) {
					if (evalBoard.isBlack(row - 1, col - 1) && evalBoard.isEmpty(row + 1, col + 1)) {
						vulnerableReds++;
					} else if (evalBoard.isBlack(row - 1, col + 1) && evalBoard.isEmpty(row + 1, col - 1)) {
						vulnerableReds++;
					} else if (evalBoard.isBlack(row + 1, col - 1) && evalBoard.isKing(row + 1, col - 1)
							&& evalBoard.isEmpty(row - 1, col + 1)) {
						vulnerableReds++;
					} else if (evalBoard.isBlack(row + 1, col + 1) && evalBoard.isKing(row + 1, col + 1)
							&& evalBoard.isEmpty(row - 1, col - 1)) {
						vulnerableReds++;
					}
				} else if (p.isBlack()) {
					if (evalBoard.isRed(row + 1, col - 1) && evalBoard.isEmpty(row - 1, col + 1)) {
						vulnerableBlacks++;
					} else if (evalBoard.isRed(row + 1, col + 1) && evalBoard.isEmpty(row - 1, col - 1)) {
						vulnerableBlacks++;
					} else if (evalBoard.isRed(row - 1, col - 1) && evalBoard.isKing(row - 1, col - 1)
							&& evalBoard.isEmpty(row + 1, col + 1)) {
						vulnerableBlacks++;
					} else if (evalBoard.isRed(row - 1, col + 1) && evalBoard.isKing(row - 1, col + 1)
							&& evalBoard.isEmpty(row + 1, col - 1)) {
						vulnerableBlacks++;
					}
				}
			}
		}
		return vulnerableReds - vulnerableBlacks;
	}

	private double fortifiedSquares(MutableBoard evalBoard) {
		int fortifiedReds = 0;
		int fortifiedBlacks = 0;
		for (int row = 1; row < evalBoard.getSize() - 1; row++) {
			for (int col = 1; col < evalBoard.getSize() - 1; col++) {
				Piece p = evalBoard.getPiece(evalBoard.getPosition(row, col));
				if (p.isRed() && !p.isKing()) {
					int fortification = 0;
					if (evalBoard.isRed(row + 1, col + 1)) {
						fortification++;
					} else if (evalBoard.isEmpty(row + 1, col + 1) && !evalBoard.isRed(row - 1, col - 1)) {
						fortification--;
					}
					if (evalBoard.isRed(row + 1, col - 1)) {
						fortification++;
					} else if (evalBoard.isEmpty(row + 1, col - 1) && !evalBoard.isRed(row - 1, col + 1)) {
						fortification--;
					}
					if(fortification > 0){
						fortifiedReds ++;
					}
				} else if (p.isBlack() && !p.isKing()) {
					int fortification = 0;
					if (evalBoard.isBlack(row - 1, col + 1)) {
						fortification++;
					} else if (evalBoard.isEmpty(row - 1, col + 1) && !evalBoard.isBlack(row + 1, col - 1)) {
						fortification--;
					}
					if (evalBoard.isBlack(row - 1, col - 1)) {
						fortification++;
					} else if (evalBoard.isEmpty(row - 1, col - 1) && !evalBoard.isBlack(row + 1, col + 1)) {
						fortification--;
					}
					if(fortification > 0){
						fortifiedBlacks ++;
					}

				}
			}
		}
		return fortifiedReds - fortifiedBlacks;
	}

	private double blockedPieces(MutableBoard evalBoard) {
		int fortifiedReds = 0;
		int fortifiedBlacks = 0;
		for (int row = 1; row < evalBoard.getSize() - 1; row++) {
			for (int col = 1; col < evalBoard.getSize() - 1; col++) {
				Piece p = evalBoard.getPiece(evalBoard.getPosition(row, col));
				if (p.isRed() && !p.isKing()) {
					int fortification = 0;
					if (evalBoard.isRed(row + 1, col + 1) && evalBoard.isBlack(row - 1, col - 1)) {
						fortification++;
					}
					if (evalBoard.isRed(row + 1, col - 1) && evalBoard.isBlack(row - 1, col + 1)) {
						fortification++;
					}
					fortifiedReds += fortification;
				} else if (p.isBlack() && !p.isKing()) {
					int fortification = 0;
					if (evalBoard.isBlack(row - 1, col + 1) && evalBoard.isRed(row + 1, col - 1)) {
						fortification++;
					}
					if (evalBoard.isBlack(row - 1, col - 1) && evalBoard.isRed(row + 1, col + 1)) {
						fortification++;
					}
					fortifiedBlacks += fortification;

				}
			}
		}
		return fortifiedReds - fortifiedBlacks;
	}

	private double centralityScore(MutableBoard evalBoard) {
		double score = 0;
		for (int row = 0; row < evalBoard.getSize(); row++) {
			for (int col = 0; col < evalBoard.getSize(); col++) {
				Piece p = evalBoard.getPiece(evalBoard.getPosition(row, col));
				if (p.isRed()) {
					score += rowScore(row) * rowScore(col);
				} else if (p.isBlack()) {
					score -= rowScore(row) * rowScore(col);
				}
			}
		}
		return score;
	}

	private double rowScore(int row) {
		if (row == 3 || row == 4) {
			return 1;
		} else if (row == 2 || row == 5) {
			return .9;
		} else if (row == 1 || row == 6) {
			return .5;
		} else {
			return 0;
		}
	}

	double cornerBonus(MutableBoard evalBoard) {
		double score = 0;
		if (evalBoard.isRed(1)) {
			score++;
		} else if (evalBoard.isBlack(1)) {
			score++;
		}
		if (evalBoard.isRed(8)) {
			score++;
		} else if (evalBoard.isBlack(8)) {
			score++;
		}
		if (evalBoard.isRed(55)) {
			score++;
		} else if (evalBoard.isBlack(55)) {
			score++;
		}
		if (evalBoard.isRed(62)) {
			score++;
		} else if (evalBoard.isBlack(62)) {
			score++;
		}

		if (evalBoard.isRed(56)) {
			score++;
		} else if (evalBoard.isBlack(56)) {
			score++;
		}
		if (evalBoard.isRed(7)) {
			score++;
		} else if (evalBoard.isBlack(7)) {
			score++;
		}
		return score;
	}
}
