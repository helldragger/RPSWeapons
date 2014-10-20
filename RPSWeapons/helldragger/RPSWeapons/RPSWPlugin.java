package helldragger.RPSWeapons;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import net.gravitydevelopment.updater.Updater;
import net.gravitydevelopment.updater.Updater.UpdateResult;
import net.gravitydevelopment.updater.Updater.UpdateType;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;




public class RPSWPlugin extends JavaPlugin
{



	/**
	 * @uml.property  name="pm"
	 * @uml.associationEnd  
	 */
	PluginManager pm;
	/**
	 * @uml.property  name="pdf"
	 * @uml.associationEnd  
	 */
	PluginDescriptionFile pdf;
	static Logger log;

	/**
	 * @uml.property  name="events"
	 * @uml.associationEnd  inverse="plugin:RolePlaySpecialityWeapons.Events"
	 */
	Events events;
	/**
	 * @uml.property  name="cmdListener"
	 * @uml.associationEnd  inverse="plugin:RolePlaySpecialityWeapons.Commands"
	 */
	Commands cmdListener;

	public static Economy economy = null;



	@Override
	public void onEnable()
	{
		pm = getServer().getPluginManager();
		pdf = getDescription();
		log = Logger.getLogger("Minecraft");

		events = new Events(this);
		cmdListener = new Commands(this);

		pm.registerEvents(this.events, this);

		getCommand("rpw").setExecutor(this.cmdListener);
		getCommand("level").setExecutor(this.cmdListener);




		Permissions.BPERMISSIONS = pm.getPlugin("bPermissions") != null;

		try
		{
			loadData();
		}
		catch (IOException | InvalidConfigurationException e)
		{
			e.printStackTrace();
		}


		log.info("RolePlayspeciality Weapons v" + pdf.getVersion() + " by " + pdf.getAuthors() + " is now enabled!");
	}




	@Override
	public void onDisable()
	{


		log.info("RolePlayspeciality Weapons v" + pdf.getVersion() + " by " + pdf.getAuthors() + " is now disabled.");
	}




	public void onReload()
	{
		log.info("Reloading RolePlaySpeciality Weapons v" + pdf.getVersion() +"...");

		log.info("Loading plugin data...");
		try
		{
			loadData();
		}
		catch (IOException | InvalidConfigurationException e)
		{
			e.printStackTrace();
		}

		log.info("Loading item data...");

		TypeChecker.loadItems(this);

		log.info("RolePlaySpeciality Weapons has successfully reloaded.");
	}


	private boolean setupEconomy()
	 
    {
 
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
 
        if (economyProvider != null) {
 
            economy = economyProvider.getProvider();
 
        }
 
        return (economy != null);
 
    }

	private void loadData() throws IOException, InvalidConfigurationException
	{
		File folder = getDataFolder();

		if (!folder.exists())
		{
			folder.mkdir();
		}
		else
		{
			File oldConfigFile = new File(folder.getPath() + File.separator + "config.yml");

			if (oldConfigFile.exists())
			{
				oldConfigFile.delete();
			}

		}

		String dataPath = folder.getPath() + File.separator;

		File configFolder = new File(dataPath + "configuration");

		if (!configFolder.exists())
		{
			configFolder.mkdir();
		}

		Config.removeOldData(this);
		Config.loadConfig(this, Config.CONFIG,false);
		Config.loadConfig(this, Config.STAGES,true);
		Config.loadConfig(this, Config.GROUPS,false);
		Config.loadConfig(this, Config.ITEMS,false);
		Config.loadConfig(this, Config.LANG,false);
		Config.loadConfigValues(this);

		TypeChecker.loadTypeChecker(this);

		Config.loadConfigValues(this);


		StageManager.loadStages();

		setupEconomy();
		
		Permissions.loadPerms();
		
		if(Config.USE_CRAFTING_MODE) 
			loadRecipes();
		
		if(Config.USE_AUTO_UPDATE){
			Updater updater = null;
			
			if(Config.DOWNLOAD_UPDATE)
				updater = new Updater(this, 77657, this.getFile(), UpdateType.DEFAULT, true);
			else
				updater = new Updater(this, 77657, this.getFile(), UpdateType.NO_DOWNLOAD, true);
			
			if (updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
			    this.getLogger().info("New version available! " + updater.getLatestName());
			    
			}else if(updater.getResult() == UpdateResult.SUCCESS)
				this.getLogger().info("New version downloaded! RPSW has updated to " + updater.getLatestName());
			
		}

	}

	void loadRecipes()
	{
		List<Material> failedChanges = new ArrayList<Material>();

		for(Material material : Material.values())
		{
			if(TypeChecker.isLevellable(material))
			{
				//recuperation de l'item de la liste des levellables
				String itemName = material.name();
				ItemStack itemStack = new ItemStack( Material.getMaterial( itemName.toUpperCase().replace(" ", "_") ),1 );

				LevelData data = new LevelData(itemStack);
				data.update();
				
				if (data.hasItemMeta())
					itemStack.setItemMeta(data.getItemMeta());
				//ajout de la recette shaped qui contient l'item + une emeraude + un bouquin ecrivable
				try 
				{
					
					ShapedRecipe newRecipe = new ShapedRecipe(itemStack);
					newRecipe.shape(" B ", " E ", " & ").setIngredient('&', itemStack.getType()).setIngredient( 'E', Material.EMERALD).setIngredient( 'B', Material.BOOK_AND_QUILL);
					
					Bukkit.addRecipe(newRecipe);
					log.info("item"+ itemStack.getType().name()+ "successfully changed.");
				}
				catch(Exception e2)
				{
					failedChanges.add(itemStack.getType());
					log.warning(itemName+" recipe cannot be added in shapedrecipe form");
					log.severe("reason:"+ e2.getLocalizedMessage());
					e2.printStackTrace();
				}
			}


		}
		if (!failedChanges.isEmpty())
		{	
			log.severe("these items has encountered problems while changing their recipes: ");
			Iterator<Material> it = failedChanges.iterator();
			while (it.hasNext())
			{
				Material itMaterial = it.next();
				log.severe("  - "+ itMaterial.name());
			}
		}
	}

	public void removeRecipe(Recipe inputRecipe){

		Iterator<Recipe> it = getServer().recipeIterator();
		Recipe recipe;
		while(it.hasNext())
		{
			recipe = it.next();
			if (recipe != null && recipe.getResult().getType() == inputRecipe.getResult().getType())
			{
				it.remove();
				log.info("recipe of "+recipe.getResult()+" successfully removed.");
			}
		}
	}

	static void copyFileUsingStream(File source, File dest) throws IOException {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(source);
			os = new FileOutputStream(dest);
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


}

