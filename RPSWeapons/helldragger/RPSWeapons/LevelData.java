package helldragger.RPSWeapons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


class LevelData extends ItemStack
{

	private Bow bowsubclass;

	private ItemStack itemStack;

	private ItemType type;

	private Stage stage;

	private int level;

	private int experience;

	private Player holder;

	private Player owner;

	private boolean hasExperienceBar;

	private double value = 100;

	//		double blocscass�s = 0;
	//	
	//		double mobstu�s = 0;
	//	
	//		double playerstu�s = 0;
	//	
	//		double monstrestu�s = 0;
	//	
	//		double boisbuch� = 0;
	//	
	//		double terrelabour�e = 0;
	//	
	//		double herbelabour�e = 0;
	//	
	//		double poissonsp�ch�s = 0;
	//	
	//		double tr�sorsp�ch�s = 0;
	//	
	//		double fl�chestir�es = 0;
	//	
	//		double fl�chetouchante = 0;

	private HashMap<Player,Float> holdersMastery = new HashMap<Player,Float>();

	private float mastery = 0;

	LevelData(ItemStack itemStack)
	{
		this.itemStack = itemStack;
		this.hasExperienceBar = LevelDataManager.hasExperienceBar(itemStack);
		this.level = 1;

		if (LevelDataManager.hasLevelData(itemStack))
		{
			readLevelData();
		}
		else
		{
			createNewLevelData();
		}
	}

	LevelData(ItemStack itemStack, Player player)
	{
		this.itemStack = itemStack;
		this.hasExperienceBar = LevelDataManager.hasExperienceBar(itemStack);
		this.holder = player;
		this.owner = player;
		this.level = 0;

		if (LevelDataManager.hasLevelData(itemStack))
		{
			if(Config.DEBUG_MODE) player.sendMessage("Creation de LevelData avort�e, level data d�j� pr�sent, lecture des donn�es.");
			readLevelData();

		}
		else
		{
			if(Config.DEBUG_MODE) player.sendMessage("Creation d'un nouvel item levell�, bravo! :)");
			createNewLevelData();
		}
	}


	LevelData(ItemStack itemStack, boolean experienceBar)
	{
		this.itemStack = itemStack;
		this.hasExperienceBar = experienceBar;

		if (LevelDataManager.hasLevelData(itemStack))
		{
			readLevelData();
		}
		else
		{
			createNewLevelData();
		}
	}


	String getDisplayName()
	{

		if (itemStack.getItemMeta().hasDisplayName())
			return itemStack.getItemMeta().getDisplayName();
		else
			return itemStack.getType().name();
	}

	ItemStack getItemStack()
	{
		return itemStack;
	}

	LevelData getLevelData(){
		return this;
	}

	ItemType getItemType(){
		return type;
	}

	void setBowSubClass(){
		this.bowsubclass = new Bow(this);
	}

	Bow getBowSubClass(){
		if (this.bowsubclass != null)
			return this.bowsubclass;
		else 
			return null;
	}
	/*
	 * Operations avec le porteur de l'item
	 * infos sur lui
	 * 
	 * 
	 */
	HashMap<Player, Float> getHolderList()
	{
		return this.holdersMastery;
	}

	Player getHolder()
	{
		return holder;
	}
	void setHolder(Player holder)
	{
		this.holder = holder;
		return;
	}

	/*
	 * Infos mastery du porteur
	 * 
	 * 
	 */

	Float getHolderMastery()
	{
		if (holdersMastery.get(holder) != null)
			return holdersMastery.get(holder);
		else return (float) 0;
	}

	Float getHolderMastery(Player holder)
	{
		if (holdersMastery.get(holder) != null)
			return holdersMastery.get(holder);
		else return (float) 0;
	}

	Float getMastery()
	{
		if (holdersMastery.get(holder) != null)
			return holdersMastery.get(holder);
		else return (float) 0;
	}

	void setHolderMastery(Float masterypercentage)
	{
		this.mastery = (masterypercentage * Float.intBitsToFloat(Config.MAX_MASTERY_POINTS) / Float.floatToIntBits(100)) + (masterypercentage* Float.floatToIntBits(Config.MAX_MASTERY_POINTS) % Float.intBitsToFloat(100)); 
	}

