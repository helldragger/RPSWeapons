package helldragger.RPSWeapons;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;


class Config
{




	static ConfigFile NAMES = new ConfigFile("names.yml");
	static ConfigFile CONFIG = new ConfigFile("config.yml");
	static ConfigFile STAGES = new ConfigFile("stages.yml");
	static ConfigFile GROUPS = new ConfigFile("groups.yml");
	static ConfigFile ITEMS = new ConfigFile("items.yml");
	static ConfigFile SAVED_DATA = new ConfigFile("savedData.yml");

	static ConfigFile LANG = new ConfigFile("lang.yml");
	static HashMap<String,String> LINES = new HashMap<String,String>();
	
	
	static boolean USE_PERMS = false;
	static boolean USE_CRAFTING_MODE = true;
	static boolean USE_AUTO_UPDATE = true;
	static boolean DOWNLOAD_UPDATE = false;
	static ChatColor DESCRIPTION_COLOR =ChatColor.GRAY;
	static ChatColor EXP_COLOR = ChatColor.GREEN;
	static double CRAFT_RATIO = 0.1;
	static int MAX_LEVEL = 100;
	static boolean NON_WEAPONS_ENABLED = true;
	static boolean CANCEL_USE_OF_TOO_HIGH_ITEMS = false;
	static int DESCRIPTION_LENGHT = 50;

	static int XP_COST_MULTIPLIER = 0;
	static int XP_LEFT_DIVIDER = 0;
	static int LEVEL_XP_MULTIPLIER = 30;
	static int EXP_ON_LEVEL = 10;
	static int EXP_PER_HIT = 5;
	static int XP_BAR_LENGHT = 50;
	static int DURABILITY_BONUS_ON_RANK_UP = 15;
	static boolean GIVE_ITEM_REWARD_ON_STAGE_UP = true;


	static boolean DISABLE_SPAWNERS = false;


	static boolean DEBUG_MODE = false;
	//	 static boolean USE_ALTERNATE_ITEMS_LIST;

	static int MAX_RAFINEMENT_LEVEL = 12;
	static int MAX_MASTERY_POINTS = 1000;

	static float FIRST_EFFECT_CRAFTING_CHANCE = 8;
	static float SECOND_EFFECT_CRAFTING_CHANCE = 3;

	static double DEFAULT_VALUE = 10;
	static int ENCHANTMENT_VALUE = 250;


	static boolean USE_RPG = false;





	static void loadConfig(RPSWPlugin rpsPlugin, ConfigFile config, boolean customisable) throws IOException,
	InvalidConfigurationException
	{
		ConfigFile config2 = config;
		File folder = rpsPlugin.getDataFolder();

		String dataPath = folder.getPath() + File.separator + "configuration" + File.separator;

		File configFile = new File(dataPath + config.getName());

		if (!configFile.exists())
		{
			InputStream is = null;
			OutputStream os = null;
			try {
				is = rpsPlugin.getResource(config.getName());
				os = new FileOutputStream(configFile);
				byte[] buffer = new byte[1024];
				int length;
				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}
			} finally {
				is.close();
				os.close();
			}


		}

		ConfigFile defaultConfig = new ConfigFile(config.getName());
		defaultConfig.load(rpsPlugin.getResource(config.getName()));

		config.load(configFile);

		if(config== Config.STAGES & customisable)
		{
			try
			{
				StageManager.loadStages();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				RPSWPlugin.log.warning("missing options in stage.yml, restoring it by default");
				loadConfig(rpsPlugin, config2, false);
			}
		}

