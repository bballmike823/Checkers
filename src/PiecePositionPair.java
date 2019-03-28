
public class PiecePositionPair {
	private Piece piece;
	private int location;

	public PiecePositionPair(Piece p, int position) {
		location = position;
		piece = p;
	}

	public Piece getPiece() {
		return piece;
	}

	public int getPosition() {
		return location;
	}
}
