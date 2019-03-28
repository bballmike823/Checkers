import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class StandardMinimaxAIPlayer extends AiPlayer {
	public StandardMinimaxAIPlayer(Side SIDE) throws Exception {
		super(SIDE);

	}

	public StandardMinimaxAIPlayer(Side SIDE, int DEPTH) throws Exception {
		super(SIDE, DEPTH, DEPTH);
	}

	public Move getMove() throws Exception {
		MutableBoard b = new MutableBoard(board);
		return miniMax(depthOfSearch, b).getMove();
	}

	private MoveScorePair miniMax(int depth, MutableBoard b) throws Exception {
		boolean isCurrentPlayer = b.currentPlayer().equals(this);
		double bestScore = isCurrentPlayer ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
		Move bestMove = null;
		if (depth == 0 || b.gameOver()) {
			bestScore = evaluate(b);
		} else {
			List<Move> availableMoves = b.availableMoves();
			Collections.shuffle(availableMoves, randomGenerator);
			availableMoves = reorderedMoves(availableMoves,b);
			if (availableMoves.isEmpty()) {
				bestScore = miniMax(depth - 1, b).getScore();
			}
			for (Move move : availableMoves) {
				b.makeMove(move);
				double score = miniMax(depth - 1, b).getScore();
				b.undoLastMove();
				if (isCurrentPlayer) {
					if (score > bestScore) {
						bestScore = score;
						bestMove = move;
					}
				} else {
					if (score < bestScore) {
						bestScore = score;
						bestMove = move;
					}
				}
			}
		}
		return new MoveScorePair(bestMove, bestScore);
	}

	private double evaluate(Board evalBoard) {
		return getSide().isRed()? evalBoard.scoreDifferential() : -1*evalBoard.scoreDifferential();
	}

}