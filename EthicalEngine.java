import java.io.*;
import java.util.*;
import java.util.Scanner;

import ethicalengine.*;
import ethicalengine.Character;
import ethicalengine.Character.BodyType;
import ethicalengine.Character.Gender;
import ethicalengine.Person.Profession;

/**
 * EthicalEngine system class.
 * 
 * @author Jianheng Shi. SID: 1087943. Username: jianheng
 */
public class EthicalEngine {
	public static Scanner keyboard = new Scanner(System.in);
	public static ArrayList<Scenario> scenarios = new ArrayList<Scenario>();
	private static String autoSavePath = "results.log";
	private static String manualSavePath = "user.log";
	private static String CSVpath = "";
	private static String welcomePath = "welcome.ascii";
	private static boolean interactiveMode = false;
	private static boolean importFromCSV = false;
	private static boolean showHelp = false;
	private static boolean startEngine = false;
	private static boolean save = false;
	private static boolean exit = false;

	/**
	 * Entry of Ethical Engine.
	 * 
	 * @param args User input command using for launch the Ethical Engine.
	 * @return Nothing.
	 */
	public static void main(String[] args) {
		parseUserCommand(args);
		if (!startEngine && showHelp) {
			help();
			System.exit(0);
		}
		if (importFromCSV) {
			try {
				importScenario(readFromCSV(CSVpath));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (startEngine) {
			welcome(welcomePath);
			saveConsent();
			if (!interactiveMode) {
				autoAuditandSave();
			} else {
				if (!importFromCSV) {
					manualAuditandSavewithRandomScenarios();
				} else {
					manualAuditandSavewithImportScenarios();
				}
			}
		}
		if (exit) {
			keyboard.close();
			System.exit(0);
		}
	}

	/**
	 * This method returns choose of save passenger or pedestrian generate by
	 * Ethical Engine algorithm. Principles of decision: Priority protection of the
	 * weak, priority protection of pedestrians, priority protection of humans.
	 * 
	 * @param A scenario needs to be decide.
	 * @return Decision in enumeration.
	 */
	public static Decision decide(Scenario scenario) {
		int passengerScore = 0;
		int pedestrianScore = 0;
		boolean pedestrainsAllAnimal = true;
		for (Character passenger : scenario.getPassengers()) {
			if (passenger instanceof Person) {
				Person personPassenger = (Person) passenger;
				if (personPassenger.getGender().name().equals("FEMALE")) {
					passengerScore += 5;
				}
				if (personPassenger.getBodyType().name().equals("OVERWEIGHT")) {
					passengerScore += 2;
				}
				if (personPassenger.getAgeCategory().name().equals("BABY")) {
					passengerScore += 10;
				}
				if (personPassenger.getAgeCategory().name().equals("CHILD")) {
					passengerScore += 5;
				}
				if (personPassenger.getAgeCategory().name().equals("SENIOR")) {
					passengerScore += 5;
				}
				if (personPassenger.isPregnant() == true) {
					passengerScore += 15;
				}
			}
		}
		for (Character pedestrian : scenario.getPedestrians()) {
			if (pedestrian instanceof Person) {
				Person personPedestrain = (Person) pedestrian;
				if (personPedestrain.getGender().name().equals("FEMALE")) {
					pedestrianScore += 5;
				}
				if (personPedestrain.getBodyType().name().equals("OVERWEIGHT")) {
					pedestrianScore += 2;
				}
				if (personPedestrain.getAgeCategory().name().equals("BABY")) {
					pedestrianScore += 10;
				}
				if (personPedestrain.getAgeCategory().name().equals("CHILD")) {
					pedestrianScore += 5;
				}
				if (personPedestrain.getAgeCategory().name().equals("SENIOR")) {
					pedestrianScore += 5;
				}
				if (personPedestrain.isPregnant() == true) {
					pedestrianScore += 15;
				}
				pedestrainsAllAnimal = false;
			}
		}
		if (scenario.isLegalCrossing() == true) {
			pedestrianScore += scenario.getPedestrians().length * 5;
		}
		// if pedestrians are all animal, save passengers first.
		if (pedestrainsAllAnimal) {
			return Decision.PASSENGERS;
		}
		// Return result according to scores.
		if (passengerScore > pedestrianScore) {
			return Decision.PASSENGERS;
		} else {
			return Decision.PEDESTRIANS;
		}
	}

	/**
	 * Enumeration of Decisions. PASSENGERS means save passenger, PEDESTRIANS means
	 * save pedestrian.
	 */
	public enum Decision {
		PEDESTRIANS, PASSENGERS
	}

	/**
	 * Used for parse user configuration input command and set system
	 * configurations.
	 * 
	 * @param userCommand User input command.
	 * @return Nothing.
	 */
	private static void parseUserCommand(String[] commandList) {
		startEngine = true;
		// command -i or --interactive
		if (contains(commandList, "-i") || contains(commandList, "--interactive")) {
			interactiveMode = true;
		}
		// command -h or --help
		if (contains(commandList, "-h") || contains(commandList, "--help")) {
			showHelp = true;
			startEngine = false;
		}
		// command -c or --config
		if ((contains(commandList, "-c") || contains(commandList, "--config"))) {
			// without or with CSV path behind
			if (contains(commandList, "-c")) {
				if (pathBehind(commandList, "-c").isEmpty()) {
					showHelp = true;
					startEngine = false;
				} else {
					importFromCSV = true;
					CSVpath = pathBehind(commandList, "-c");
				}
			}
			if (contains(commandList, "--config")) {
				if (pathBehind(commandList, "--config").isEmpty()) {
					showHelp = true;
					startEngine = false;
				} else {
					importFromCSV = true;
					CSVpath = pathBehind(commandList, "--config");
				}
			}
		}
		// command -r or --results
		if (contains(commandList, "-r") || contains(commandList, "--results")) {
			// without or with save path behind
			if (contains(commandList, "-r")) {
				if (!pathBehind(commandList, "-r").isEmpty()) {
					autoSavePath = pathBehind(commandList, "-r");
					manualSavePath = pathBehind(commandList, "-r");
					File f = new File(autoSavePath);
					try {
						FileWriter fr = new FileWriter(f, true);
						fr.close();
					} catch (Exception e) {
						// TODO: handle exception
						if (e instanceof FileNotFoundException) {
							System.out.print("ERROR: could not print results. Target directory does not exist.\n");
							startEngine = false;
						}
					}
				}
			}
			if (contains(commandList, "--results")) {
				if (!pathBehind(commandList, "--results").isEmpty()) {
					autoSavePath = pathBehind(commandList, "-results");
					manualSavePath = pathBehind(commandList, "-results");
					File f = new File(autoSavePath);
					try {
						FileWriter fr = new FileWriter(f, true);
						fr.close();
					} catch (Exception e) {
						// TODO: handle exception
						if (e instanceof FileNotFoundException) {
							System.out.print("ERROR: could not print results. Target directory does not exist.\n");
							startEngine = false;
						}
					}
				}
			}
		}

	}

	/**
	 * This method read from welcome.ascii file and print it into console.
	 * 
	 * @param path Path of welcome.ascii file.
	 * @return Nothing.
	 */
	private static void welcome(String path) {
		try {
			File f = new File(path);
			FileReader fr = new FileReader(f);
			BufferedReader csv = new BufferedReader(fr);
			String str;
			while ((str = csv.readLine()) != null) {
				System.out.print(str + "\n");
			}
			csv.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/**
	 * This method gets consent of saving result file from user's console input.
	 * 
	 * @return Nothing.
	 */
	private static void saveConsent() {
		String scan;
		System.out.print("Do you consent to have your decisions saved to a file? (yes/no)\n");
		scan = keyboard.nextLine();
		while (true) {
			if (scan.equals("yes")) {
				save = true;
				break;
			} else if (scan.equals("no")) {
				save = false;
				break;
			} else {
				System.out.print("Invalid response. Do you consent to have your decisions saved to a file? (yes/no)\n");
				System.out.print("$");
				scan = keyboard.nextLine();
				continue;
			}
		}
	}

	/**
	 * This method ask user for whether continue with new scenarios or not.
	 * 
	 * @return Whether the user agrees to continue or not.
	 */
	private static boolean continueConsent() {
		String scan;
		System.out.print("Would you like to continue? (yes/no)\n");
		scan = keyboard.nextLine();
		while (true) {
			if (scan.equals("yes")) {
				return true;
			} else if (scan.equals("no")) {
				return false;
			}
		}
	}

	/**
	 * This method print out help documentation to the console.
	 */
	private static void help() {
		System.out.printf("EthicalEngine - COMP90041 - Final Project%n%n" + "Usage: java EthicalEngine [arguments]%n%n"
				+ "Arguments:%n" + "   -c or --config\tOptional: path to config file%n"
				+ "   -h or --help\t\tPrint Help (this message) and exit%n"
				+ "   -r or --results\tOptional: path to results log file%n"
				+ "   -i or --interactive\tOptional: launches interactive mode%n");
	}

	/**
	 * This method generates random scenarios and save them into local scenarios
	 * space of the Ethical Engine.
	 * 
	 * @param num Number of scenarios will be generated.
	 */
	private static void randomScenario(int num) {
		ScenarioGenerator sg = new ScenarioGenerator();
		for (int i = 0; i < num; i++) {
			Scenario randomS1 = sg.generate();
			scenarios.add(randomS1);
		}
	}

	/**
	 * This method audit scenarios in local scenarios space automatically then save
	 * the result to results.log according to parameter save.
	 * 
	 * @return Nothing.
	 */
	private static void autoAuditandSave() {
		if (!importFromCSV) {
			randomScenario(3);
		}
		Scenario[] s = new Scenario[scenarios.size()];
		Audit audit = new Audit(scenarios.toArray(s));
		audit.run();
		if (!importFromCSV) {
			for (int i = 0; i < 3; i++) {
				System.out.print(scenarios.get(i).toString() + "\n");
				System.out.print("Who should be saved? (passenger(s) [1] or pedestrian(s) [2])\n");
				if (audit.getDecisions()[i]) {
					System.out.print("$ passenger\n");
				}
				if (!audit.getDecisions()[i]) {
					System.out.print("$ pedestrians\n");
				}
			}
		} else {
			for (int i = 0; i < scenarios.size(); i++) {
				System.out.print(scenarios.get(i).toString() + "\n");
				System.out.print("Who should be saved? (passenger(s) [1] or pedestrian(s) [2])\n");
				if (audit.getDecisions()[i]) {
					System.out.print("$ passenger\n");
				}
				if (!audit.getDecisions()[i]) {
					System.out.print("$ pedestrians\n");
				}
			}
		}
		audit.printStatistic();
		if (save) {
			if (!(autoSavePath.isEmpty())) {
				audit.printToFile(autoSavePath);
			}
		}
		if (!scenarios.isEmpty()) {
			exit = true;
		} else {
			exit = false;
		}
	}

	/**
	 * This method automatically generate random scenarios and ask user manually
	 * audit them then save the result to results.log according to parameter save.
	 * 
	 * @return Nothing.
	 */
	private static void manualAuditandSavewithRandomScenarios() {
		do {
			randomScenario(3);
			String scan;
			Scenario[] a = {};
			Audit audit = new Audit(scenarios.toArray(a));
			boolean[] decisions = new boolean[scenarios.size()];
			audit.setAuditType("User");

			for (int i = scenarios.size() - 3; i < scenarios.size(); i++) {
				System.out.print(scenarios.get(i).toString() + "\n");
				System.out.print("Who should be saved? (passenger(s) [1] or pedestrian(s) [2])\n");
				// System.out.print("$");
				scan = keyboard.nextLine();
				if (scan.equals("passenger") || scan.equals("passengers") || scan.equals("1")) {
					decisions[i] = true;
				}
				if (scan.equals("pedestrian") || scan.equals("pedestrians") || scan.equals("2")) {
					decisions[i] = false;
				}
			}

			audit.setDecisions(decisions);
			audit.printStatistic();

			if (save) {
				audit.printToFile(manualSavePath);
			}
			if (!scenarios.isEmpty()) {
				exit = true;
			} else {
				exit = false;
			}
		} while (continueConsent());
	}

	/**
	 * This method ask user manually audit scenarios imported from CSV file in local
	 * scenarios space then save the result to results.log according to parameter
	 * save.
	 * 
	 * @return Nothing.
	 */
	private static void manualAuditandSavewithImportScenarios() {
		String scan;
		Scenario[] a = {};
		Scenario[] allScenarios = scenarios.toArray(a);
		int scenarioNo = 0;
		boolean[] decisions = new boolean[scenarios.size()];

		do {
			for (int i = scenarioNo; i < scenarioNo + 3; i++) {
				System.out.print(scenarios.get(i).toString() + "\n");
				System.out.print("Who should be saved? (passenger(s) [1] or pedestrian(s) [2])\n");
				// System.out.print("$");
				scan = keyboard.nextLine();
				if (scan.equals("passenger") || scan.equals("passengers") || scan.equals("1")) {
					decisions[i] = true;
				}
				if (scan.equals("pedestrian") || scan.equals("pedestrians") || scan.equals("2")) {
					decisions[i] = false;
				}
				if (i == scenarios.size() - 1) {
					Audit audit = new Audit(Arrays.copyOf(allScenarios, i + 1));
					audit.setAuditType("User");
					audit.setDecisions(decisions);
					audit.printStatistic();
					System.out.print("That's all. Press Enter to quit.\n");
					try {
						System.in.read();
						System.exit(0);
					} catch (IOException e) {
						// TODO Auto-generated catch block
					}
				}
			}
			scenarioNo += 3;
			Audit audit = new Audit(Arrays.copyOf(allScenarios, scenarioNo));
			audit.setAuditType("User");
			audit.setDecisions(decisions);
			audit.printStatistic();

			if (save) {
				audit.printToFile(manualSavePath);
			}
			if (!scenarios.isEmpty()) {
				exit = true;
			} else {
				exit = false;
			}
		} while (continueConsent());
	}

	/**
	 * This method read from CSV file of given path. Return a line-by-line String
	 * array of its content.
	 * 
	 * @param filepath Given path of the CSV file.
	 * @return Content of CSV file by lines.
	 * @throws FileNotFoundException On given file path.
	 */
	private static String[] readFromCSV(String filepath) throws FileNotFoundException {
		ArrayList<String> lines = new ArrayList<>();
		File f = new File(filepath);
		if (!f.exists()) {
			throw new FileNotFoundException();
		}
		FileReader fr = new FileReader(f);
		BufferedReader csv = new BufferedReader(fr);
		String str;
		try {
			while ((str = csv.readLine()) != null) {
				lines.add(str);
			}
			csv.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] content = new String[lines.size()];
		for (int i = 0; i < lines.size(); i++) {
			content[i] = lines.get(i);
		}
		return content;
	}

	/**
	 * This method parse content of the CSV file, then add to local scenarios.
	 * 
	 * @param content Content of CSV file by lines.
	 * @return Nothing.
	 */
	public static void importScenario(String[] content) {
		for (int i = 1; i < content.length; i++) {
			String thisLine = content[i];
			String[] splitLine = thisLine.split(",");
			boolean isLegalCrossing = true;
			if (splitLine[0].matches("scenario:(.*)")) {
				if (splitLine[0].matches("(.*)green")) {
					isLegalCrossing = true;
				} else if (splitLine[0].matches("(.*)red")) {
					isLegalCrossing = false;
				}
				scenarios.add(new Scenario(isLegalCrossing));
			} else if (splitLine[0].equals("person") || splitLine[0].equals("animal")) {
				try {
					Character newCharacter = parseCharacter(splitLine, i + 1);
					if (splitLine[9].equals("passenger")) {
						scenarios.get(scenarios.size() - 1).addPassenger(newCharacter);
					} else if (splitLine[9].equals("pedestrian")) {
						scenarios.get(scenarios.size() - 1).addPedestrian(newCharacter);
					}
				} catch (Exception e) {
					// TODO: handle exception
					if (e instanceof InvalidDataFormatException) {
						((InvalidDataFormatException) e).printMessage(i + 1);
					}
					if (e instanceof NumberFormatException) {
						((NumberFormatException) e).printMessage(i + 1);
					}
				}
			}
		}
	}

	/**
	 * This method parse lines and generate Characters according to content of the
	 * line.
	 * 
	 * @param line      Line in CSV file.
	 * @param linecount Line count of that line.
	 * @return A Character generated by the line.
	 * @throws InvalidDataFormatException On given line exceed or less than 10 rows.
	 */
	public static Character parseCharacter(String[] line, int linecount) throws InvalidDataFormatException {
		if (!(line.length == 10)) {
			throw new InvalidDataFormatException();
		}
		if (line[0].equals("person")) {
			Object[] characteristics = new Object[8];
			for (int i = 1; i < 9; i++) {
				try {
					characteristics[i] = parseValue(line[i], i);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					if (e instanceof NumberFormatException) {
						((NumberFormatException) e).printMessage(linecount);
					}
					if (e instanceof InvalidCharacteristicException) {
						((InvalidCharacteristicException) e).printMessage(linecount);
					}
					if (i == 1) {
						characteristics[i] = Gender.UNKNOWN;
					} else if (i == 2) {
						characteristics[i] = 0;
					} else if (i == 3) {
						characteristics[i] = BodyType.UNSPECIFIED;
					} else if (i == 4) {
						characteristics[i] = Profession.UNKNOWN;
					}
					continue;
				}
			}
			Person output = new Person((int) characteristics[2], (Profession) characteristics[4],
					(Gender) characteristics[1], (BodyType) characteristics[3], (boolean) characteristics[5]);
			if ((boolean) characteristics[6]) {
				output.setAsYou((boolean) characteristics[6]);
			}
			return output;
		} else if (line[0].equals("animal")) {
			String species = line[7];
			boolean isPet = Boolean.parseBoolean(line[8]);
			Animal output = new Animal(species);
			if (isPet) {
				output.setPet(isPet);
			}
			return output;
		}
		return null;
	}

	/**
	 * This method parse characteristic and add the characteristic to Character.
	 * 
	 * @param value     Line in CSV file.
	 * @param columnNum Column number of the characteristic.
	 * @return Specific characteristic.
	 * @throws InvalidDataFormatException On given line exceed or less than 10 rows.
	 * @throws NumberFormatException      On incorrect number format.
	 */
	public static Object parseValue(String value, int columnNum)
			throws NumberFormatException, InvalidCharacteristicException {
		switch (columnNum) {
		case 1:
			if (!inEnum(Gender.class, value)) {
				throw new InvalidCharacteristicException();
			}
			return Gender.valueOf(value.toUpperCase());
		case 2:
			if (!isNumeric(value)) {
				throw new NumberFormatException();
			} else {
				return Integer.parseInt(value);
			}
		case 3:
			if (value.equals("")) {
				return BodyType.UNSPECIFIED;
			} else if (!inEnum(BodyType.class, value)) {
				throw new InvalidCharacteristicException();
			} else {
				return BodyType.valueOf(value.toUpperCase());
			}
		case 4:
			if (value.equals("")) {
				return Profession.UNKNOWN;
			} else if (!inEnum(Profession.class, value)) {
				throw new InvalidCharacteristicException();
			} else {
				return Profession.valueOf(value.toUpperCase());
			}
		case 5:
			return Boolean.parseBoolean(value);
		case 6:
			return Boolean.parseBoolean(value);
		case 7:
			return value;
		case 8:
			return Boolean.parseBoolean(value);
		default:
			return null;
		}
	}

	/**
	 * To judge if a string is numeric or not.
	 * 
	 * @param str String input.
	 * @return Whether it is numeric or not.
	 */
	public static boolean isNumeric(String str) {
		for (int i = 0; i < str.length(); i++) {
			if (!java.lang.Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Determine whether the given string is in the enumeration class.
	 * 
	 * @param string Given string.
	 * @param clazz  Class of enumeration.
	 * @return In of not in.
	 */
	public static <T extends Enum<?>> boolean inEnum(Class<T> clazz, String string) {
		T[] enumContent = clazz.getEnumConstants();
		String[] enumString = new String[enumContent.length];
		for (int i = 0; i < enumString.length; i++) {
			enumString[i] = enumContent[i].name();
			if (enumString[i].equals(string.toUpperCase())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determine whether the given string is in the string list.
	 * 
	 * @param a Given string.
	 * @param s Given string list.
	 * @return In of not in.
	 */
	private static boolean contains(String[] s, String a) {
		for (String string : s) {
			if (a.equals(string)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Find index of a given string in string list.
	 * 
	 * @param a Given string.
	 * @param s Given string list.
	 * @return Index of the string. -1 if not in.
	 */
	private static int index(String[] s, String a) {
		for (int i = 0; i < s.length; i++) {
			if (a.equals(s[i])) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * To get path behind some user input command. Return empty string if there is
	 * no path.
	 * 
	 * @param a Given command.
	 * @param s Given command line consists of several commands.
	 * @return Path follow the command. Empty string if no path.
	 */
	private static String pathBehind(String[] commandList, String command) {
		int len = commandList.length;
		if (!contains(commandList, command)) {
			return "";
		}
		int index = index(commandList, command);
		if (index == (len - 1)) {
			return "";
		} else {
			if (isFlag(commandList[index + 1])) {
				return "";
			} else {
				return commandList[index + 1];
			}
		}
	}

	/**
	 * To judge if a command is a flag or not.
	 * 
	 * @param a Given command.
	 * @return Whether it is a flag or not.
	 */
	private static boolean isFlag(String a) {
		if (a.matches("-(.*)")) {
			return true;
		} else
			return false;
	}
}
