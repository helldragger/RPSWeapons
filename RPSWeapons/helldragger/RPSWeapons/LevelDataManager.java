package helldragger.RPSWeapons;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

 class LevelDataManager
{
	 static boolean hasLevelData(ItemStack itemStack)
	{
		if (!itemStack.hasItemMeta())
		{
			return false;
		}

		if (!itemStack.getItemMeta().hasLore())
		{
			return false;
		}

		if (Util.searchListForString(itemStack.getItemMeta().getLore(), "Level", "false", "▲") == "false")
		{
			System.out.println("Failed search.");
			return false;
		}

		return true;
	}

	 static Player hasOwner(ItemMeta meta){


		if (meta.getLore().isEmpty())
			return null;

		List<String> lore = meta.getLore();

		for(String string : lore)
			if (string.startsWith(Config.DESCRIPTION_COLOR+"This item "))
			{
				if (string.contains("property"))
					return Bukkit.getPlayer( string.substring(string.indexOf("is "), string.indexOf(" property")) );


			}

		return null;


	}

	 static boolean hasExperienceBar(ItemStack item)
	{
		if (getType(item) == ItemType.ITEM)
		{
			switch (item.getType())
			{
			case WORKBENCH:
			case FURNACE:
			case BOW:
			case FISHING_ROD:
				return true;
			default:
				return false;
			}
		}
		else
		{
			return true;
		}
	}

	 static LevelData getLevelData(ItemStack itemStack){


		if(TypeChecker.isLevellable( itemStack.getType() ) ){



			
			LevelData data = null;
			if (hasLevelData(itemStack))
				try
			{
					data = ((LevelData) itemStack);
			}
			catch(Exception e)
			{
				if(Config.DEBUG_MODE) RPSWPlugin.log.info("DEBUG : erreur exception lors de recuperation level data "+ e.getMessage());
				data = LevelData.recoverLevelData(itemStack);
			}
			else
				data = new LevelData(itemStack);

			return data;




		}

		return null;

	}

	 static LevelData getLevelData(ItemStack itemStack,Player player){


		if(TypeChecker.isLevellable( itemStack.getType() ) ){



			boolean hasLevelData = hasLevelData(itemStack);
			LevelData data = null;
			if (hasLevelData)
				data = ((LevelData) itemStack);
			else
				data = new LevelData(itemStack,player);

			return data;
		}

		return null;

	}




	 static int getLevel(ItemMeta meta)
	{
		String rawData = Util.searchListForString(meta.getLore(), "Level", "ERROR", "▲");

		if (rawData == "ERROR")
		{
			Bukkit.getLogger().warning("Error reading level data!");
			return 0;
		}

		String[] split = rawData.split(" ");
		return Integer.valueOf(split[1]);
	}

	 static int getLevel(LevelData data)
	{

		return data.getLevel();
	}



	 static int getExperience(ItemMeta meta, boolean hasExpBar)
	{
		if (!hasExpBar)
		{
			return 0;
		}

		
		
		String rawData = Util.searchListForString(meta.getLore(), "    ", "ERROR", "▲");

		if (rawData == "ERROR")
		{
			Bukkit.getLogger().warning("Error reading experience data for " + meta.getDisplayName() + " (line 85)");
			return 0;
		}

		String[] split = rawData.split("xp");

		if (split.length < 2)
		{
			Bukkit.getLogger().warning("Error reading experience data for " + meta.getDisplayName() + " (line 93)");
		}
		
		return (getLevel(meta) * Config.LEVEL_XP_MULTIPLIER) - Integer.valueOf(split[0].substring(6));
	}


	 static int getExperience(LevelData data)
	{	
		return data.getExperience();
	}


	 static Float getMastery(ItemMeta meta){

		List<String> lores = meta.getLore();

		if(lores != null)
		{
			String rawData = Util.searchListForString(meta.getLore(), "Holder's mastery: ", "ERROR", "▲");

			if (rawData == "ERROR")
			{
				Bukkit.getLogger().warning("Error 1 reading Mastery data for " + meta.getDisplayName());
				return (float)0;
			}

			String[] split = rawData.split(": ");


			if (split.length < 2)
			{
				Bukkit.getLogger().warning("Error 2 reading Mastery data for " + meta.getDisplayName());
			}

			try
			{
				String[] secondSplit = split[1].replaceFirst("6", "%").split("%");

				return  Float.valueOf(secondSplit[secondSplit.length-1]);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				Bukkit.getLogger().warning("Error 3 reading Mastery data for " + meta.getDisplayName());
			}

		}


		return (float)1;

	}

