package helldragger.RPSWeapons;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


class Commands implements CommandExecutor
{
	/**
	 * @uml.property  name="plugin"
	 * @uml.associationEnd  multiplicity="(1 1)" inverse="cmdListener:RolePlaySpecialityWeapons.RPSWPlugin"
	 */
	private RPSWPlugin plugin;

	Commands(RPSWPlugin rpsPlugin)
	{
		this.plugin = rpsPlugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args)
	{

		if (label.equalsIgnoreCase("rpw"))
		{
			if (args.length == 0)
			{
				sendHelp(sender);
			} 
			else
			{
				if (args[0].equalsIgnoreCase("version"))
				{
					sender.sendMessage(ChatColor.GRAY + "This server is running RolePlaySpeciality Weapons version " + plugin.pdf.getVersion());
					return true;
				} 
				else if (args[0].equalsIgnoreCase("reload"))
				{
					if (sender instanceof Player)
					{
						if (!Permissions.hasPermission((Player) sender, "reload"))
						{
							sender.sendMessage(ChatColor.RED + Config.LINES.get("command no permission"));
							return true;
						}
					}

					plugin.onReload();
					sender.sendMessage(ChatColor.GRAY + "Reloaded RolePlaySpeciality Weapons.");
					return true;
				}
				else if (args[0].equalsIgnoreCase("setlevel"))
				{
					if (sender instanceof Player)
					{
						if (!Permissions.hasPermission((Player) sender, "setlevel"))
						{
							sender.sendMessage(ChatColor.RED + Config.LINES.get("command no permission"));
							return true;
						}

						if (args.length < 2)
						{
							sender.sendMessage(ChatColor.RED + Config.LINES.get("must specify level"));
							sender.sendMessage(ChatColor.GRAY + "/wl setlevel <level> - Sets the level of the item in hand.");
							return true;
						}

						if (args[1].length() > 7)
						{
							sender.sendMessage(ChatColor.RED + Config.LINES.get("too high number"));
						}

						ItemStack itemStack = ((Player) sender).getItemInHand();

						if (itemStack == null || itemStack.getType() == Material.AIR || !(TypeChecker.isLevellable(itemStack.getType())))
						{
							sender.sendMessage(ChatColor.RED + Config.LINES.get("is not levellable"));
							return true;
						}

						LevelData item = new LevelData(itemStack);
						item.setLevel(Integer.valueOf(args[1]));

						sender.sendMessage(ChatColor.GRAY + "Set item level to " + args[1] + ".");
						return true;
					}
					else
					{
						sender.sendMessage(ChatColor.RED +  Config.LINES.get("command must be in game"));
						return true;
					}

				}
				else if(args[0].equalsIgnoreCase("debug") && Config.DEBUG_MODE){
					/*
					 * 
					 * PlayerItems a afficher
					 * 
					 */

					sender.sendMessage("---------DEBUG COMMAND USED---------");


					sender.sendMessage("---------Stage list---------");
					
					List<Stage> stagelist = new ArrayList<Stage>();
					stagelist.addAll(StageManager.getStages());
					
					for(Stage stage : stagelist)
					{
						sender.sendMessage(" - "+stage.getColor()+stage.getName()+ChatColor.WHITE+" at level "+stage.getLevel());

						String[] modifsnom  = stage.getNameModifications();
						sender.sendMessage("  - prefix: "+modifsnom[0]+" suffix: "+modifsnom[1]);

						sender.sendMessage("  - BONUSES: armor("+stage.getBonus("armor")+") damage("+stage.getBonus("damage")+") fishing("+stage.getBonus("fishing")+")");


					}

					sender.sendMessage("---------------------");

				}
			}


			return true;
		}
		else if (label.equalsIgnoreCase("level"))
		{

			if (sender instanceof Player)
			{
				Player player = (Player) sender;

				if (args.length == 0)
				{

					if(Permissions.hasPermission(player, "level"))
					{
						ItemStack item = player.getItemInHand();

						if(item != null)
						{
							if(TypeChecker.isLevellable(item.getType()))
							{
								if(!LevelDataManager.hasLevelData(item))
								{
									LevelData data = new LevelData(item, player);
									player.setItemInHand(data.getItemStack());
									if (item.hasItemMeta())
									{
										if(item.getItemMeta().hasDisplayName())
										{
											player.sendMessage(item.getItemMeta().getDisplayName()+ChatColor.GREEN+ Config.LINES.get("is now levelled"));
										}
									}
									else
										player.sendMessage(item.getType().name()+ChatColor.GREEN+ Config.LINES.get("is now levelled"));

								}
								else
									player.sendMessage(item.getType().name()+ChatColor.RED+ Config.LINES.get("is already levelled"));
							}
							else
								player.sendMessage(ChatColor.WHITE+item.getType().name()+ChatColor.RED+ Config.LINES.get("is not levellable"));
						}
						else
							player.sendMessage(ChatColor.RED+Config.LINES.get("nothing in hand"));


					}
					else
						player.sendMessage(ChatColor.RED+Config.LINES.get("command no permission"));

				}
				else if (args[0].equalsIgnoreCase("remove"))
				{
					if(Permissions.hasPermission(player, "level.remove"))
					{
						ItemStack item = player.getItemInHand();

						if(item != null)
						{
							if(TypeChecker.isLevellable(item.getType()))
							{
								if(LevelDataManager.hasLevelData(item))
								{

									player.setItemInHand(new ItemStack(item.getType(), item.getDurability()));
									if (item.hasItemMeta())
									{
										if(item.getItemMeta().hasDisplayName())
										{
											player.sendMessage(item.getItemMeta().getDisplayName()+ChatColor.GREEN+ Config.LINES.get("is now normal"));
										}
									}
									else
										player.sendMessage(item.getType().name()+ChatColor.GREEN+ Config.LINES.get("is now normal"));

								}
								else
									player.sendMessage(item.getType().name()+ChatColor.RED+ Config.LINES.get("is already normal"));
							}
							else
								player.sendMessage(ChatColor.WHITE+item.getType().name()+ChatColor.RED+ Config.LINES.get("is already normal"));
						}
						else
							player.sendMessage(ChatColor.RED+ Config.LINES.get("nothing in hand"));


					}
					else
						player.sendMessage(ChatColor.RED+Config.LINES.get("command no permission"));

				}
				else if (args[0].equalsIgnoreCase("buy"))
				{

					if(Permissions.hasPermission(player, "level.buy"))
					{
						ItemStack item = player.getItemInHand();

						if(item != null)
						{
							if(TypeChecker.isLevellable(item.getType()))
							{
								if(LevelDataManager.hasLevelData(item))
								{

									int count = 0;
									if(args.length >= 2)
									{
										try
										{
											count = Integer.parseInt(args[1]);
										}
										catch(Exception e)
										{

											player.sendMessage(ChatColor.RED+Config.LINES.get("use command example")+" /level buy <number> || /level buy");
											return false;
										}

									}else
										count = 1;

									for(int i = 0; i != count; i++)
									{
										LevelData data = LevelDataManager.getLevelData(item);
										double cost = ((data.getExperienceBeforeLvlUp() / Config.XP_LEFT_DIVIDER)*Config.XP_COST_MULTIPLIER);



										boolean transfertok = false;

										if (! buyLevels(player, data))
											return false;

										if (RPSWPlugin.economy.bankWithdraw(player.getDisplayName(), cost ).transactionSuccess())
										{//Si le joueur a payé assez d'argent pour level up:


											player.sendMessage(ChatColor.GOLD
													+Config.LINES.get("you just paid")
													+ cost
													+RPSWPlugin.economy.currencyNamePlural()
													+Config.LINES.get("to buy this level"));
											data.addExperience(player, data.getExperienceBeforeLvlUp());
											data.update();
											transfertok = true;
										}
										if (!transfertok)
										{
											if(RPSWPlugin.economy.withdrawPlayer(player.getName(), cost).transactionSuccess())
											{


												player.sendMessage(ChatColor.GOLD
														+Config.LINES.get("you just paid")
														+ cost
														+RPSWPlugin.economy.currencyNamePlural()
														+Config.LINES.get("to buy this level"));
												data.addExperience(player, data.getExperienceBeforeLvlUp());
												data.update();
												transfertok = true;
											}
										}

										if(!transfertok)
										{

											player.sendMessage(ChatColor.RED
													+ Config.LINES.get("not enough money you need")
													+ChatColor.GOLD
													+((data.getExperienceBeforeLvlUp() / Config.XP_LEFT_DIVIDER)*Config.XP_COST_MULTIPLIER)
													+" "
													+RPSWPlugin.economy.currencyNamePlural());
											return false;

										}
									}


								}
								else
									player.sendMessage(item.getType().name()+ChatColor.RED+ Config.LINES.get("is not levelled"));
							}
							else
								player.sendMessage(ChatColor.WHITE+item.getType().name()+ChatColor.RED+ Config.LINES.get("is not levellable"));
						}
						else
							player.sendMessage(ChatColor.RED+ Config.LINES.get("nothing in hand"));


					}
					else
						player.sendMessage(ChatColor.RED+Config.LINES.get("command no permission"));

				}

				return true;
			}
			else
				sender.sendMessage( Config.LINES.get("command must be in game"));

		}

		return false;
	}


