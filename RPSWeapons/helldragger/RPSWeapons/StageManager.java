package helldragger.RPSWeapons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

class StageManager
{
	static HashMap<Integer, Stage> stages = new HashMap<Integer, Stage>();

	static void loadStages()
	{
		Map<String, Object> yamlStages = Config.STAGES.getValues(false);

		for (Entry<String, Object> entry : yamlStages.entrySet())
		{
			if ( entry.getKey() =="null") continue;
			String stageName = entry.getKey();
			int level = Config.STAGES.getInt(stageName + ".level");
			Stage stage = createStage(stageName);

			stages.put(level, stage);
		}
	}	

	static Collection<Stage> getStages(){
		return stages.values();
	}

	static Stage getNextStage(Stage stage){
		int level = stage.getLevel();
		int maxLevel = Config.MAX_LEVEL;

		while (level < maxLevel){
			level++;
			if (getStage(level) != null & getStage(level) != stage) return getStage(level);
		}
		return stage;
	}

	static List<String> getStagesNames()
	{
		List<String> nameList = new ArrayList<String>();
		Collection<Stage> stageList = stages.values();
		if (!stageList.isEmpty())
			for (Stage value : stageList)
				nameList.add(value.getName());

		return nameList;
	}

	static boolean nextStageExist(Stage stage){
		int level = stage.getLevel();
		int maxLevel = Config.MAX_LEVEL;


		while (level < maxLevel){
			level++;
			if (getStage(level) != null) return true;
		}
		return false;
	}

	static boolean nextStageExist(int level){
		int maxLevel = Config.MAX_LEVEL;


		while (level < maxLevel){
			level++;
			if (getStage(level) != null) return true;
		}
		return false;
	}

	static Stage getStage(ItemType type, int level)
	{
		Stage stage = null;

		while (stage == null && level > 0)
		{
			stage = stages.get(level);
			level--;
		}

		return stage;
	}

	static Stage getStage(int level)
	{
		Stage stage = null;

		while (stage == null && level > 0)
		{
			stage = stages.get(level);
			level--;
		}

		return stage;
	}

	static Stage getNullStage(){
		return createStage("null");
	}

	private static Stage createStage(String name)
	{
		ConfigurationSection config = Config.STAGES.getConfigurationSection(name);
		int level = config.getInt("level");
		ChatColor color = Util.getSafeChatColor(config.getString("color"), ChatColor.WHITE);
		List<LevelEnchantment> enchantments = getEnchantments(config.getString("enchantments"));
		Map<String, Integer> bonuses = getBonuses(config.getConfigurationSection("bonuses"));
		List<ItemStack> itemsReward = getRewards(config.getConfigurationSection("items reward"));




		String prefix = config.getString("prefix","none");
		String suffix = config.getString("suffix","none");
		ChatColor prefixcolor = Util.getSafeChatColor(config.getString("prefix color"), color);
		ChatColor suffixcolor = Util.getSafeChatColor(config.getString("suffix color"), color);



		if (prefix != null)
			if (prefix.equalsIgnoreCase("") || prefix.equalsIgnoreCase("none"))
				prefix = null;
			else 
				prefix = color + "[" + prefixcolor + prefix + color + "] ";
		else
			prefix = null;

		if (suffix != null)
			if (suffix.equalsIgnoreCase("") || suffix.equalsIgnoreCase("none"))
				suffix = null;
			else 
				suffix = color + " [" + suffixcolor + suffix + color + "]";
		else
			suffix = null;

		String desc = config.getString("description",""); //si c'est null, c'est = "" sinon c'est la donnée.
		ChatColor descColor = Util.getSafeChatColor(config.getString("description color"), color);

		return new Stage(name, level, color, enchantments, bonuses, itemsReward, prefix, suffix, desc, descColor);
	}

	//	// create the only one null stage
	//	private static Stage createStage(){
	//		ConfigurationSection config = Config.STAGES.getConfigurationSection("null");
	//		
	//		int level = 1;
	//		ChatColor color = Util.getSafeChatColor(config.getString("color"), ChatColor.WHITE);
	//		List<LevelEnchantment> enchantments = new ArrayList<LevelEnchantment>();
	//		Map<String, Integer> bonuses = getBonuses(config.getConfigurationSection("bonuses"));
	//		
	//		
	//		
	//		return new Stage("null",level,color,enchantments, bonuses);
	//	}

	private static List<LevelEnchantment> getEnchantments(String data)
	{
		List<LevelEnchantment> list = new ArrayList<LevelEnchantment>();

		for (String enchantment : Util.getCommaSeperatedValues(data))
		{
			try
			{
				String[] split = enchantment.split("\\.");

				int id = Integer.valueOf(split[0]);
				int level = Integer.valueOf(split[1]);

				list.add(new LevelEnchantment(id, level));				
			}
			catch (NumberFormatException e)
			{
				Bukkit.getLogger().warning("RolePlaySpeciality Weapons: Invalid enchantment format '" + enchantment + "'.");
			}
		}

		return list;
	}

	private static Map<String, Integer> getBonuses(ConfigurationSection config)
	{
		Map<String, Integer> map = new HashMap<String, Integer>();

		for (Entry<String, Object> entry : config.getValues(false).entrySet())
		{
			String name = entry.getKey();
			int value = config.getInt(name);

			map.put(name, value);
		}

		return map;
	}

	@SuppressWarnings("deprecation")
	static List<ItemStack> getRewards(ConfigurationSection config) {
		List<ItemStack> map = new ArrayList<ItemStack>();
		if(config != null)
			try{
				if(!config.getValues(false).isEmpty())

					for (Entry<String, Object> entry : config.getValues(false).entrySet())
					{
						String name = entry.getKey();
						ItemStack item = null;

						try
						{
							int idname = Integer.parseInt(name);

							//L'id est sous forme numerique
							if(Material.getMaterial(idname) != null)	
								item = Util.stringToItemStack(  Material.getMaterial(idname), entry.getValue().toString());


						}
						catch(NumberFormatException e)
						{
							//l'id est sous forme material.name
							if(Material.getMaterial(name) != null)	
								item = Util.stringToItemStack( Material.getMaterial(name), entry.getValue().toString());

						}


						if(item != null)
							map.add(item);
						else
							RPSWPlugin.log.warning("UNKNOWN REWARD ITEM IN "+config.getName()+" ("+name+")");

					}
			}
		catch(NullPointerException e)
		{
			RPSWPlugin.log.warning("ERROR WHILE READING ITEM REWARDS IN STAGE "+config.getName());
			e.printStackTrace();
		}
		return map;
	}

	


}
