package com.kodingkingdom.crafterarcadian;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.worldcretornica.plotme.utils.NameFetcher;
import com.worldcretornica.plotme.utils.UUIDFetcher;


public class CrafterWrapper {
	private static final long bufferTime = 2*1000;
	private static ReentrantLock lock = new ReentrantLock(); 
	public static UUID getPlayerId(String playerName){		
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);

        if (player.getUniqueId() != null) {
            return player.getUniqueId();}
        else {
        	
    		lock.lock();
    		try {
    			Thread.sleep(bufferTime);}
    		catch (InterruptedException e) {}
    		finally{
    			lock.unlock();}
            List<String> names = new ArrayList<String>();
            names.add(playerName);
            UUIDFetcher fetcher = new UUIDFetcher(names);

            try {
                CrafterArcadianPlugin.say("Fetching " + playerName + " UUID from Mojang servers...");
                Map<String, UUID> response = fetcher.call();                    
                if (response.size() > 0) {
                	UUID playerId = response.values().toArray(new UUID[0])[0];
                	CrafterArcadianPlugin.say("Fetched " + playerId.toString() + "for" + playerName);
                    return playerId;}}
            catch (IOException e) {
            	CrafterArcadianPlugin.say("Unable to connect to Mojang server!");
            	if (e.getMessage()!=null&&e.getMessage().contains("HTTP response code: 429")){
            		CrafterArcadianPlugin.say("HTTP response code 429");
            		CrafterArcadianPlugin.say("Retrying...");
            		return getPlayerId(playerName);}} 
            catch (Exception e) {
            	CrafterArcadianPlugin.say("Exception while running UUIDFetcher");
                e.printStackTrace();}}
        return null;}
	public static String getPlayerName(UUID playerId){			
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);

        if (player.getName() != null) {
            return player.getName();}
        else {
    		lock.lock();
    		try {
    			Thread.sleep(bufferTime);}
    		catch (InterruptedException e) {}
    		finally{
    			lock.unlock();}
            List<UUID> names = new ArrayList<UUID>();
            names.add(playerId);
            NameFetcher fetcher = new NameFetcher(names);

            try {
            	CrafterArcadianPlugin.say("Fetching " + playerId.toString() + " Name from Mojang servers...");
                Map<UUID, String> response = fetcher.call();
                
                if (response.size() > 0) {
                	String playerName = response.values().toArray(new String[0])[0];
                	CrafterArcadianPlugin.say("Fetched " + playerName + "for" + playerId.toString());
                	return playerName;}}
            catch (IOException e) {
            	CrafterArcadianPlugin.say("Unable to connect to Mojang server!");
            	if (e.getMessage()!=null&&e.getMessage().contains("HTTP response code: 429")){
            		CrafterArcadianPlugin.say("HTTP response code 429");
            		CrafterArcadianPlugin.say("Retrying...");
            		return getPlayerName(playerId);}} 
            catch (Exception e) {
            	CrafterArcadianPlugin.say("Exception while running NameFetcher");
                e.printStackTrace();}}
		return null;}}