	void setMastery(int masterypercentage)
	{
		this.mastery = (masterypercentage * Config.MAX_MASTERY_POINTS / 100) + (masterypercentage*Config.MAX_MASTERY_POINTS % 100); 
	}

	void setHolderMastery(Player holder, Float masterypercentage)
	{
		float masteryPoints = ((masterypercentage * Float.intBitsToFloat(Config.MAX_MASTERY_POINTS) / Float.intBitsToFloat(100)) + (masterypercentage* Float.intBitsToFloat(Config.MAX_MASTERY_POINTS) % Float.intBitsToFloat(100)));
		if (holdersMastery.get(holder) != null)
			this.holdersMastery.put(holder, (Float) masteryPoints);

	}

	/*
	 * Operations avec le propri�taire de l'item
	 * infos sur lui
	 * 
	 * 
	 */
	Player getOwner()
	{

		if(this.hasItemMeta())
			if(this.getItemMeta().hasLore())
				return LevelDataManager.getOwner(this.getItemMeta());

		return this.owner;
	}
	void setOwner(Player owner){
		this.owner = owner;
		return;
	}

	/*
	 * Operations avec les infos de l'item
	 * infos sur durabilit�
	 * 
	 * 
	 */

	boolean setDurability(int i)
	{
		this.itemStack.setDurability((short) i);
		return true;
	}



	/*
	 * Operations avec le LevelData de l'item
	 * infos sur le stage
	 * 
	 * 
	 */

	Stage getStage()
	{
		return stage;
	}

	private void setStage(Stage stage){
		this.stage = stage;
	}


	/*
	 * infos sur le level
	 * 
	 * 
	 */

	int getLevel()
	{
		return level;
	}

	private boolean addLevel(Player player)
	{
		Stage stage = LevelDataManager.getStage(type, level + 1);

		if (Permissions.hasStagePermission(player, stage.getName()))
		{
			level++;
			experience = 0;
			mastery = mastery + 100;

			//faire en sorte qu'� chaque level  up les bonus augmentent pour faire echellonnage des differents stages


			if(stage.getName() != this.getStage().getName()	)
			{
				itemStack.setDurability(Short.parseShort(Integer.toString(itemStack.getDurability() - Config.DURABILITY_BONUS_ON_RANK_UP)));
				player.sendMessage(ChatColor.GREEN+"Your item ranked up and gain "+Config.DURABILITY_BONUS_ON_RANK_UP+" durability!");
				this.stage = stage;

				giveReward(this, player);


			}

			return true;
		}
		else
		{
			return false;
		}
	}

	void setLevel(int level)
	{
		this.level = level;
		update();
	}


	int getExperience()
	{
		return experience;
	}

	void setExperience(int experience){
		this.experience = experience;
	}

	int getExperienceBeforeLvlUp(){
		return ((Config.LEVEL_XP_MULTIPLIER*level)-experience);
	}

	boolean addExperience(Player player, int amount)
	{
		experience += amount;


		if (this.getHolder() instanceof Player)
		{
			if (player.getName() == this.getHolder().getName())
				mastery++;
			else 
			{
				mastery = 0;
			}
		}
		boolean levelUp = false;

		while (experience >= (Config.LEVEL_XP_MULTIPLIER*level))
		{
			if (addLevel(player) && (level+1 < Util.getMaxLevel(player, TypeChecker.getItemType( this.getItemStack().getType() ) ) ) )
			{
				levelUp = true;
			}
			else
			{
				levelUp = false;
				break;
			}
		}

		update();

		return levelUp;
	}

	/*
	 * infos sur la barre d'xp
	 * 
	 * 
	 */

	boolean hasExperienceBar()
	{
		return hasExperienceBar;
	}

	void setExperienceBar(boolean experienceBar)
	{
		hasExperienceBar = experienceBar;
		update();
	}

	/*
	 * lecture des infos du levelData
	 * 
	 * 
	 */

	private void readLevelData()
	{


		ItemMeta meta = itemStack.getItemMeta();

		type = LevelDataManager.getType(itemStack);
		level = LevelDataManager.getLevel(meta);
		
		mastery = LevelDataManager.getMastery(meta);
		owner = LevelDataManager.getOwner(meta);

		stage = LevelDataManager.getStage(type, level);

	}

