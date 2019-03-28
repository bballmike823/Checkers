
public class SimplePointsAI extends AiPlayer{

	public SimplePointsAI(Side SIDE) throws Exception {
		super(SIDE);
	}
	public SimplePointsAI(Side SIDE, int DEPTH) throws Exception {
		super(SIDE,DEPTH);
	}
	public SimplePointsAI(Side SIDE,int DEPTH, int MAXDEPTH) throws Exception {
		super(SIDE,DEPTH,MAXDEPTH);
	}
	@Override
	protected double evaluate(MutableBoard evalBoard){
		return getSide().isRed()? evalBoard.scoreDifferential() :-1 * evalBoard.scoreDifferential();
	}
}
