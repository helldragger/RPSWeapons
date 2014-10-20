package helldragger.RPSWeapons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.minecraft.server.v1_7_R1.EntityPlayer;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;


class Events implements Listener
{
	//TODO houe pas marcher, autorepair, owner pas maarxcher au craft
	private RPSWPlugin plugin;



	HashMap< Arrow, Bow> arrowStorage = new HashMap<Arrow, Bow>();


	List<UUID> spawnStorage = new ArrayList<UUID>();

	Events(RPSWPlugin rpsPlugin)
	{
		plugin = rpsPlugin;
	}







	@EventHandler
	void onPlayerDrop(PlayerDropItemEvent event){


		if (event.getItemDrop().getItemStack() instanceof LevelData)
			LevelDataManager.getLevelData( event.getItemDrop().getItemStack()).setHolder(null);


	}

	@EventHandler
	void onPlayerLoot(PlayerPickupItemEvent event){

		/*
		 * Si objet looté, si il est levellable par le plug in
		 * on verifie s'il a un leveldata associé (selon son type on recherche dans les levelData de son type)
		 * -si oui, on met son owner par le nom du joueur
		 * on l'ajoute a la liste des level data du joueur
		 * on l'associe au joueur dans la liste des leveldata de son type
		 * 
		 * -sinon on crée le leveldata 
		 * et on fait les manips ci dessus 
		 * 
		 * 
		 * 
		 * VERSION ACTUALISEE
		 * 
		 * Verif si item levellable
		 *  si oui, verification de presence de données de leveldata
		 *   si oui mise du joueur en temps que porteur
		 *   si non mise d'un level data avec ce joueur en proprio
		 * 
		 */

		ItemStack itemStack = event.getItem().getItemStack();

		if (TypeChecker.isLevellable(itemStack.getType())
				&& LevelDataManager.hasLevelData(itemStack))
		{
			//si cet item est levellable
			Player player = event.getPlayer();

			if (itemStack instanceof LevelData)
				//si cet item a deja des données de levelling
				((LevelData) event.getItem().getItemStack()).setHolder(player);

		}

	}





