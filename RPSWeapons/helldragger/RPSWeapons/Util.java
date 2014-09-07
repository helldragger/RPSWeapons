package helldragger.RPSWeapons;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

class Util
{
	
	static ItemStack stringToItemStack(Material mat,String node)
	{

		int amount = 1;
		String name = "";
		List<String> description = new ArrayList<String>();
		ItemStack item = new ItemStack(mat, amount);


		//alors c'est une liste d'infos
		String[] args = node.replace("[", "").replace("]", "").replace('&', ChatColor.COLOR_CHAR).replace("'", "\'").split(",");
		if(args.length >= 1)
		{
			try
			{amount = Integer.parseInt(args[0]);}
			catch(Exception e)
			{RPSWPlugin.log.warning("item reward "+mat.name()+" haven't a correct amount");}

			if(args.length >= 2)
			{
				name = (args[1].isEmpty()) ? "" : args[1];
				if (args.length >= 3)
					description = Util.descriptionToLines(args[2]);
			}

		}



		item = new ItemStack(mat);

		if(amount > 1)
			item.setAmount(amount);

		if(!name.contentEquals("") & !name.contentEquals(" "))
		{
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(name);
			item.setItemMeta( meta );
		}

		if(!description.isEmpty())
		{
			ItemMeta meta = item.getItemMeta();
			meta.setLore(description);
			item.setItemMeta( meta );
		}



		return item;
	}
	
	static List<String> descriptionToLines(String desc){
		List<String> result = new ArrayList<String>();
		if (desc != null)
			for(int i = 0; Config.DESCRIPTION_LENGHT * i < desc.length(); i++)
			{
				
				
				if((i+1)*Config.DESCRIPTION_LENGHT >= desc.length())
					result.add(desc.substring(i*Config.DESCRIPTION_LENGHT, desc.length()-1));
				else
					{
					if(desc.substring(i*Config.DESCRIPTION_LENGHT, (i+1)*Config.DESCRIPTION_LENGHT).endsWith(""+ChatColor.COLOR_CHAR))
						desc.replaceAll(""+ChatColor.COLOR_CHAR,  (" "+ChatColor.COLOR_CHAR));

					result.add(desc.substring(i*Config.DESCRIPTION_LENGHT, (i+1)*Config.DESCRIPTION_LENGHT));
					}
			}	
		return result;
	}
	
	static String capitalizeFirst(String string, char divider)
	{
		String div = String.valueOf(divider);

		String[] words = string.split(div);

		string = "";

		for (int i = 0; i < words.length - 1; i++)
		{
			string += words[i].substring(0, 1).toUpperCase() + words[i].substring(1).toLowerCase() + " ";
		}

		string += words[words.length - 1].substring(0, 1).toUpperCase()
				+ words[words.length - 1].substring(1).toLowerCase();

		return string;
	}

	static List<String> getCommaSeperatedValues(String string)
	{
		List<String> list = new ArrayList<String>();
		if (string == null)
			return list;
		if (string.startsWith("none") || string.isEmpty())
			return list;

		string = string.toUpperCase();
		string.replaceAll(" ", "_");

		String[] values = string.split(",");

		for (int i = 0; i < values.length; i++)
		{
			String value = values[i];

			if (value.startsWith("_"))
			{
				value = value.substring(1);
			}

			list.add(value);
		}

		return list;
	}

	static void dropExperience(Location loc, int expToDrop, int expPerOrb)
	{
		World world = loc.getWorld();

		int maxOrbs = expToDrop / expPerOrb;

		for (int orb = 0; orb < maxOrbs; orb++)
		{
			((ExperienceOrb) world.spawn(loc, ExperienceOrb.class)).setExperience(expPerOrb);
		}
	}

	static String searchListForString(List<String> list, String string, String def)
	{
		if (list == null) return def;

		for (String s : list)
		{
			if (ChatColor.stripColor(s).startsWith(ChatColor.stripColor(string)))
			{
				return s;
			}
		}

		return def;
	}

	static String searchListForString(List<String> list, String string, String def, String prefix)
	{
		if (list == null) return def;

		for (String line : list)
		{
			String strippedLine = ChatColor.stripColor(line);

			if (strippedLine.startsWith(prefix))
			{
				return line.substring(prefix.length());
			}

			if (strippedLine.startsWith(ChatColor.stripColor(string)))
			{
				return line;
			}
		}

		return def;
	}

	static int searchListForStringID(List<String> list, String string, int def)
	{
		if (list == null) return def;

		ListIterator<String> i = list.listIterator();

		while (i.hasNext())
		{
			String s = i.next();
			if (ChatColor.stripColor(s).startsWith(ChatColor.stripColor(string)))
			{
				return i.nextIndex() - 1;
			}
		}

		return def;
	}

	static int searchListForStringID(List<String> list, String string, int def, String prefix)
	{
		if (list == null) return def;

		ListIterator<String> i = list.listIterator();

		while (i.hasNext())
		{
			String s = i.next();
			if (ChatColor.stripColor(s).startsWith(prefix + ChatColor.stripColor(string)))
			{
				return i.nextIndex() - 1;
			}
		}

		return def;
	}

	static ChatColor getSafeChatColor(String color, ChatColor def)
	{
		if (color != null)
		{
			if(color != "")
			for (ChatColor c : ChatColor.values())
			{
				if (color.equalsIgnoreCase(c.name()))
				{
					return c;
				}
			}

			
		}
		return def;
	}

	static int getLevelOnCurve(int min, int max, double ratio)
	{
		Random rand = new Random();
		int level = 1;

		int roll = rand.nextInt(100) + 1;

		for (int i = min; i <= max; i++)
		{			
			if (roll <= (ratio / i) * 100)
			{
				//System.out.println("i: " + i + " needed: " + (ratio / i * 100) + " roll: " + roll);
				level = i;
			}
			else
			{
				return level;
			}
		}

		return level;
	}

	static int getMaxLevel(Player player, ItemType type)
	{
		if (!Config.USE_PERMS)
		{
			return Config.MAX_LEVEL;
		}

		for (int level = Config.MAX_LEVEL; level > 0; level--)
		{
			Stage stage = StageManager.getStage(type, level);

			if (stage == null) continue;

			if (Permissions.hasPermission(player, stage.getName()))
			{
				return level;
			}
		}

		return 0;
	}

	static void printlnObj(PrintStream printer, Object...objects)
	{
		String line = "";

		for (Object obj : objects)
		{
			line += obj.toString() + ":";
		}

		printer.println(line);
	}

	public static void dropItem(Location loc, ItemStack itemstack) {
		loc.getWorld().dropItem(loc, itemstack);
		
		
	}
}