	void sendHelp(CommandSender sender)
	{
		if (sender instanceof Player)
		{

			sender.sendMessage(ChatColor.GRAY + "/rpw reload - Reloads plugin configuration.");
			sender.sendMessage(ChatColor.GRAY + "/rpw setlevel <level> - Sets the level of the item in hand.");
			sender.sendMessage(ChatColor.GRAY + "/rpw version - Displays plugin version information.");
			if (Config.DEBUG_MODE) sender.sendMessage(ChatColor.GRAY + "/rpw debug - Displays some debug info.");
			sender.sendMessage(ChatColor.GRAY + "/level - transform item in hand into levelled item if it's possible");
			sender.sendMessage(ChatColor.GRAY + "/level buy  - pay xp left before levelling up on item in hand ");
			sender.sendMessage(ChatColor.GRAY + "/level remove - restore an levelled item to a normal item");


		}
		else
		{
			sender.sendMessage(ChatColor.GRAY + "/rpw reload - Reloads plugin configuration.");
			sender.sendMessage(ChatColor.GRAY + "/rpw setlevel <level> - Sets the level of the item in hand.");
			sender.sendMessage(ChatColor.GRAY + "/rpw version - Displays plugin version information.");
		}
	}

	boolean buyLevels(Player player, LevelData data)
	{
		Object[] results = plugin.events.levelling(player, data.getItemStack());

		return (boolean) results[1];

	}

}
