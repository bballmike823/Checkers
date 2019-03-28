import java.util.LinkedList;
import java.util.List;

public class Move {
	private List<Integer> positions;
	private int size;
	private boolean jump;
	public Move(List<Integer> squares) {
		positions = new LinkedList<Integer>();
		for(int p:squares){
			positions.add(p);
		}
		size = positions.size();
		int row1 = Board.getRow(getPosition(0));
		int row2 = Board.getRow(getPosition(1));
		jump = Math.abs(row1 - row2) == 2;
	}
	// Takes string in form p1:p2:p3
	public Move(String moveString){
		positions = new LinkedList<Integer>();
		String[] temp = moveString.split(":");
		for(String p:temp){
			int position = Integer.parseInt(p);
			if(isValidPosition(position)){
				positions.add(position);
			} else {
				try {
					throw new Exception("invalid position");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		size = positions.size();
		int row1 = Board.getRow(getPosition(0));
		int row2 = Board.getRow(getPosition(1));
		jump = Math.abs(row1 - row2) == 2;
	}
	
	private boolean isValidPosition(int position){
		return position >= 0 && position < 64;
	}
	public static boolean isProperFormat(String moveString){
		for(int index = 0; index < moveString.length();index++){
			char temp = moveString.charAt(index);
			boolean valid = (temp == ':') ||(temp >= '0' && temp <= '9');
			if(!valid){
				return false;
			}
		}
		return true;
	}

	public int getInitialPosition() {
		return positions.get(0);
		}
	public int getFinalPosition(){
		return positions.get(size - 1);
	}
	public int getPosition(int index){
		return positions.get(index);
	}
	public int getSize(){
		return size;
	}

	public String toString() {
		String result = "";
		for(int position: positions){
			result += " " + position;
		}
		result = result.trim();
		return result;
				
	}
	public boolean equals(Move move){
		if(size != move.getSize()){
			return false;
		}
		for(int i = 0; i < size;i++){
			if(getPosition(i)!=move.getPosition(i)){
				return false;
			}
		}
		return true;
	}
	public boolean makesJump(){
		return jump;
	}
}