	 static Float getMastery(LevelData data){

		return data.getHolderMastery();

	}

	static Stage getStage(ItemType type, int level)
	{
		Stage stage = null;

		while (stage == null && level > 0)
		{
			stage = StageManager.getStage(type, level);
			level--;
		}

		return stage;
	}

	 static Stage getNullStage(ItemType type)
	{
		Stage stage = null;
		int level = 0;
		while (stage == null && level < Config.MAX_LEVEL)
		{
			stage = StageManager.getStage(type, level);
			level++;
		}

		return stage;
	}

	 static ItemType getType(ItemStack itemStack)
	{
		return TypeChecker.getItemType(itemStack.getType());
	}

	 static String createExpBar(int max, int amount)
	{
		ChatColor on = Config.EXP_COLOR;
		ChatColor off = Config.DESCRIPTION_COLOR;
		int nb_bar = 0;
		//TODO Ajout de l'option longueur de barre d'xp
		if(Config.XP_BAR_LENGHT >= 25)
			nb_bar = Config.XP_BAR_LENGHT;

		String bar = off + "[";


		for (int i = 1; i <= (nb_bar); i++)
		{
			if ((amount * nb_bar / max) >= i)
				bar += on + "|";
			else
				bar += off + "|";
		}
		bar += off + "]";

		return bar;
	}

	 static int readExperience(String expBar, int level, LevelData data)
	{
		try
		{
			
			return data.getExperience();
		}
		catch (Exception e)
		{
			ChatColor on = Config.EXP_COLOR;
			ChatColor off = Config.DESCRIPTION_COLOR;

			int nb_bar = 0;
			//TODO Ajout de l'option longueur de barre d'xp
			if(Config.XP_BAR_LENGHT >= 25)
				nb_bar = Config.XP_BAR_LENGHT;
			
			int exp = 0;

			expBar = expBar.substring(3, expBar.length() - 1); // Remove brackets

			String[] lines = expBar.split("\\|");

			for (int i = 0; i < lines.length; i++)
			{
				String line = lines[i];

				if (line.equals(on.toString()))
				{
					exp += 1;
				}
				else if (line.equals(off.toString()))
				{
					break;
				}
			}

			return exp * ( (level * Config.LEVEL_XP_MULTIPLIER) / nb_bar);

		}
	}




	 static Player getOwner(ItemMeta meta) {
		List<String> lores = meta.getLore();

		if(lores != null)
		{
			String rawData = Util.searchListForString(meta.getLore(), "This item is ", "ERROR", "▲");

			if (rawData == "ERROR")
			{
				rawData = Util.searchListForString(meta.getLore(), "This item have no owner", "ERROR", "▲");
				if (rawData == "ERROR")
				{
					Bukkit.getLogger().warning("Error 1 reading Owner data for " + meta.getDisplayName());

				}
				return null;
			}

			rawData = rawData.replace("This item is ", "");
			rawData = rawData.replace(" property.", "");
			
			try
			{
				return  Bukkit.getPlayer(rawData);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				Bukkit.getLogger().warning("Error 2 reading Owner data for " + meta.getDisplayName());
			}

		}		return null;
	}

	 static Player getOwner(LevelData data) {
		return data.getOwner();
	}

	static Player getHolder(ItemMeta meta) {
		List<String> lores = meta.getLore();

		if(lores != null)
		{
			String rawData = Util.searchListForString(meta.getLore(), "Current holder:", "ERROR", "▲");

			if (rawData == "ERROR")
			{
				rawData = Util.searchListForString(meta.getLore(), "Current holder:", "ERROR", "▲");
				if (rawData == "ERROR")
				{
					Bukkit.getLogger().warning("Error 1 reading Owner data for " + meta.getDisplayName());

				}
				return null;
			}

			rawData = rawData.replace("Current holder: ", "");
			
			try
			{
				return  Bukkit.getPlayer(rawData);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				Bukkit.getLogger().warning("Error 2 reading Owner data for " + meta.getDisplayName());
			}

		}		return null;
	}

	public static Stage getStage(int level) {
		return StageManager.getStage(level);
	}
}