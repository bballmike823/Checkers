
public class Piece {
	public enum Type {
		STANDARD, KING
	}

	private Side side;
	private Type type;

	// default to empty piece.
	public Piece() {
		side = Side.EMPTY;
		type = Type.STANDARD;
	}

	public Piece(Side pieceSide) {
		side = pieceSide;
		type = Type.STANDARD;
	}

	public Piece(Piece p) {
		side = p.side;
		type = p.type;
	}

	public boolean isEmpty() {
		return side == Side.EMPTY;
	}

	public boolean isRed() {
		return side == Side.RED;
	}

	public boolean isBlack() {
		return side == Side.BLACK;
	}

	public Side getSide(){
		return side;
	}
	public boolean isKing() {
		return type == Type.KING;
	}

	public void makeKing() {
		type = Type.KING;
	}

	public String toString() {
		switch (side) {
		case RED:
			switch (type) {
			case STANDARD:
				return "r";
			case KING:
				return "R";
			}
		case BLACK:
			switch (type) {
			case STANDARD:
				return "b";
			case KING:
				return "B";
			}
		case EMPTY:
			return " ";
		}
		return null;
	}
}
