public class NumberFormatException extends Exception {
	public NumberFormatException() {
	}
	
	public void printMessage(int linecount) {
		System.out.print("WARNING: invalid number format in config file in line " + linecount+"\n");
	}
}
