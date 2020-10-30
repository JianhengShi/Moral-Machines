public class InvalidCharacteristicException extends Exception {
	public InvalidCharacteristicException() {
	}
	
	public void printMessage(int linecount) {
		System.out.print("WARNING: invalid characteristic in config file in line " + linecount+"\n");
	}
}