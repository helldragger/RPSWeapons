package helldragger.RPSWeapons;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

 class Bow{
	
	private LevelData lvldata;
	private Player shooter;
	
	Bow( LevelData leveldata, Player player )
	{
		
		this.lvldata = leveldata;
		this.shooter = player;
		
	}
	
	Bow( LevelData leveldata)
	{
		
		this.lvldata = leveldata;
		this.shooter = leveldata.getHolder();
		
	}
	
	
	 ItemStack getBow(){
		return this.lvldata.getItemStack();
	}
	
	 Player getShooter(){
		return shooter;
	}
	
	 LevelData getLevelData(){
		return lvldata;
	}
	
	 Bow getBowObject(){
		return this;
	}
}
