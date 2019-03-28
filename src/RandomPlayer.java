import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomPlayer extends Player {
	public RandomPlayer(Side side) throws Exception {
		super(side);
	}

	public Move getMove() throws Exception {
		List<Move> availableMoves = board.availableMoves();
		if(availableMoves.size()==0){
			return null;
		} else {
		Random rand = new Random();
		int m = rand.nextInt(availableMoves.size());
		return availableMoves.get(m);
		}
	}
}
