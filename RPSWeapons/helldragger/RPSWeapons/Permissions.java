package helldragger.RPSWeapons;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.CalculableType;

class Permissions
{
	static boolean BPERMISSIONS = false;
	//TODO faire l'ajout des perms des stages avec des childrens pour que chaque stage accessible donne acces aux anterieurs

	static void loadPerms(){
		List<String> stageNames = StageManager.getStagesNames();
		for(String name: stageNames)

			Bukkit.getPluginManager().addPermission(new Permission("roleplayspecialityweapons.stage."+name.toLowerCase().replace(" ", "_")));

	}
	/*I) charger les permissions customs
	 * 1. recuperer celles des stages
	 * 2. les ajouter a customPerms
	 * 
	 * II) chercher des permissions pour un joueur
	 * 1. voir s'il c'est une perm custom ou non
	 * 2 a. si oui verif si il a cette permission dans sa config
	 * 3 a. 
	 * 
	 * 2 b. si non, laisser faire bukkit. 
	 */




	static boolean hasPermission(Player player, String node)
	{
		if(Config.USE_PERMS){

			for(String name: StageManager.getStagesNames())
				if(node.contains(name.toLowerCase()))
					return hasStagePermission(player, name.toLowerCase());

			node = "roleplayspecialityweapons." + node.toLowerCase();
			if (Bukkit.getPluginManager().getPermission(node) == null){
				player.sendMessage(ChatColor.DARK_RED+"La permission necessaire à cette action n'existe pas, veuillez reporter cette erreur a l'administrateur.");
				RPSWPlugin.log.warning("Permission demandée inconnue("+node+")!!");
				return false;
			}
			if (BPERMISSIONS)
			{
				return hasBPermission(player, node);
			}
			else
			{
				return player.hasPermission(node);
			}
		}else{
			return true;
		}
	}

	static boolean hasStagePermission(Player player, String stagename)
	{
		if(Config.USE_PERMS){
			stagename = "roleplayspecialityweapons.stage." + stagename.toLowerCase();
			if (Bukkit.getPluginManager().getPermission(stagename) == null){
				player.sendMessage(ChatColor.DARK_RED+"La permission necessaire à cette action n'existe pas, veuillez reporter cette erreur a l'administrateur.");
				RPSWPlugin.log.warning("Permission demandée inconnue!!("+stagename+")");
				return false;
			}
			if (BPERMISSIONS)
			{
				return hasBPermission(player, stagename);
			}
			else
			{
				return player.hasPermission(stagename);
			}
		}else{
			return true;
		}
	}


	private static boolean hasBPermission(Player player, String node )
	{
		return ApiLayer.hasPermission(player.getWorld().getName(), CalculableType.USER, player.getName(), node);
	}
}

