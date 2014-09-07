package helldragger.RPSWeapons;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;

class  TypeChecker
{
	static RPSWPlugin plugin;
	static YamlConfiguration pluginNames;	

	static void loadTypeChecker(RPSWPlugin plugin) 
	{
		TypeChecker.plugin = plugin;
		loadItems(plugin);
	}


	static void loadItems(RPSWPlugin plugin)
	{
		pluginNames = YamlConfiguration.loadConfiguration(plugin.getResource("names.yml"));
	}

	/**
	 * Check if an item is a weapon.
	 * 
	 * @param plugin
	 *            - Instance of main class
	 * @param material
	 *            - The material of the item to check
	 * @return True if the item is a weapon.
	 */
	static boolean isWeapon(Material material)
	{
		if(ToolType.getByItemName(material.name()) != null)
			if(ToolType.getByItemName(material.name()) == ToolType.SWORD
					|| ToolType.getByItemName(material.name()) == ToolType.AXE
					|| ToolType.getByItemName(material.name()) == ToolType.BOW)
				return true;
		return pluginNames.getConfigurationSection("weapons").getValues(false).containsKey(material.name());
	}

	/**
	 * Check if an item is armor.
	 * 
	 * @param plugin
	 *            - Instance of main class
	 * @param material
	 *            - The material of the item to check
	 * @return True if the item is armor.
	 */
	static boolean isArmor(Material material)
	{
		return pluginNames.getConfigurationSection("armor").getValues(false).containsKey(material.name());
	}

	/**
	 * Check if an item is a tool.
	 * 
	 * @param plugin
	 *            - Instance of main class
	 * @param material
	 *            - The material of the item to check
	 * @return True if the item is a tool.
	 */
	static boolean isTool(Material material)
	{
		return pluginNames.getConfigurationSection("tools").getValues(false).containsKey(material.name());
	}

	static boolean isLevellable(Material material){
		if(	pluginNames.getConfigurationSection("tools").getValues(false).containsKey(material.name())
				||	pluginNames.getConfigurationSection("armor").getValues(false).containsKey(material.name())
				||  pluginNames.getConfigurationSection("weapons").getValues(false).containsKey(material.name())
				){
			return true;
		}
		else{
			return false;
		}

	}


	/**
	 * Checks if a tool is the correct tool to use for the specified block.
	 * 
	 * @param plugin
	 *            - Instance of main class
	 * @param block
	 *            - The block to check
	 * @param tool
	 *            - The tool to check
	 * @return True if the tool is the proper tool for the block.
	 */
	static boolean isCorrectTool(ToolType tool, Block block)
	{

		String values = pluginNames.getConfigurationSection("correct tools.").getString(tool.name().toUpperCase());

		List<String> returnValues = Util.getCommaSeperatedValues(values);
		for(String value : returnValues){
			if (value.equalsIgnoreCase(block.getType().name().replace(" ", "_")))
				return true;
		}

		return false;
	}

	/**
	 * Get the name of the item that is shown in-game.
	 * 
	 * @param plugin
	 *            - Instance of main class
	 * @param material
	 *            - The material to get the name of
	 * @return The in-game name of the item.
	 */
	static String getInGameName(Material material)
	{
		if (isWeapon(material))
		{
			return pluginNames.getConfigurationSection("weapons").getString(material.name());
		}
		else if (isArmor(material))
		{
			return pluginNames.getConfigurationSection("armor").getString(material.name());
		}
		else if (isTool(material))
		{
			return pluginNames.getConfigurationSection("tools").getString(material.name());
		}
		else
		{
			return Util.capitalizeFirst(material.name(), '_');
		}
	}

	static ItemType getItemType(Material material)
	{
		if (isWeapon(material))
		{
			return ItemType.WEAPON;
		}
		else if (isArmor(material))
		{
			return ItemType.ARMOR;
		}
		else if (isTool(material))
		{
			return ItemType.TOOL;
		}
		else
		{
			return ItemType.ITEM;
		}
	}
}