	@EventHandler
	void onDamage(EntityDamageByEntityEvent event)
	{
		/*
		 * PARTIE DE L'EVENT CONCERNANT CELUI RECEVANT LES DOMMAGES
		 * 
		 */


		event.getEntityType();
		if (event.getEntityType().equals(EntityType.PLAYER) )
		{
			Player player = (Player) event.getEntity();

			ItemStack[] armor = player.getInventory().getArmorContents();

			if (Config.DEBUG_MODE) player.sendMessage("DEBUG : dégats "+ChatColor.RED+"reçus"+ChatColor.WHITE+", equipement en scan.");
			if(armor.length != 0)
				for (int i = 0; i != armor.length; i++)
				{
					ItemStack itemStack = armor[i];

					short itemdurability = itemStack.getDurability();
					if ( !(itemStack == null) 
							&& TypeChecker.isArmor(itemStack.getType())
							&& Config.isItemEnabled(plugin, itemStack.getType()) 
							&& event.isCancelled() == false
							&& LevelDataManager.hasLevelData(itemStack)
							/* si il y a quelque chose dans le slot d'armure*/
							||(event.getDamager() instanceof Player 
									&& player.getWorld().getPVP())
									/* et/ou si les degats viennent d'un joueur et que lee pvpv est activé */
							)
					{
						Object[] result = levelling(player,itemStack);

						boolean hasPermission = (boolean) result[0];
						boolean xpgain = (boolean) result[1];
						LevelData data = (LevelData) result[2];

						if(data == null)
							return;



						if (hasPermission)
						{

							/*
							 * Bonus d'armure
							 * 
							 * 	1. recuperation du bonus du stage
							 */


							if (data.getStage().getBonus("armor") > 0)
								event.setDamage(event.getDamage() - data.getStage().getBonus("armor"));



							if (Config.DEBUG_MODE) player.sendMessage("DEBUG : "+ChatColor.GREEN+" "+data.getStage().getBonus("armor")+" dégats supplémentaires amortis ("+event.getDamage()+")");


							if(xpgain)
							{
								if (Config.DEBUG_MODE) player.sendMessage("DEBUG : "+ChatColor.GREEN+"Gain de "+Config.EXP_PER_HIT+"xp"+ChatColor.WHITE+" pour "+data.getDisplayName());

								if (data.addExperience(player, Config.EXP_PER_HIT))
								{
									player.sendMessage(data.getDisplayName()+ChatColor.GREEN+ Config.LINES.get("level up") +data.getLevel()+" !");

									Util.dropExperience(player.getLocation(), Config.EXP_ON_LEVEL, 3);
								}
								else
								{
									if(Config.DEBUG_MODE) player.sendMessage(data.getDisplayName()+ChatColor.GREEN+" a gagné "+ChatColor.WHITE+Config.EXP_PER_HIT+ChatColor.GREEN+" ("+data.getExperience()+"/"+(data.getExperience()+data.getExperienceBeforeLvlUp())+")");
								}

							}
						}
						if (data.getDurability() != itemdurability)
						{
							data.setDurability(itemdurability);
						}
						data.update();
					}

				}

		}



		/*
		 * PARTIE DE L'EVENT CONCERNANT CELUI FAISANT DES DEGATS
		 * DEGATS CORPS A CORPS
		 * 
		 */


		if (event.getDamager().getType().equals(EntityType.PLAYER) )
		{

			Player player = (Player) event.getDamager();
			if (Config.DEBUG_MODE) player.sendMessage("DEBUG : dégats "+ChatColor.GREEN+"infligés"+ChatColor.WHITE+", item en main en scan.");

			if (player.getGameMode().equals(GameMode.SURVIVAL) )
			{

				ItemStack itemStack = player.getItemInHand();

				if (!(itemStack == null) 
						&& Config.isItemEnabled(plugin, itemStack.getType() ) //que l'itm est activé
						&& !event.isCancelled() //que l'event 'est pas annulé
						&& !event.getEntity().hasMetadata("NPC") //que les degats ne viennent pas d'un npc
						&& !(itemStack.getAmount() > 1) //si l'item n'en contient pas plusieurs
						&& TypeChecker.isWeapon( itemStack.getType() ) //si l'arme est une arme
						&& TypeChecker.isLevellable( itemStack.getType() ) //si l'arme est levellable
						&& itemStack.getType() != Material.ARROW //si l'item n'est pas une fleche ni un arc
						&& itemStack.getType() != Material.BOW
						&& LevelDataManager.hasLevelData(itemStack)
						)//que le pvp est activé) //que l'xp proviens des mobs des spawner

				{
					if((Config.DISABLE_SPAWNERS //si les spawners sont desactivés
							&& spawnStorage.contains(event.getEntity().getUniqueId())))
					{
						if(Config.DEBUG_MODE) player.sendMessage(ChatColor.RED+"Gain d'xp désactivé sur les mobs provenant de spawners.");

						return;
					}

					if((event.getEntity() instanceof Player //si c'est un joueur
							&& !player.getWorld().getPVP()))
					{
						if(Config.DEBUG_MODE) player.sendMessage(ChatColor.RED+"PVP désactivé dans cette zone.");
						return;
					}

					Object[] result = levelling(player,itemStack);

					boolean hasPermission = (boolean) result[0];
					boolean xpgain = (boolean) result[1];
					LevelData data = (LevelData) result[2];

					if(data == null)
						return;

					if (hasPermission)
					{

						if (data.getStage().getBonus("damage") > 0)
							event.setDamage(event.getDamage() + data.getStage().getBonus("damage"));



						if (Config.DEBUG_MODE) player.sendMessage("DEBUG : "+ChatColor.GREEN+" "+data.getStage().getBonus("damage")+" dégats supplémentaires infligés ("+event.getDamage()+")");


						if(xpgain)
						{

							if (Config.DEBUG_MODE) player.sendMessage("DEBUG : "+ChatColor.GREEN+"Gain de "+Config.EXP_PER_HIT+"xp"+ChatColor.WHITE+" pour "+data.getDisplayName());


							if (data.addExperience(player, Config.EXP_PER_HIT))
							{
								player.sendMessage(data.getDisplayName()+ChatColor.GREEN+ Config.LINES.get("level up") +data.getLevel()+" !");
								Util.dropExperience(player.getLocation(), Config.EXP_ON_LEVEL, 3);
							}
							else
							{
								if(Config.DEBUG_MODE) player.sendMessage(data.getDisplayName()+ChatColor.GREEN+" a gagné "+ChatColor.WHITE+Config.EXP_PER_HIT+ChatColor.GREEN+" ("+data.getExperience()+"/"+(data.getExperience()+data.getExperienceBeforeLvlUp())+")");
							}

						}
						data.update();

					}else
					{
						if(Config.CANCEL_USE_OF_TOO_HIGH_ITEMS)
						{
							event.setCancelled(true);
							return;
						}

					}

				}
			}
		}





		/*
		 * PARTIE DE L'EVENT CONCERNANT CELUI FAISANT DES DEGATS
		 * DEGATS FLECHES TIREES
		 * 
		 */

		if (event.getDamager().getType() == EntityType.ARROW)
		{

			Arrow arrow = (Arrow) event.getDamager();

			if (arrowStorage.containsKey(arrow))
			{
				Bow bowdata = arrowStorage.get(arrow);
				Stage stage = bowdata.getLevelData().getStage();


				event.setDamage(event.getDamage() + stage.getBonus("damage"));
				Player player = (Player) bowdata.getShooter();


				if (Config.DEBUG_MODE) player.sendMessage("DEBUG : Degats "+ChatColor.GREEN+"infligés"+ChatColor.WHITE+" ("+ChatColor.RED+event.getDamage()+ChatColor.WHITE+") "+ChatColor.GREEN+"augmentés"+ChatColor.WHITE+" de "+ChatColor.RED+stage.getBonus("damage")+ChatColor.WHITE+".");


				if(player.getGameMode().equals(GameMode.SURVIVAL))
				{

					LevelData data = bowdata.getLevelData();

					//					data.flêchetouchante +=1 ;

					Object[] result = levelling(player,bowdata.getBow());

					boolean hasPermission = (boolean) result[0];
					boolean xpgain = (boolean) result[1];

					if(data == null)
						return;

					if (hasPermission)
					{



						if (data.getStage().getBonus("damage") > 0)
							event.setDamage(event.getDamage() + data.getStage().getBonus("damage"));


						if (Config.DEBUG_MODE) player.sendMessage("DEBUG : "+ChatColor.GREEN+" "+data.getStage().getBonus("damage")+" dégats supplémentaires infligés ("+event.getDamage()+")");



						if(xpgain)
						{

							if (Config.DEBUG_MODE) player.sendMessage("DEBUG : "+ChatColor.GREEN+"Gain de "+Config.EXP_PER_HIT+"xp"+ChatColor.WHITE+" pour "+data.getDisplayName());



							if (data.addExperience(player, Config.EXP_PER_HIT))
							{
								player.sendMessage(data.getDisplayName()+ChatColor.GREEN+ Config.LINES.get("level up") +data.getLevel()+" !");
								Util.dropExperience(arrow.getLocation(), Config.EXP_ON_LEVEL, 3);
							}
							else
							{
								if(Config.DEBUG_MODE) player.sendMessage(data.getDisplayName()+ChatColor.GREEN+" a gagné "+ChatColor.WHITE+Config.EXP_PER_HIT+ChatColor.GREEN+" ("+data.getExperience()+"/"+(data.getExperienceBeforeLvlUp()+data.getExperience())+")");
							}


						}
					}
					else
					{
						if(Config.CANCEL_USE_OF_TOO_HIGH_ITEMS)
						{
							event.setCancelled(true);
							return;
						}

					}

					arrowStorage.remove(arrow);
					data.update();
				}
			}
		}
	}




