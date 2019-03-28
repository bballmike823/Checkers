
public class CalculatedValue<Type> {
	private Type value;
	private boolean calculated;
	CalculatedValue(){
		value = null;
		calculated = false;
	}
	CalculatedValue(Type VALUE){
		value = VALUE;
		calculated = true;
	}
	public Type getValue(){
		return value;
	}
	public void setValue(Type VALUE){
		value = VALUE;
		calculated = true;
	}
	public boolean calculated(){
		return calculated;
	}
	public void reset(){
		calculated = false;
		value = null;
		
	}
}