	/*
	 * Operations de recuperation
	 * des donn�es d'un level data
	 * inconnu
	 * 
	 * 
	 */

	static LevelData recoverLevelData(ItemStack itemStack){
		//creation d'un nouveau levelData a modif.

		itemStack.setDurability(Short.valueOf(Integer.toString(itemStack.getDurability())));

		LevelData recoveredLevelData = new LevelData(itemStack);

		//recuperation des informations necessaires sur l'item
		ItemMeta meta = itemStack.getItemMeta();

		ItemType type = LevelDataManager.getType(itemStack);
		int level =  LevelDataManager.getLevel(meta);
		boolean hasExperienceBar = LevelDataManager.hasExperienceBar(itemStack);

		if (itemStack.getType() == Material.BOW)
			recoveredLevelData.setBowSubClass();


		//recuperation des infos et creation du levelData retrouv�
		recoveredLevelData.setStage(LevelDataManager.getStage(type, level));
		recoveredLevelData.setLevel(level);
		recoveredLevelData.setExperience(LevelDataManager.getExperience(meta, hasExperienceBar));
		recoveredLevelData.setOwner(LevelDataManager.hasOwner(meta));
		recoveredLevelData.setHolderMastery(LevelDataManager.getMastery(meta));
		recoveredLevelData.setHolder(LevelDataManager.getHolder(meta));

		return recoveredLevelData;
	}

	static LevelData recoverLevelData(ItemStack itemStack,Player player){
		//creation d'un nouveau levelData a modif.

		itemStack.setDurability(Short.valueOf(Integer.toString(itemStack.getDurability())));

		if(Config.DEBUG_MODE) player.sendMessage("Debug: durability item ="+ Short.toString(itemStack.getDurability()));
		LevelData recoveredLevelData = new LevelData(itemStack, player);
		if(Config.DEBUG_MODE) player.sendMessage("Debug: durability lvldata ="+ Short.toString(recoveredLevelData.getDurability()));

		//recuperation des informations necessaires sur l'item
		ItemMeta meta = itemStack.getItemMeta();

		ItemType type = LevelDataManager.getType(itemStack);
		int level =  LevelDataManager.getLevel(meta);
		boolean hasExperienceBar = LevelDataManager.hasExperienceBar(itemStack);

		//recuperation des infos et creation du levelData retrouv�
		if (itemStack.getType() == Material.BOW)
			recoveredLevelData.setBowSubClass();

		recoveredLevelData.setStage(LevelDataManager.getStage(type, level));
		recoveredLevelData.setLevel(level);
		recoveredLevelData.setExperience(LevelDataManager.getExperience(meta, hasExperienceBar));
		recoveredLevelData.setHolderMastery(LevelDataManager.getMastery(meta));
		recoveredLevelData.setHolder(player);

		return recoveredLevelData;
	}

	/*
	 * Ecriture d'un level data dans les meta de l'item
	 * 
	 */


