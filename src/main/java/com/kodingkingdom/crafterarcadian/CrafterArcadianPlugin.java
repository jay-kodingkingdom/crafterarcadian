package com.kodingkingdom.crafterarcadian;
import java.util.logging.Level;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class CrafterArcadianPlugin extends JavaPlugin implements Listener{
		
	CrafterArcadian craftArcadian;
	
	@Override
    public void onEnable(){
		craftArcadian=new CrafterArcadian(this);
    	craftArcadian.loadConfig();} 
    @Override
    public void onDisable(){craftArcadian.saveConfig();}
    
    static CrafterArcadianPlugin instance=null;
    public CrafterArcadianPlugin(){instance=this;}
    public static CrafterArcadianPlugin getPlugin(){
    	return instance;}
    public static void say(String msg){
    	instance.getLogger().log(Level.INFO
    			, msg);}
    public static void debug(String msg){
    	instance.getLogger().log(Level.FINE
    			, msg);}}