	@EventHandler
	void onLabour(PlayerInteractEvent event)
	{
		//si c'est un bloc labourable 
		//puis si l'objet se transforme en dirt (bloc labourable)
		//verification que le joueur a bien une houe dans la main

		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();
		if (itemStack != null)
		{

			if(TypeChecker.isTool(itemStack.getType())
					&& event.getAction() == Action.RIGHT_CLICK_BLOCK
					&& LevelDataManager.hasLevelData(itemStack))//si c'est un outil et s'il ne casse pas!
				if (
						(event.getClickedBlock().getType() == Material.DIRT //si c'est de la terre
						|| event.getClickedBlock().getType() == Material.GRASS // ou si c'est de l'herbe
						|| event.getClickedBlock().getType() == Material.MYCEL) // ou si c'est du mycelium
						&& ToolType.getByItemName(itemStack.getType().name()) == ToolType.HOE ) //et si c'est une houe
				{


					//si c'est une houe

					//levelling!!
					Object[] result = levelling(player,itemStack);

					boolean hasPermission = (boolean) result[0];
					boolean xpgain = (boolean) result[1];
					LevelData data = (LevelData) result[2];

					if(data == null)
						return;

					//				if(event.getClickedBlock().getType() == Material.GRASS)
					//					data.herbelabourée += 1;
					//				else
					//					data.terrelabourée += 1;

					if (hasPermission)
					{

						if(xpgain)
						{

							if (Config.DEBUG_MODE) player.sendMessage("DEBUG : "+ChatColor.GREEN+"Gain de "+Config.EXP_PER_HIT+"xp"+ChatColor.WHITE+" pour "+data.getDisplayName());


							if (data.addExperience(player, Config.getDeathExperience(plugin, event.getPlayer().getType())))
							{
								player.sendMessage(data.getDisplayName()+ChatColor.GREEN+ Config.LINES.get("level up") +data.getLevel()+" !");
								Util.dropExperience(player.getLocation(), Config.EXP_ON_LEVEL, 3);
							}else
							{
								if(Config.DEBUG_MODE) player.sendMessage(data.getDisplayName()+ChatColor.GREEN+" a gagné "+ChatColor.WHITE+Config.EXP_PER_HIT+ChatColor.GREEN+" ("+data.getExperience()+"/"+(data.getExperience()+data.getExperienceBeforeLvlUp())+")");
							}

						}

					}else
					{
						if(Config.CANCEL_USE_OF_TOO_HIGH_ITEMS)
						{
							event.setCancelled(true);
							return;
						}

					}
					data.update();

				} else {
					if(event.getAction() == Action.RIGHT_CLICK_AIR)
						return;
				}
		}

	}

