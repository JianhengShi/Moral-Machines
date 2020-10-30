public class InvalidDataFormatException extends Exception {
	public InvalidDataFormatException() {
	}

	public void printMessage(int linecount) {
		System.out.print("WARNING: invalid data format in config file in line " + linecount+"\n");
	}
}