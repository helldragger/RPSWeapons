package helldragger.RPSWeapons;

 enum ToolType
{
	PICKAXE,
	AXE,
	SPADE,
	HOE,
	SHEARS,
	SWORD,
	BOW;
	
	 static ToolType getByItemName(String string)
	{
		
		String[] temp = string.split("_");
		if (temp.length > 1)
			if (temp[1].equalsIgnoreCase("HOE")
					|| temp[1].equalsIgnoreCase("SPADE")
					|| temp[1].equalsIgnoreCase("PICKAXE")
					|| temp[1].equalsIgnoreCase("AXE")
					|| temp[1].equalsIgnoreCase("SWORD"))
				return ToolType.valueOf(string.split("_")[1]);
			else
				return null;
		else if (temp[0].equalsIgnoreCase("BOW")
				|| temp[0].equalsIgnoreCase("SHEARS"))
			return ToolType.BOW;
		else
			return null;
	}
}
