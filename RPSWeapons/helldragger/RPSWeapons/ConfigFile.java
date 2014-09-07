package helldragger.RPSWeapons;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;

 class ConfigFile extends YamlConfiguration
{

	private String name;
	
	private File file;
	private String fileName;
	private String filePath;
	
	 ConfigFile(String name)
	{
		this.name = name;
	}
	
	 ConfigFile(String name ,File file){
		this.name = name;
		this.file = file;
		this.fileName = file.getName();
		
	}
	

	 public String getName()
	{
		return name;
	}
	
	 File getFile(){
		return file;
	}
	
	 String getFileName(){
		return fileName;
	}
	
	 String getPath(){
		return filePath;
	}
	
	 void setPath(String path){
		this.file = new File(path,this.fileName);
		this.filePath = path;
		
	}
	
	 void setPath(String path, String fileName){
		this.file = new File(path,fileName);
		this.filePath = path;
		this.fileName = fileName;
		
	}
	
	 void setFile(File file){
		this.file = file;
		this.fileName = file.getName();
		this.filePath = file.getPath();
	}
	
	 void save() throws IOException{
		super.save(this.file);
	}
	
}
