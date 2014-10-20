package helldragger.RPSWeapons;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

 class LevelEnchantment
{
	/**
	 * @uml.property  name="enchantment"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private Enchantment enchantment;
	/**
	 * @uml.property  name="level"
	 */
	private int level;
	
	 LevelEnchantment(Enchantment enchantment, int level)
	{
		this.enchantment = enchantment;
		this.level = level;
	}
	
	@SuppressWarnings("deprecation")
	 LevelEnchantment(int id, int level)
	{
		this.enchantment = Enchantment.getById(id);
		this.level = level;
	}
	
	 void apply(ItemStack item)
	{
		item.addUnsafeEnchantment(enchantment, level);
	}
	
	 int getLevel(){
		return this.level;
	}
	
	 void setLevel(int level)
	{
		this.level = level;
	}
	
	 Enchantment getEnchantment()
	{
		return this.enchantment;
	}
	
	 void setEnchantment(Enchantment enchant)
	{
		this.enchantment = enchant;
	}
}