	@EventHandler
	void onDeath(EntityDeathEvent event)
	{


		if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent)
		{
			EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause();

			if (damageEvent.getDamager() instanceof EntityPlayer)
			{
				Player player = (Player) damageEvent.getDamager();


				if (player.getGameMode().equals(GameMode.SURVIVAL))
				{
					ItemStack itemStack = player.getItemInHand();




					if (	   itemStack == null 
							|| !TypeChecker.isLevellable(itemStack.getType()) 
							|| !Config.isItemEnabled(plugin, itemStack.getType()) 
							|| itemStack.getAmount() > 1
							|| !LevelDataManager.hasLevelData(itemStack))
					{
						return;
					}

					if (TypeChecker.isWeapon(itemStack.getType())){


						Object[] result = levelling(player,itemStack);

						boolean hasPermission = (boolean) result[0];
						boolean xpgain = (boolean) result[1];
						LevelData data = (LevelData) result[2];

						if(data == null)
							return;


						//						if (event.getEntityType() == EntityType.BAT
						//								|| event.getEntityType() == EntityType.CHICKEN
						//								|| event.getEntityType() == EntityType.COW
						//								|| event.getEntityType() == EntityType.HORSE
						//								|| event.getEntityType() == EntityType.IRON_GOLEM
						//								|| event.getEntityType() == EntityType.MUSHROOM_COW
						//								|| event.getEntityType() == EntityType.PIG
						//								|| event.getEntityType() == EntityType.SHEEP
						//								|| event.getEntityType() == EntityType.SNOWMAN
						//								|| event.getEntityType() == EntityType.SQUID
						//								|| event.getEntityType() == EntityType.WOLF
						//								|| event.getEntityType() == EntityType.VILLAGER
						//								|| event.getEntityType() == EntityType.OCELOT)
						//							data.mobstués += 1;
						//						else if (event.getEntityType() == EntityType.BLAZE
						//								|| event.getEntityType() == EntityType.CAVE_SPIDER
						//								|| event.getEntityType() == EntityType.CREEPER
						//								|| event.getEntityType() == EntityType.ENDER_DRAGON
						//								|| event.getEntityType() == EntityType.ENDERMAN
						//								|| event.getEntityType() == EntityType.GHAST
						//								|| event.getEntityType() == EntityType.GIANT
						//								|| event.getEntityType() == EntityType.MAGMA_CUBE
						//								|| event.getEntityType() == EntityType.PIG_ZOMBIE
						//								|| event.getEntityType() == EntityType.SILVERFISH
						//								|| event.getEntityType() == EntityType.SKELETON
						//								|| event.getEntityType() == EntityType.SLIME
						//								|| event.getEntityType() == EntityType.SPIDER
						//								|| event.getEntityType() == EntityType.WITCH
						//								|| event.getEntityType() == EntityType.WITHER
						//								|| event.getEntityType() == EntityType.WITHER_SKULL)
						//							data.monstrestués += 1;
						//						else if (event.getEntityType() == EntityType.PLAYER)
						//							data.playerstués += 1;
						//							

						if (spawnStorage.contains(event.getEntity().getUniqueId()))
						{
							spawnStorage.remove(event.getEntity().getUniqueId());

							if (Config.DISABLE_SPAWNERS)
							{
								return;
							}
						}


						if (hasPermission)
						{

							if(xpgain)
							{

								if (Config.DEBUG_MODE) player.sendMessage("DEBUG : "+ChatColor.GREEN+"Gain de "+Config.EXP_PER_HIT+"xp"+ChatColor.WHITE+" pour "+data.getDisplayName());


								if (data.addExperience(player, Config.getDeathExperience(plugin, event.getEntityType())))
								{
									player.sendMessage(data.getDisplayName()+ChatColor.GREEN+ Config.LINES.get("level up") +data.getLevel()+" !");
									Util.dropExperience(player.getLocation(), Config.EXP_ON_LEVEL, 3);
								}else
								{
									if(Config.DEBUG_MODE) player.sendMessage(data.getDisplayName()+ChatColor.GREEN+" a gagné "+ChatColor.WHITE+Config.EXP_PER_HIT+ChatColor.GREEN+" ("+data.getExperience()+"/"+(data.getExperience()+data.getExperienceBeforeLvlUp())+")");
								}

							}

						}

						data.update();




					}



				}
			}
		}
	}

	@EventHandler
	void onSpawn(CreatureSpawnEvent event)
	{
		if (event.getSpawnReason() == SpawnReason.SPAWNER)
		{
			spawnStorage.add(event.getEntity().getUniqueId());
		}
	}


	void onSheepCut(PlayerShearEntityEvent event)
	{
		if (event.getEntity().getType().compareTo(EntityType.SHEEP) == 0)
		{
			Player player = event.getPlayer();
			ItemStack itemStack = event.getPlayer().getItemInHand();
			if (TypeChecker.isLevellable(itemStack.getType())){

				if (Config.DEBUG_MODE) player.sendMessage("DEBUG : item Levellable ="+ChatColor.GREEN+" OUI");


				if (!event.isCancelled()
						&& LevelDataManager.hasLevelData(itemStack))
				{
					if (Config.DEBUG_MODE) event.getPlayer().sendMessage("DEBUG : Utilisation outil = "+ChatColor.GREEN+"OK"+ChatColor.WHITE+"; tentative de levelling...");

					Object[] result = levelling(event.getPlayer(),itemStack);

					boolean hasPermission = (boolean) result[0];
					boolean xpgain = (boolean) result[1];
					LevelData data = (LevelData) result[2];

					if(data == null)
						return;

					//					if(ToolType.getByItemName( itemStack.getType().name() ) == ToolType.AXE)
					//						data.boisbuché += 1;
					//					else 
					//						data.blocscassés += 1;


					if (hasPermission)
					{

						if(xpgain)
						{


							if (Config.DEBUG_MODE) player.sendMessage("DEBUG : "+ChatColor.GREEN+"Gain de "+Config.EXP_PER_HIT+"xp"+ChatColor.WHITE+" pour "+data.getDisplayName());


							if (data.addExperience(event.getPlayer(), Config.EXP_PER_HIT))
							{

								player.sendMessage(data.getDisplayName()+ChatColor.GREEN+ Config.LINES.get("level up") +data.getLevel()+" !");


								Util.dropExperience(event.getEntity().getLocation(), Config.EXP_ON_LEVEL, 3);
							}else
							{
								if(Config.DEBUG_MODE) event.getPlayer().sendMessage(data.getDisplayName()+ChatColor.GREEN+" a gagné "+ChatColor.WHITE+Config.EXP_PER_HIT+ChatColor.GREEN+" ("+data.getExperience()+"/"+(data.getExperience()+data.getExperienceBeforeLvlUp())+")");
							}

						}

						else
						{
							if(Config.CANCEL_USE_OF_TOO_HIGH_ITEMS)
							{
								event.getPlayer().sendMessage(ChatColor.RED + Config.LINES.get("too high level"));
								event.setCancelled(true);
								return;
							}

						}
					}else
					{
						if(Config.CANCEL_USE_OF_TOO_HIGH_ITEMS)
						{
							event.getPlayer().sendMessage(ChatColor.RED + Config.LINES.get("too high level"));
							event.setCancelled(true);
							return;
						}

					}

				}
				else{

					if (Config.DEBUG_MODE) event.getPlayer().sendMessage(ChatColor.RED+"Mauvais outil ou item sans levels!");

				}


			}
		}

	}





	@EventHandler
	void onBlockBreak(BlockBreakEvent event)
	{
		Player player = event.getPlayer();
		ItemStack itemStack = event.getPlayer().getItemInHand();

		if ( TypeChecker.isTool(itemStack.getType())
				&& ToolType.getByItemName(itemStack.getType().name()) != ToolType.HOE
				&& ToolType.getByItemName(itemStack.getType().name()) != null)
		{


			if (TypeChecker.isLevellable(itemStack.getType())){

				if (Config.DEBUG_MODE) player.sendMessage("DEBUG : item Levellable ="+ChatColor.GREEN+" OUI");


				if (  TypeChecker.isCorrectTool( ToolType.getByItemName( itemStack.getType().name() ) , event.getBlock() ) 
						&&	!event.isCancelled()
						&& LevelDataManager.hasLevelData(itemStack))
				{
					if (Config.DEBUG_MODE) event.getPlayer().sendMessage("DEBUG : Utilisation outil = "+ChatColor.GREEN+"OK"+ChatColor.WHITE+"; tentative de levelling...");

					Object[] result = levelling(event.getPlayer(),itemStack);

					boolean hasPermission = (boolean) result[0];
					boolean xpgain = (boolean) result[1];
					LevelData data = (LevelData) result[2];

					if(data == null)
						return;

					//					if(ToolType.getByItemName( itemStack.getType().name() ) == ToolType.AXE)
					//						data.boisbuché += 1;
					//					else 
					//						data.blocscassés += 1;


					if (hasPermission)
					{

						if(xpgain)
						{


							if (Config.DEBUG_MODE) player.sendMessage("DEBUG : "+ChatColor.GREEN+"Gain de "+Config.EXP_PER_HIT+"xp"+ChatColor.WHITE+" pour "+data.getDisplayName());


							if (data.addExperience(event.getPlayer(), Config.EXP_PER_HIT))
							{

								player.sendMessage(data.getDisplayName()+ChatColor.GREEN+ Config.LINES.get("level up")+data.getLevel()+" !");


								Util.dropExperience(event.getBlock().getLocation(), Config.EXP_ON_LEVEL, 3);
							}else
							{
								if(Config.DEBUG_MODE) event.getPlayer().sendMessage(data.getDisplayName()+ChatColor.GREEN+" a gagné "+ChatColor.WHITE+Config.EXP_PER_HIT+ChatColor.GREEN+" ("+data.getExperience()+"/"+(data.getExperience()+data.getExperienceBeforeLvlUp())+")");
							}

						}

						else
						{
							event.getPlayer().sendMessage(ChatColor.RED + Config.LINES.get("too high level"));
						}
						data.update();
					}
					else
					{
						if(Config.CANCEL_USE_OF_TOO_HIGH_ITEMS)
						{
							event.setCancelled(true);
							return;
						}

					}

				}
				else{

					if (Config.DEBUG_MODE) event.getPlayer().sendMessage(ChatColor.RED+"Mauvais outil ou item sans levels!");

				}


			}
		}else

			if (Config.DEBUG_MODE) event.getPlayer().sendMessage(ChatColor.RED+"Ceci n'est pas fait pour ce genre de choses!");

	}

	@EventHandler
	void onPlayerFish(PlayerFishEvent event)
	{
		Player player = event.getPlayer();
		Fish hook = event.getHook();
		ItemStack rod = player.getItemInHand();

		if(LevelDataManager.hasLevelData(rod))
		{

			/*
			 * Action
			 * 
			 */

			switch (event.getState())
			{
			case FISHING:

				LevelData data = LevelDataManager.getLevelData(rod);


				if (Config.DEBUG_MODE) player.sendMessage("DEBUG : "+ChatColor.GOLD+"Chance de la Mer "+ChatColor.WHITE+"("+ChatColor.BLUE+hook.getBiteChance()+ChatColor.WHITE+")  améliorée de "+ChatColor.BLUE+data.getStage().getBonus("fishing")+ChatColor.WHITE+" avec "+data.getDisplayName());



				hook.setBiteChance(hook.getBiteChance() + data.getStage().getBonus("fishing"));
				break;
			case CAUGHT_FISH:




				Object[] result = levelling(player, rod);

				boolean hasPermission = (boolean) result[0];
				boolean xpgain = (boolean) result[1];
				data = (LevelData) result[2];


				if (hasPermission)
				{

					if(xpgain)
					{

						if (Config.DEBUG_MODE) player.sendMessage("DEBUG : "+ChatColor.GREEN+"Gain de "+Config.EXP_PER_HIT+"xp"+ChatColor.WHITE+" pour "+data.getDisplayName());


						//						if (event.getHook() instanceof Fish )
						//							data.poissonspéchés += 1 ;
						//						else 
						//							data.trésorspéchés += 1 ;

						if (data.addExperience(player, Config.EXP_PER_HIT))
						{
							player.sendMessage(data.getDisplayName()+ChatColor.GREEN+ Config.LINES.get("level up") +data.getLevel()+" !");


							Util.dropExperience(player.getLocation(), Config.EXP_ON_LEVEL, 3);
						}
						else
						{
							if(Config.DEBUG_MODE) player.sendMessage(data.getDisplayName()+ChatColor.GREEN+" a gagné "+ChatColor.WHITE+Config.EXP_PER_HIT+ChatColor.GREEN+" ("+data.getExperience()+"/"+(data.getExperience()+data.getExperienceBeforeLvlUp())+")");
						}


					}
					data.update();
				}
				else
				{
					if(Config.CANCEL_USE_OF_TOO_HIGH_ITEMS)
					{
						event.setCancelled(true);
						return;
					}

				}


				break;
			default:
				break;
			}

		}

	}

	//	@EventHandler
	//	void onChangingName(InventoryClickEvent e){
	//		// check if the event has been cancelled by another plugin
	//		if(!e.isCancelled()){
	//			HumanEntity ent = e.getWhoClicked();
	//
	//			// not really necessary
	//			if(ent instanceof Player){
	//				Player player = (Player)ent;
	//				Inventory inv = e.getInventory();
	//
	//				// see if the event is about an anvil
	//				if(inv instanceof AnvilInventory){
	//					
	//					InventoryView view = e.getView();
	//					int rawSlot = e.getRawSlot();
	//
	//					// compare the raw slot with the inventory view to make sure we are talking about the upper inventory
	//					if(rawSlot == view.convertSlot(rawSlot)){
	//						/*
	//		slot 0 = left item slot
	//		slot 1 = right item slot
	//		slot 2 = result item slot
	//
	//		see if the player clicked in the result item slot of the anvil inventory
	//						 */
	//						if(rawSlot == 2){
	//							/*
	//		get the current item in the result slot
	//		I think inv.getItem(rawSlot) would be possible too
	//							 */
	//							ItemStack item = e.getCurrentItem();
	//
	//							// check if there is an item in the result slot
	//							if(item != null){
	//								ItemMeta meta = item.getItemMeta();
	//
	//								// it is possible that the item does not have meta data
	//								if(meta != null){
	//									// see whether the item is beeing renamed
	//									if(meta.hasDisplayName()){
	//										String displayName = meta.getDisplayName();
	//
	//										// do something
	//									}
	//								}
	//							}
	//						}
	//					}
	//				}
	//			}
	//		}
	//	}
	//




	@EventHandler
	void onShootBow(EntityShootBowEvent event)
	{
		if (event.getEntity().getType().equals(EntityType.PLAYER))
		{

			Player player = (Player) event.getEntity();

			if(player.getGameMode().equals(GameMode.SURVIVAL))
			{

				ItemStack bow = event.getBow();
				if (Config.DEBUG_MODE) player.sendMessage("DEBUG : ARC en scan");


				if (TypeChecker.isLevellable(bow.getType())
						&& LevelDataManager.hasLevelData(bow))
				{


					LevelData data = haveLevelData(bow, player);
					if (data == null)
						return;

					boolean hasPermission = Permissions.hasStagePermission(player, data.getStage().getName());

					if (hasPermission)
					{
						Bow lvlbow = new Bow(data, player);
						Vector velocity = event.getProjectile().getVelocity();
						Arrow arrow = player.launchProjectile(Arrow.class);
						arrow.setVelocity(velocity);
						arrowStorage.put(arrow, lvlbow);

						if (Config.DEBUG_MODE) player.sendMessage("DEBUG : Fleche tirée avec "+data.getDisplayName()+ChatColor.WHITE+" enregistrée.");
					}
					else
					{
						player.sendMessage(ChatColor.RED + Config.LINES.get("too high level") );
						event.setCancelled(true);
					}


				}

			}
		}
	}


	LevelData haveLevelData(ItemStack itemStack)
	{
		if (LevelDataManager.hasLevelData(itemStack))
		{
			if (itemStack instanceof LevelData)
				try
			{

					return ((LevelData) itemStack);
			}
			catch(Exception e)
			{}
			else
				try
			{

					return LevelData.recoverLevelData(itemStack);
			}
			catch(Exception e){}
		}
		return null;
	}

	LevelData haveLevelData(ItemStack itemStack,Player player)
	{
		if (LevelDataManager.hasLevelData(itemStack))
		{
			if (itemStack instanceof LevelData)
				try
			{
					if (Config.DEBUG_MODE) player.sendMessage("DEBUG: recup leveldata");
					return ((LevelData) itemStack);
			}
			catch(Exception e)
			{}
			else
				try
			{
					if (Config.DEBUG_MODE) player.sendMessage("DEBUG: recup leveldata 2");

					return LevelData.recoverLevelData(itemStack,player);
			}
			catch(Exception e){}
		}
		return null;

	}

	Object[] levelling(Player player, ItemStack itemStack)
	{

		if(!LevelDataManager.hasLevelData(itemStack))
		{

			Object[] resultat = new Object[3];
			resultat[0] = false;
			resultat[1] = false;
			resultat[2] = null;

			return resultat;

		}

		LevelData data = haveLevelData(itemStack, player);
		if (data == null)

		{

			Object[] resultat = new Object[3];
			resultat[0] = false;
			resultat[1] = false;
			resultat[2] = null;

			return resultat;

		}

		if(data.getOwner() == null) data.setOwner(player);
		if (data.getHolder() != player) data.setHolder(player);


		boolean hasPermission = Permissions.hasStagePermission(player, data.getStage().getName());

		if (hasPermission)
		{
			/*
			 * Verification si le prochain level est accessible!
			 * 
			 */
			boolean xpgain = true;

			if (data.getExperienceBeforeLvlUp() <= Config.EXP_PER_HIT){
				/*
				 * Si le level est juste en dessous du level max 
				 *  et que le joueur peut atteindre ce level
				 *  
				 */
				if(data.getLevel() == Config.MAX_LEVEL -1)
					if (!Permissions.hasPermission(player, "maxlevel") ) {

						player.sendMessage(data.getDisplayName()+ChatColor.WHITE+ Config.LINES.get("no longer gain xp") +ChatColor.RED+ Config.LINES.get("no permission max level"));
						xpgain = false;
					}




				/*
				 * Si le level est au niveau max 
				 * 
				 * 
				 */
				if(data.getLevel() >= Config.MAX_LEVEL)
					if(!Permissions.hasPermission(player, "maxlevel.override"))
					{
						xpgain = false;
						player.sendMessage(data.getDisplayName()+ChatColor.WHITE+ Config.LINES.get("no longer gain xp") +ChatColor.GOLD+ Config.LINES.get("max level reached"));

					}


				Stage dataStage = data.getStage();
				/* Si le level n'est pas juste en dessous 
				 * mais qu'il risque de faire arriver un stage 
				 * dont il as ou pas la permission 
				 * 
				 * stage actuel, il a la perm.
				 * stage suivant? 
				 *   si oui verif si c'est au passage de ce level.
				 *     si oui verif s'il a la permission de ce stage suivant.
				 * 
				 * */
				if(dataStage.nextStageExist())
					if(data.getLevel() == dataStage.getNextStageLevel() -1)
						if (!Permissions.hasStagePermission(player, dataStage.getNextStageName()))
						{
							xpgain = false;

							player.sendMessage(data.getDisplayName()+ChatColor.WHITE+ Config.LINES.get("no longer gain xp") +ChatColor.RED+ Config.LINES.get("no permission next stage") +dataStage.getNextStageColor()+dataStage.getNextStageName()+ChatColor.RED+")");


						}


			}

			Object[] resultat = new Object[3];
			resultat[0] = hasPermission;
			resultat[1] = xpgain;
			resultat[2] = data;

			return resultat;


		}

		player.sendMessage(ChatColor.RED+Config.LINES.get("no permission use stage")+ChatColor.GOLD+data.getStage().getName());

		Object[] resultat = new Object[3];
		resultat[0] = hasPermission;
		resultat[1] = false;
		resultat[2] = data;

		return resultat;

	}


}
