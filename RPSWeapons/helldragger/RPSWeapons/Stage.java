package helldragger.RPSWeapons;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

class Stage
{
	/**
	 * @uml.property  name="name"
	 */
	private final String name;
	/**
	 * @uml.property  name="level"
	 */
	private final int level;
	/**
	 * @uml.property  name="color"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private final ChatColor color;
	/**
	 * @uml.property  name="enchantments"
	 */
	private final List<LevelEnchantment> enchantments;
	/**
	 * @uml.property  name="bonuses"
	 * @uml.associationEnd  qualifier="name:java.lang.String java.lang.Integer"
	 */
	private final Map<String, Integer> bonuses;

	private final String prefix;

	private final String suffix;
	
	private final List<ItemStack> itemsReward;
	private final List<String> description;
	private final ChatColor descColor;

	Stage(String name, int level, ChatColor color, List<LevelEnchantment> enchantments, Map<String, Integer> bonuses, List<ItemStack> itemsReward, String prefix, String suffix, String descriptionRP, ChatColor descColor)
	{
		this.name = name;
		this.level = level;
		this.color = color;
		this.enchantments = enchantments;
		this.bonuses = bonuses;
		if (prefix != null)
			if (prefix.equalsIgnoreCase("") || prefix.equalsIgnoreCase("none"))
				this.prefix = null;
			else 
				this.prefix = prefix;
		else
			this.prefix = null;
		
		if (suffix != null)
			if (suffix.equalsIgnoreCase("") || suffix.equalsIgnoreCase("none"))
				this.suffix = null;
			else 
				this.suffix = suffix;
		else
			this.suffix = null;
		this.itemsReward = itemsReward;
		this.descColor = descColor; 
		this.description = descriptionToLines(descriptionRP);
	}

	private List<String> descriptionToLines(String desc){
		List<String> result = new ArrayList<String>();
		if (desc != null)
			for(int i = 0; Config.DESCRIPTION_LENGHT * i < desc.length(); i++)
			{
				if((i+1)*Config.DESCRIPTION_LENGHT >= desc.length())
					result.add(descColor + desc.substring(i*Config.DESCRIPTION_LENGHT, desc.length()-1));
				else
					result.add(descColor + desc.substring(i*Config.DESCRIPTION_LENGHT, (i+1)*Config.DESCRIPTION_LENGHT));
			}	
		return result;
	}
	
	/**
	 * @return
	 * @uml.property  name="name"
	 */
	String getName()
	{
		return name;
	}

	String[] getNameModifications()
	{
		String[] namemodif = new String[2];
		namemodif[0]=prefix;
		namemodif[1]=suffix;

		return namemodif;


	}

	/**
	 * @return
	 * @uml.property  name="level"
	 */
	int getLevel()
	{
		return level;
	}

	/**
	 * @return
	 * @uml.property  name="color"
	 */
	ChatColor getColor()
	{
		return color;
	}

	List<LevelEnchantment> getEnchantments()
	{
		return enchantments;
	}

	int getBonus(String name)
	{
		return bonuses.get(name);
	}

	boolean setBonus(String key,Integer value){
		if (bonuses.containsKey(key)) {
			bonuses.put(key, value);
			return true;
		}else{
			return false;
		}

	}

	List<String> getDescription(){
		return this.description;
	}
	
	ChatColor getDescriptionColor(){
		return this.descColor;
	}

	boolean nextStageExist(){
		return StageManager.nextStageExist(this.level);
	}

	String getNextStageName(ItemType type){

		return StageManager.getNextStage(this).name;
	}

	String getNextStageName(){

		return StageManager.getNextStage(this).getName();
	}





	int getNextStageLevel(ItemType type){
		return StageManager.getNextStage(this).level;
	}

	int getNextStageLevel(){
		return StageManager.getNextStage(this).level;
	}






	ChatColor getNextStageColor(ItemType type){

		return StageManager.getNextStage(this).color;
	}

	ChatColor getNextStageColor(){

		return StageManager.getNextStage(this).color;
	}

	List<LevelEnchantment> getNextStageEnchantments(ItemType type){
		return StageManager.getNextStage(this).enchantments;
	}

	int getNextStageBonus(ItemType type,String bonus){

		return StageManager.getNextStage(this).getBonus(bonus);

	}

	void apply(ItemStack item)
	{
		/*
		 * 1. si l'item n'as pas de nom personnalisé
		 *  - L'item viens d'etre levellé
		 *  - il n'y a pas eu de stage avant cela.
		 *  -> creation de son nom + prefixe et suffixe (pas de verification)
		 * 
		 * 2. si l'item en a un
		 *  - Soit l'item avait deja un nom custom et viens d'etre levellé:
		 *   -> verification si l'item avait un leveldata qui est faux
		 *    si c'est faux alors le nom custom est entouré des prefixes et suffixes.
		 *  
		 *  - Soit l'item est deja levellé
		 *   -> verification s'il y avait un leveldata qui est vrai
		 *   si c'est vrai alors verification si l'item a un level charniere (si Stage(lvl - 1) != this) et qu'il n'as pas eu d'xp (xp = 0) 
		 * 	  si c'est vrai alors verification des prefixes precedents
		 *     si y en a on les vire
		 *    puis verification de la présence des prefixes et suffixes actuels
		 *     si ils manquent on les ajoute.
		 *
		 * 
		 */
		
		if (item.hasItemMeta())
		{
			ItemMeta meta = item.getItemMeta();
			String nomitem = "";
			
			if(!meta.hasDisplayName())
				nomitem = getColor() + TypeChecker.getInGameName(item.getType());
			else
			{
				if(!LevelDataManager.hasLevelData(item))
					nomitem = meta.getDisplayName();
				else
				{
					
					if(StageManager.getStage(LevelDataManager.getLevel(meta) - 1) != this
							& LevelDataManager.getExperience(meta, true) == 0)
					{
						Stage lstStg = StageManager.getStage(LevelDataManager.getLevel(meta) - 1);
						
						nomitem = meta.getDisplayName().replace(lstStg.prefix, "").replace(lstStg.suffix, "");
					}
				}
			}
			
			
			
			
			if(prefix != null)
				if(prefix != "" || prefix.contains("null"))
					if(!meta.getDisplayName().startsWith(prefix))
						nomitem = prefix + nomitem;
			
			if(suffix != null)
				if(suffix != "" || suffix.contains("null"))
					if(!meta.getDisplayName().endsWith(suffix))
						nomitem = nomitem + suffix;
			
			
			meta.setDisplayName(nomitem);


			item.setItemMeta(meta);

			boolean hasEnchants = false;
			Map<Enchantment, Integer> enchants = item.getEnchantments();
			if(!enchants.isEmpty()) hasEnchants = true;

			for (LevelEnchantment enchantment : getEnchantments())
			{
				if (hasEnchants)
				{
					if (enchants.keySet().contains(enchantment.getEnchantment()))
					{
						if ( enchants.get(enchantment.getEnchantment()) <= enchantment.getLevel() )
						{
							enchantment.apply(item);
						}
					}
				}
				else
					enchantment.apply(item);

			}
		}
	}

	List<ItemStack> getItemRewards() {
		return this.itemsReward;
	}
}