	private void writeLevelData()
	{


		ItemMeta meta = itemStack.getItemMeta();

		if (meta == null)
		{
			return;
		}

		List<String> lore = meta.getLore();

		if (lore == null) 
		{
			lore = new ArrayList<String>();
		}

		List<String> othersData = new ArrayList<String>();

		if(LevelDataManager.hasLevelData(itemStack))
		{
			if(!lore.isEmpty())
			{
				try
				{
					Iterator<String> it = lore.iterator();

					while (it.hasNext())
					{
						boolean onvire = false;

						String lorestrings = it.next();
						if(!stage.getDescription().isEmpty())
							for(String line : stage.getDescription())
								if(lorestrings.contains(stage.getDescriptionColor()+line))
									onvire = true;

						if(lorestrings.contains(ChatColor.WHITE+"This item")
								||lorestrings.contains(Config.DESCRIPTION_COLOR+"Level")
								||lorestrings.contains(Config.DESCRIPTION_COLOR+"EXP:")
								||lorestrings.contains(Config.DESCRIPTION_COLOR+"Current holder:")
								||lorestrings.contains(Config.DESCRIPTION_COLOR+"Holder's mastery:")
								||lorestrings.contains(ChatColor.GRAY+"Value:")
								||lorestrings.contains(ChatColor.GRAY+"Durability:")
								||lorestrings.contains("xp before level up!")
								||lorestrings.contains(ChatColor.GREEN+""+ChatColor.ITALIC+" Bite chance bonus:")
								||lorestrings.contains(ChatColor.GREEN+""+ChatColor.ITALIC+" Armor bonus:")
								||lorestrings.contains(ChatColor.GREEN+""+ChatColor.ITALIC+" Damage bonus:"))
							onvire=true;

						if(onvire)
						{
							if(Config.DEBUG_MODE) RPSWPlugin.log.info("lore '"+ lorestrings +"' retir� de l'item");
							it.remove();

						}
						else
						{
							othersData.add(lorestrings);
							if(Config.DEBUG_MODE) RPSWPlugin.log.info("lore '"+ lorestrings +"' laiss� sur de l'item");
						}
					}


				}catch(Exception e)
				{
					e.printStackTrace();
				}

			}
		}else
			if (meta.hasLore())
				othersData.addAll(meta.getLore());
			




		lore.clear();

		if(owner != null)
			lore.add(ChatColor.WHITE+"This item is "+ChatColor.BOLD+owner.getDisplayName()+ChatColor.WHITE+" property.");
		else
			lore.add(ChatColor.WHITE+"This item have no owner.");

		lore.add(Config.DESCRIPTION_COLOR + "Level " + level);

		if (hasExperienceBar)
		{
			lore.add(Config.DESCRIPTION_COLOR + "EXP: " + LevelDataManager.createExpBar((Config.LEVEL_XP_MULTIPLIER*level), experience));
			lore.add(Config.DESCRIPTION_COLOR + "    " + getExperienceBeforeLvlUp()+"xp before level up!");

			if(holder != null)
			{
				lore.add(Config.DESCRIPTION_COLOR+"Current holder:"+ChatColor.BOLD+holder.getDisplayName());
				lore.add(Config.DESCRIPTION_COLOR+"Holder's mastery: "+ChatColor.GOLD+((this.mastery * Float.intBitsToFloat(100) / Float.floatToIntBits(Config.MAX_MASTERY_POINTS)))+"%");

			}
			else
			{
				lore.add(Config.DESCRIPTION_COLOR+"Current holder: "+ChatColor.BOLD+"none");
				lore.add(Config.DESCRIPTION_COLOR+"Holder's mastery: "+ChatColor.GOLD+"0%");

			}

		}

		if(TypeChecker.isWeapon(this.getType()) & this.stage.getBonus("damage") > 0)
			lore.add(ChatColor.GREEN+""+ChatColor.ITALIC+" Damage bonus:"+this.stage.getBonus("damage"));

		if(TypeChecker.isArmor(this.getType()) & this.stage.getBonus("armor") > 0)
			lore.add(ChatColor.GREEN+""+ChatColor.ITALIC+" Armor bonus:"+this.stage.getBonus("armor"));

		if(this.getType() == Material.FISHING_ROD & this.stage.getBonus("fishing") > 0)
			lore.add(ChatColor.GREEN+""+ChatColor.ITALIC+" Bite chance bonus:"+this.stage.getBonus("fishing"));

		if(!stage.getDescription().isEmpty())
		{
			for(String line : stage.getDescription())
				lore.add(stage.getDescriptionColor() + line);
		}


		//			{//trucs tu�s (haches et �p�es)
		//				if (this.mobstu�s != 0)
		//					lore.add(Config.DESCRIPTION_COLOR+"Mobs passifs tu�s: "+ChatColor.GOLD+this.mobstu�s);
		//				if (this.monstrestu�s != 0)
		//					lore.add(Config.DESCRIPTION_COLOR+"Monstres tu�s: "+ChatColor.GOLD+this.mobstu�s);
		//				if (this.playerstu�s != 0)
		//					lore.add(Config.DESCRIPTION_COLOR+"Joueurs tu�s: "+ChatColor.GOLD+this.mobstu�s);
		//	
		//			}
		//	
		//			{//blocs cass�s (pioches et pelles)
		//	
		//				if (this.blocscass�s != 0)
		//					lore.add(Config.DESCRIPTION_COLOR+"Blocs cass�s: "+ChatColor.GOLD+this.blocscass�s);
		//	
		//			}
		//	
		//			{//bois buch� (haches)
		//	
		//				if (this.boisbuch� != 0)
		//					lore.add(Config.DESCRIPTION_COLOR+"Bois buch�: "+ChatColor.GOLD+this.boisbuch�);
		//	
		//			}
		//	
		//			{//choses labour�es (houes)
		//	
		//				if (this.herbelabour�e != 0)
		//					lore.add(Config.DESCRIPTION_COLOR+"Herbe labour�e: "+ChatColor.GOLD+this.herbelabour�e);
		//	
		//				if (this.terrelabour�e != 0)
		//					lore.add(Config.DESCRIPTION_COLOR+"Terre labour�e: "+ChatColor.GOLD+this.terrelabour�e);
		//	
		//			}
		//	
		//			{//choses p�ch�es (cannes a peche)
		//	
		//				if (this.poissonsp�ch�s != 0)
		//					lore.add(Config.DESCRIPTION_COLOR+"Poissons p�ch�s: "+ChatColor.GOLD+this.poissonsp�ch�s);
		//	
		//				if (this.tr�sorsp�ch�s != 0)
		//					lore.add(Config.DESCRIPTION_COLOR+"Tr�sors p�ch�s: "+ChatColor.GOLD+this.tr�sorsp�ch�s);
		//	
		//			}
		//	
		//			{//fl�ches tir�es (arc)
		//	
		//				if (this.fl�chestir�es != 0)
		//					lore.add(Config.DESCRIPTION_COLOR+"Fl�ches tir�es: "+ChatColor.GOLD+this.fl�chestir�es);
		//	
		//				if (this.fl�chetouchante != 0)
		//					lore.add(Config.DESCRIPTION_COLOR+"Fl�ches ayant atteint leurs cibles: "+ChatColor.GOLD+this.fl�chetouchante);
		//	
		//	
		//	
		//			}
		//	

		lore.add(ChatColor.GRAY+"Value: "+ChatColor.GOLD+value);
		lore.add(ChatColor.GRAY+"Durability: "+ChatColor.GOLD+""+Integer.toString((itemStack.getType().getMaxDurability() - itemStack.getDurability() - 1))+" /"+Short.toString(itemStack.getType().getMaxDurability()));


		lore.addAll(othersData);

		meta.setLore(lore);
		itemStack.setItemMeta(meta);
	}

