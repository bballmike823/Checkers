
public enum Side {
	RED,BLACK,EMPTY;
	public boolean isEmpty(){
		return this.equals(EMPTY);
	}
	public boolean isRed(){
		return this.equals(RED);
	}
	public boolean isBlack(){
		return this.equals(BLACK);
	}
}
