package net.betterverse.towns.chat;

import java.util.regex.Pattern;

import net.betterverse.towns.NotRegisteredException;
import net.betterverse.towns.TownsFormatter;
import net.betterverse.towns.TownsSettings;
import net.betterverse.towns.chat.event.TownsChatEvent;
import net.betterverse.towns.chat.util.StringReplaceManager;
import net.betterverse.towns.object.Nation;
import net.betterverse.towns.object.Resident;
import net.betterverse.towns.object.Town;
import net.betterverse.towns.object.TownsUniverse;

public class TownsChatFormatter {
	private static StringReplaceManager<TownsChatEvent> replacer = new StringReplaceManager<TownsChatEvent>();
	static {
		/*replacer.registerFormatReplacement(Pattern.quote("{worldname}"), new TownsChatReplacerCallable() {
			@Override
			public String call(String match, TownsChatEvent event) throws Exception {
				return String.format(TownsSettings.getChatWorldFormat(), event.getEvent().getPlayer().getWorld().getName());
			}
		});*/
		replacer.registerFormatReplacement(Pattern.quote("{town}"), new TownsChatReplacerCallable() {
			@Override
			public String call(String match, TownsChatEvent event) throws Exception {
				return event.getResident().hasTown() ? event.getResident().getTown().getName() : "";
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{townformatted}"), new TownsChatReplacerCallable() {
			@Override
			public String call(String match, TownsChatEvent event) throws Exception {
				return formatTownTag(event.getResident(), false, true);
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{towntag}"), new TownsChatReplacerCallable() {
			@Override
			public String call(String match, TownsChatEvent event) throws Exception {
				return formatTownTag(event.getResident(), false, false);
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{towntagoverride}"), new TownsChatReplacerCallable() {
			@Override
			public String call(String match, TownsChatEvent event) throws Exception {
				return formatTownTag(event.getResident(), true, false);
			}
		});

		replacer.registerFormatReplacement(Pattern.quote("{nation}"), new TownsChatReplacerCallable() {
			@Override
			public String call(String match, TownsChatEvent event) throws Exception {
				return event.getResident().hasNation() ? event.getResident().getTown().getNation().getName() : "";
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{nationformatted}"), new TownsChatReplacerCallable() {
			@Override
			public String call(String match, TownsChatEvent event) throws Exception {
				return formatNationTag(event.getResident(), false, true);
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{nationtag}"), new TownsChatReplacerCallable() {
			@Override
			public String call(String match, TownsChatEvent event) throws Exception {
				return formatNationTag(event.getResident(), false, false);
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{nationtagoverride}"), new TownsChatReplacerCallable() {
			@Override
			public String call(String match, TownsChatEvent event) throws Exception {
				return formatNationTag(event.getResident(), true, false);
			}
		});

		replacer.registerFormatReplacement(Pattern.quote("{townstag}"), new TownsChatReplacerCallable() {
			@Override
			public String call(String match, TownsChatEvent event) throws Exception {
				return formatTownsTag(event.getResident(), false, false);
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{townsformatted}"), new TownsChatReplacerCallable() {
			@Override
			public String call(String match, TownsChatEvent event) throws Exception {
				return formatTownsTag(event.getResident(), false, true);
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{townstagoverride}"), new TownsChatReplacerCallable() {
			@Override
			public String call(String match, TownsChatEvent event) throws Exception {
				return formatTownsTag(event.getResident(), true, false);
			}
		});

		replacer.registerFormatReplacement(Pattern.quote("{title}"), new TownsChatReplacerCallable() {
			@Override
			public String call(String match, TownsChatEvent event) throws Exception {
				return event.getResident().hasTitle() ? event.getResident().getTitle() : "";
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{surname}"), new TownsChatReplacerCallable() {
			@Override
			public String call(String match, TownsChatEvent event) throws Exception {
				return event.getResident().hasSurname() ? event.getResident().getSurname() : "";
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{townsnameprefix}"), new TownsChatReplacerCallable() {
			@Override
			public String call(String match, TownsChatEvent event) throws Exception {
				return TownsFormatter.getNamePrefix(event.getResident());
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{townsnamepostfix}"), new TownsChatReplacerCallable() {
			@Override
			public String call(String match, TownsChatEvent event) throws Exception {
				return TownsFormatter.getNamePostfix(event.getResident());
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{townsprefix}"), new TownsChatReplacerCallable() {
			@Override
			public String call(String match, TownsChatEvent event) throws Exception {
				return event.getResident().hasTitle() ? event.getResident().getTitle() : TownsFormatter.getNamePrefix(event.getResident());
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{townspostfix}"), new TownsChatReplacerCallable() {
			@Override
			public String call(String match, TownsChatEvent event) throws Exception {
				return event.getResident().hasSurname() ? event.getResident().getSurname() : TownsFormatter.getNamePostfix(event.getResident());
			}
		});

		replacer.registerFormatReplacement(Pattern.quote("{townscolor}"), new TownsChatReplacerCallable() {
			@Override
			public String call(String match, TownsChatEvent event) throws Exception {
				return event.getResident().isMayor() ? (event.getResident().isKing() ? TownsSettings.getKingColour() : TownsSettings.getMayorColour()) : "";
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{group}"), new TownsChatReplacerCallable() {
			@Override
			public String call(String match, TownsChatEvent event) throws Exception {
				return TownsUniverse.getPermissionSource().getPlayerGroup(event.getEvent().getPlayer());
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{permprefix}"), new TownsChatReplacerCallable() {
			@Override
			public String call(String match, TownsChatEvent event) throws Exception {
				return TownsUniverse.getPermissionSource().getPrefixSuffix(event.getResident(), "prefix");
			}
		});
		replacer.registerFormatReplacement(Pattern.quote("{permsuffix}"), new TownsChatReplacerCallable() {
			@Override
			public String call(String match, TownsChatEvent event) throws Exception {
				return TownsUniverse.getPermissionSource().getPrefixSuffix(event.getResident(), "suffix");
			}
		});

		replacer.registerFormatReplacement(Pattern.quote("{playername}"), new TownsChatReplacerCallable() {
			@Override
			public String call(String match, TownsChatEvent event) throws Exception {
				return event.getEvent().getPlayer().getName();
			}
		});
		/*
		replacer.registerFormatReplacement(Pattern.quote("{modplayername}"), new TownsChatReplacerCallable() {
			@Override
			public String call(String match, TownsChatEvent event) throws Exception {
				return event.getEvent().getPlayer().getDisplayName();
			}
		});
		*/
		replacer.registerFormatReplacement(Pattern.quote("{channelTag}"), new TownsChatReplacerCallable() {
			@Override
			public String call(String match, TownsChatEvent event) throws Exception {
				return formatTownsTag(event.getResident(), false, false);
			}
		});

		// Replace colours last ({msg} is replaced last as it can't be regex parsed).
		replacer.registerFormatReplacement("&{1}[0-9A-Fa-f]{1}", new TownsChatReplacerCallable() {
			@Override
			public String call(String match, TownsChatEvent event) throws Exception {
				return "\u00A7" + match.charAt(1);
			}
		});
	}

	public static String getChatFormat(TownsChatEvent event) {
		// Replace the {msg} here so it's not regex parsed.
		return replacer.replaceAll(event.getFormat(), event).replace("{modplayername}", "%1$s").replace("{msg}", "%2$s");
	}

	/**
	 * @param resident
	 * @param override use full names if no tag is present
	 * @param full	 Only use full names (no tags).
	 * @return
	 */
	public static String formatTownsTag(Resident resident, Boolean override, Boolean full) {
		try {
			if (resident.hasTown()) {
				Town town = resident.getTown();
				String townTag = town.getTag();
				Nation nation = null;
				String nationTag = null;
				if (resident.hasNation()) {
					nation = town.getNation();
					nationTag = nation.getTag();
				}

				String nTag = "", tTag = "";

				//Force use of full names only
				if (full) {
					nationTag = "";
					townTag = "";
				}
				// Load town tags/names
				if (townTag != null && ! townTag.isEmpty()) {
					tTag = townTag;
				} else if (override || full) {
					tTag = town.getName();
				}

				// Load the nation tags/names
				if ((nationTag != null) && ! nationTag.isEmpty()) {
					nTag = nationTag;
				} else if (resident.hasNation() && (override || full)) {
					nTag = nation.getName();
				}

				// Output depending on what tags are present
				if ((! tTag.isEmpty()) && (! nTag.isEmpty())) {
					return String.format(TownsSettings.getChatTownNationTagFormat(), nTag, tTag);
				}

				if (! nTag.isEmpty()) {
					return String.format(TownsSettings.getChatNationTagFormat(), nTag);
				}

				if (! tTag.isEmpty()) {
					return String.format(TownsSettings.getChatTownTagFormat(), tTag);
				}
			}
		} catch (NotRegisteredException e) {
			// no town or nation
		}
		return "";
	}

	public static String formatTownTag(Resident resident, Boolean override, Boolean full) {
		try {
			if (resident.hasTown()) {
				if (full) {
					return String.format(TownsSettings.getChatTownTagFormat(), resident.getTown().getName());
				} else if (resident.getTown().hasTag()) {
					return String.format(TownsSettings.getChatTownTagFormat(), resident.getTown().getTag());
				} else if (override) {
					return String.format(TownsSettings.getChatTownTagFormat(), resident.getTown().getName());
				}
			}
		} catch (NotRegisteredException e) {
		}
		return "";
	}

	public static String formatNationTag(Resident resident, Boolean override, Boolean full) {
		try {
			if (resident.hasNation()) {
				if (full) {
					return String.format(TownsSettings.getChatNationTagFormat(), resident.getTown().getNation().getName());
				} else if (resident.getTown().getNation().hasTag()) {
					return String.format(TownsSettings.getChatNationTagFormat(), resident.getTown().getNation().getTag());
				} else if (override) {
					return String.format(TownsSettings.getChatNationTagFormat(), resident.getTown().getNation().getName());
				}
			}
		} catch (NotRegisteredException e) {
		}
		return "";
	}
}