		if(!customisable)
		{
			config.setDefaults(defaultConfig);
			config.options().copyDefaults(true);
			config.save(configFile);
		}

	}

	static void loadConfigValues(RPSWPlugin rpsPlugin)
	{
		for(String key :LANG.getKeys(true))
			LINES.put(key, LANG.getString(key));
		
		
		
		
		String GENERAL = "general.";
		String COLORS = "colors.";
		String MONEY = "money.";
		String LEVELLING = "levelling.";

		USE_PERMS 				= CONFIG.getBoolean(GENERAL+"use permissions");
		USE_CRAFTING_MODE 		= CONFIG.getBoolean(GENERAL+"use crafting system");
		USE_AUTO_UPDATE			= CONFIG.getBoolean(GENERAL+"use auto-update notifier");
		USE_AUTO_UPDATE			= CONFIG.getBoolean(GENERAL+"download latest version automatically");
		DISABLE_SPAWNERS 		= CONFIG.getBoolean(GENERAL+"disable mob spawners");
		DEBUG_MODE 				= CONFIG.getBoolean(GENERAL+"debug mode enabled");
		//		USE_ALTERNATE_ITEMS_LIST= CONFIG.getBoolean(GENERAL+"use alternate item list");
		CRAFT_RATIO 			= CONFIG.getDouble(GENERAL+"crafting ratio");
		XP_BAR_LENGHT			= CONFIG.getInt(GENERAL+"xp bar lenght");
		CANCEL_USE_OF_TOO_HIGH_ITEMS= CONFIG.getBoolean(GENERAL+"cancel use of leveled items beyond your permissions");
		DESCRIPTION_LENGHT = CONFIG.getInt(GENERAL+"stages RP description line lenght");
		

		DESCRIPTION_COLOR 	= Util.getSafeChatColor(CONFIG.getString(COLORS+"item description color"), ChatColor.GRAY);
		EXP_COLOR 			= Util.getSafeChatColor(CONFIG.getString(COLORS+"item experience bar color"), ChatColor.WHITE);

		DEFAULT_VALUE 		= CONFIG.getInt(MONEY+"wooden utils default value");
		ENCHANTMENT_VALUE 	= CONFIG.getInt(MONEY+"basic enchantment value");
		XP_COST_MULTIPLIER 	= CONFIG.getInt(MONEY+"xp cost multiplier");
		XP_LEFT_DIVIDER 	= CONFIG.getInt(MONEY+"xp Divider");

		NON_WEAPONS_ENABLED = CONFIG.getBoolean(LEVELLING+"normal items enabled");
		LEVEL_XP_MULTIPLIER = CONFIG.getInt(LEVELLING+"xp multiplier (xpmax = level*xpmultiplier)");		
		MAX_LEVEL 			= CONFIG.getInt(LEVELLING+"maximum level reachable");
		EXP_ON_LEVEL 		= CONFIG.getInt(LEVELLING+"experience bonus on levelling up");
		EXP_PER_HIT 		= CONFIG.getInt(LEVELLING+"experience per hit");
		MAX_MASTERY_POINTS 	= CONFIG.getInt(LEVELLING+"max mastery points per item");
		DURABILITY_BONUS_ON_RANK_UP = CONFIG.getInt(LEVELLING+"durability bonus on rank up");
		GIVE_ITEM_REWARD_ON_STAGE_UP = CONFIG.getBoolean(LEVELLING+"give items reward on stage up");


		if (EXP_PER_HIT < 5)
		{
			EXP_PER_HIT = 5;
		}

		if (EXP_ON_LEVEL < EXP_PER_HIT)
		{
			EXP_ON_LEVEL =(int) (EXP_PER_HIT + (EXP_PER_HIT*0.5));
		}
	}

	static int getDeathExperience(RPSWPlugin plugin, EntityType type)
	{
		String name = type.name().replace('_', ' ').toLowerCase();
		int exp = plugin.getConfig().getInt("general.experience per kill." + name);

		if (exp >= 6)
			return exp;
		else
			return 6;
	}

	@Deprecated
	static boolean isItemEnabled(RPSWPlugin plugin,int i)
	{
		String disabledItems = plugin.getConfig().getString("general.disabled items");

		if (Util.getCommaSeperatedValues(disabledItems).contains(String.valueOf(i)))
		{
			return false;
		}

		if (!NON_WEAPONS_ENABLED)
		{
			if (!TypeChecker.isWeapon(Material.getMaterial(i))
					&& !TypeChecker.isArmor(Material.getMaterial(i))
					&& !TypeChecker.isTool(Material.getMaterial(i)))
			{
				return false;
			}
		}

		return true;
	}

	static boolean isItemEnabled(RPSWPlugin plugin, Material material){

		String disabledItems = plugin.getConfig().getString("general.disabled items");

		if (Util.getCommaSeperatedValues(disabledItems).contains(String.valueOf(material.toString())))
		{
			return false;
		}

		if (!NON_WEAPONS_ENABLED)
		{
			if (!TypeChecker.isWeapon(material)
					&& !TypeChecker.isArmor(material)
					&& !TypeChecker.isTool(material))
			{
				return false;
			}
		}
		return true;

	}

	static void removeOldData(RPSWPlugin rpsPlugin)
	{
		try
		{
			deleteOldConfigs(rpsPlugin);
		} 
		catch (IOException | InvalidConfigurationException e)
		{

		}



		CONFIG.set("general.require permissions for leveling items", null);
		CONFIG.set("general.require permissions for using items", null);
	}

	private static void deleteOldConfigs(RPSWPlugin plugin) throws IOException, InvalidConfigurationException
	{
		File folder = plugin.getDataFolder();

		new File(folder.getPath() + File.separator + "config.yml").delete();

		for (File file : new File(folder.getPath() + File.separator + "configuration").listFiles())
		{
			if (file.getName().equalsIgnoreCase("armor.yml"))
				file.delete();
			if (file.getName().equalsIgnoreCase("weapons.yml"))
				file.delete();
			if (file.getName().equalsIgnoreCase("tools.yml"))
				file.delete();
			if (file.getName().equalsIgnoreCase("items.yml"))
			{
				YamlConfiguration config = new YamlConfiguration();
				config.load(file);
				if (config.contains("stages"))
					file.delete();
			}
		}
	}
}