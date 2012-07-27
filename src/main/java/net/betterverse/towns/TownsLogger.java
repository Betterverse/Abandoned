package net.betterverse.towns;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Logger;

import net.betterverse.towns.object.Nation;
import net.betterverse.towns.object.Resident;
import net.betterverse.towns.object.Town;
import net.betterverse.towns.object.TownsEconomyObject;
import net.betterverse.towns.util.FileMgmt;

public class TownsLogger {
	public static final Logger log = Logger.getLogger("net.betterverse.towns.log");
	public static final Logger money = Logger.getLogger("net.betterverse.towns.moneylog");
	public static final Logger debug = Logger.getLogger("net.betterverse.towns.debug");

	public static void setup(String root, boolean append) {
		String logFolder = root + FileMgmt.fileSeparator() + "logs";
		//FileMgmt.checkFolders(new String[]{logFolder});

		setupLogger(log, logFolder, "towns.log", new TownsLogFormatter(), TownsSettings.isAppendingToLog());

		setupLogger(money, logFolder, "money.csv", new TownsMoneyLogFormatter(), TownsSettings.isAppendingToLog());
		money.setUseParentHandlers(false);

		//if (TownsSettings.getDebug()) {
		setupLogger(debug, logFolder, "debug.log", new TownsLogFormatter(), TownsSettings.isAppendingToLog());
		//debug.setUseParentHandlers(false);	//if enabled this prevents the messages from showing in the console.
		//}
	}

	public static void shutDown() {
		CloseDownLogger(log);
		CloseDownLogger(money);
		CloseDownLogger(debug);
	}

	public static void setupLogger(Logger logger, String logFolder, String filename, Formatter formatter, boolean append) {
		try {
			FileHandler fh = new FileHandler(logFolder + FileMgmt.fileSeparator() + filename, append);
			fh.setFormatter(formatter);
			logger.addHandler(fh);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void CloseDownLogger(Logger logger) {
		for (Handler fh : logger.getHandlers()) {
			logger.removeHandler(fh);
			fh.close();
		}
	}

	public static void logMoneyTransaction(TownsEconomyObject a, double amount, TownsEconomyObject b, String reason) {
		money.info(String.format("%s,%s,%s,%s", reason == null ? "" : reason, getObjectName(a), amount, getObjectName(b)));
		//money.info(String.format("   %-48s --[ %16.2f ]--> %-48s", getObjectName(a), amount, getObjectName(b)));
	}

	private static String getObjectName(TownsEconomyObject obj) {
		String type;
		if (obj == null) {
			type = "Server";
		} else if (obj instanceof Resident) {
			type = "Resident";
		} else if (obj instanceof Town) {
			type = "Town";
		} else if (obj instanceof Nation) {
			type = "Nation";
		} else {
			type = "Server";
		}

		return String.format("[%s] %s", type, obj != null ? obj.getName() : "");
	}
}