	private void createNewLevelData()
	{

		type = LevelDataManager.getType(itemStack);

		level = 1;
		experience = 0;
		value = 100;

		update();
	}

	/*
	 * op�ration de mise a jour des infos sur l'item
	 * 
	 * 
	 */

	void update()
	{
		if (level > Config.MAX_LEVEL)
		{
			level = Config.MAX_LEVEL;
			experience = (Config.LEVEL_XP_MULTIPLIER*level);
		}



		stage = StageManager.getStage(level);

		if (stage != null)
		{
			stage.apply(itemStack);
		}
		else{
			stage = LevelDataManager.getNullStage(type);
			stage.apply(itemStack);

		}
		writeLevelData();
	}

	private void giveReward(LevelData data, Player player)
	{
		Stage stage = data.stage;



		if(stage.getLevel() == data.level && data.getExperience() == 0)
			if(Config.GIVE_ITEM_REWARD_ON_STAGE_UP)
				if (!stage.getItemRewards().isEmpty())
				{

					String itemsgained = "you gained ";

					for(ItemStack itemstack: stage.getItemRewards())
					{
						if(itemstack.getItemMeta().hasDisplayName())
							itemsgained = itemsgained +", "+itemstack.getItemMeta().getDisplayName()+ChatColor.WHITE+" x"+itemstack.getAmount();
						else
							itemsgained = itemsgained +", "+ itemstack.getType().name()+ChatColor.WHITE+" x"+itemstack.getAmount();

						HashMap<Integer, ItemStack> itemLeft = player.getInventory().addItem(itemstack); 
						if(!itemLeft.isEmpty())
							for(Integer i : itemLeft.keySet())
								Util.dropItem(player.getLocation(),itemLeft.get(i));
					}

					player.sendMessage(itemsgained);
				}


	}


}