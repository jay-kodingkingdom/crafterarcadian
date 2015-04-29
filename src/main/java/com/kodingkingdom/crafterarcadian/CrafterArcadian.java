package com.kodingkingdom.crafterarcadian;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import com.kodingkingdom.craftercoordinator.CrafterCoordinator;
import com.kodingkingdom.craftercoordinator.CrafterCoordinatorPlugin;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class CrafterArcadian implements Listener, CommandExecutor{	
		
	CrafterArcadianPlugin plugin;
	CrafterArcadia crafterArcadia=null;
	HashSet<UUID> arcadianIds;

	CrafterArcadian(CrafterArcadianPlugin Plugin){
		plugin=Plugin;
		arcadianIds=new HashSet<UUID>();
		plugin.getCommand("obtainarcadia").setExecutor(this);
		plugin.getCommand("releasearcadia").setExecutor(this);
		plugin.getCommand("listarcadian").setExecutor(this);
		plugin.getCommand("addarcadian").setExecutor(this);
		plugin.getCommand("removearcadian").setExecutor(this);}

	@Override
	public boolean onCommand(final CommandSender sender, Command command,
			String label, final String[] args) {
		
		if (sender instanceof Player){
			if (!(coordinator.checkPlayerLimit(((Player)sender).getUniqueId()))){
				sender.sendMessage("ACCESS DENIED: No permission");
				return false;}
			if (args.length==2 && label.equalsIgnoreCase("obtainarcadia") && (args[1].equalsIgnoreCase("true")||args[1].equalsIgnoreCase("false"))){
				(new BukkitRunnable(){public void run(){
					try{
						crafterArcadia=CrafterArcadia.obtainArcadia(args[0], args[1].equalsIgnoreCase("true"), ((Player)sender), CrafterArcadian.this);
						if (crafterArcadia==null) sender.sendMessage("Cannot obtain arcadia!");
						else sender.sendMessage("Arcadia "+args[0]+(args[1].equalsIgnoreCase("true")?" with allow":" with disallow")+" obtained at plot "+crafterArcadia.modelId);}
					catch(Exception e){
						sender.sendMessage("Exception "+e.toString()+" was thrown!");}}}).runTaskAsynchronously(plugin);
				return true;}
			if (args.length==0 && label.equalsIgnoreCase("releasearcadia")){
				if (crafterArcadia==null) sender.sendMessage("You must obtain arcadia before release!");
				else {
					(new BukkitRunnable(){public void run(){
						try{
							crafterArcadia.release((Player)sender);
							sender.sendMessage("Arcadia "+crafterArcadia.name+(crafterArcadia.allowed?" with allow":" with disallow")+" released");}
						catch(Exception e){
							sender.sendMessage("Exception "+e.toString()+" was thrown!");}}}).runTaskAsynchronously(plugin);}
				return true;}}
		if (args.length==0 && label.equalsIgnoreCase("listarcadian")){
			(new BukkitRunnable(){public void run(){
				sender.sendMessage("Arcadians:");
				for (UUID playerId : arcadianIds){
					try {
						String playerName = CrafterWrapper.getPlayerName(playerId);
						sender.sendMessage(playerName==null?"Unknown!":playerName);}
					catch(Exception e){
						sender.sendMessage("Exception "+e.toString()+" was thrown!");}}
				sender.sendMessage("---------");}}).runTaskAsynchronously(plugin);
			return true;}
		if (args.length==1 && label.equalsIgnoreCase("addarcadian")){
			(new BukkitRunnable(){public void run(){
				try{
					arcadianIds.add(CrafterWrapper.getPlayerId(args[0]));
					sender.sendMessage("Arcadian "+args[0]+" added");}
				catch(Exception e){
					sender.sendMessage("Exception "+e.toString()+" was thrown!");}}}).runTaskAsynchronously(plugin);
			return true;}
		if (args.length==1 && label.equalsIgnoreCase("removearcadian")){
			(new BukkitRunnable(){public void run(){
				try{
					arcadianIds.remove(CrafterWrapper.getPlayerId(args[0]));
					sender.sendMessage("Arcadian "+args[0]+" removed");}
				catch(Exception e){
					sender.sendMessage("Exception "+e.toString()+" was thrown!");}}}).runTaskAsynchronously(plugin);
			return true;}
		sender.sendMessage("Command not understood!");
		return false;}
	
	public void loadConfig(){
		CrafterArcadianPlugin plugin = CrafterArcadianPlugin.getPlugin();
		FileConfiguration config = plugin.getConfig();
		
		try{
			for (String idString : config.getStringList(CrafterArcadianConfig.ARCADIAN.config)){
				arcadianIds.add(UUID.fromString(idString));}
			
			plugin.getLogger().info("Config successfully loaded");}
		
		catch(Exception e){
			plugin.getLogger().severe("Could not load config!");
			plugin.getLogger().severe("ERROR MESSAGE: "+e.getMessage());
			e.printStackTrace();
			config.set("crafterarcadian.ERROR", true);}}			
	
	public void saveConfig(){
		CrafterArcadianPlugin plugin = CrafterArcadianPlugin.getPlugin();
		FileConfiguration config = plugin.getConfig();

		if (config.isSet("crafterarcadian.ERROR")){
			plugin.getLogger().info("Config state invalid, will not save");
			return;}
		try{
			for(String key : config.getKeys(false)){
				 config.set(key,null);}
			
			ArrayList<String> arcadianIdStrings=new ArrayList<String>();
			for (UUID arcadianId: arcadianIds){
				arcadianIdStrings.add(arcadianId.toString());}
			config.set(CrafterArcadianConfig.ARCADIAN.config,arcadianIdStrings);		

			plugin.saveConfig();
			plugin.getLogger().info("Config successfully saved");}
		catch(Exception e){
			plugin.getLogger().severe("Could not save config!");
			plugin.getLogger().severe("ERROR MESSAGE: "+e.getMessage());
			e.printStackTrace();}}
	
	CrafterCoordinator coordinator = CrafterCoordinatorPlugin.getPlugin().getCoordinator();
	WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
		
	public enum CrafterArcadianConfig {
		ARCADIAN("crafterarcadian.arcadian");		
		public final String config;
		
		private CrafterArcadianConfig(String Config){
			config=Config;}}}
