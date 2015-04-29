package com.kodingkingdom.crafterarcadian;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldedit.bukkit.selections.Selection;
import com.worldcretornica.plotme.Plot;
import com.worldcretornica.plotme.PlotManager;
import com.worldcretornica.plotme.PlotMapInfo;
import com.worldcretornica.plotme.SqlManager;

public class CrafterArcadia{	
	
	private CrafterArcadian crafterArcadian;
	private CrafterArcadia self;
	private ReentrantLock lock=new ReentrantLock();
	
	String name;
	String modelId;
	String worldName;
	boolean allowed;
	
	public static CrafterArcadia obtainArcadia(String Name,boolean Allowed,Player player, CrafterArcadian CrafterArcadian){
		if (!PlotManager.isPlotWorld(player.getWorld())){
			throw new IllegalStateException("This is not a plot world!");}
		PlotMapInfo pmi = PlotManager.getMap(player.getWorld());
		int measureSize = pmi.PlotSize + pmi.PathWidth;
		int x = (int) Math.ceil((double)(player.getLocation().getBlockX()) / measureSize);
		int z = (int) Math.ceil((double)(player.getLocation().getBlockZ()) / measureSize);
		String id=x+";"+z;
		if (PlotManager.isPlotAvailable(id, player)){
			Plot plot = new Plot(Name, PlotManager.getPlotTopLoc(player.getWorld(), id), PlotManager.getPlotBottomLoc(player.getWorld(), id), x+";"+z, PlotManager.getMap(player.getWorld()).DaysToExpiration);
			PlotManager.getPlots(player.getWorld()).put(id, plot);
			SqlManager.addPlot(plot, x, z, player.getWorld());
			return new CrafterArcadia(Name,id,player.getWorld().getName(),Allowed,CrafterArcadian);}
		else return null;}
	public void release(Player player){
		lock.lock();try{
			if (self==null) throw new RuntimeException("Arcadia has already been released!");			

			Selection selection = crafterArcadian.worldEdit.getSelection(player);			
			if (selection==null) {
				throw new NullPointerException("You have not made a selection!");}
			else if (!PlotManager.isPlotWorld(selection.getWorld())){
				throw new IllegalStateException("This is not a plot world!");}
			PlotMapInfo pmi = PlotManager.getMap(selection.getWorld());
			int measureSize = pmi.PlotSize + pmi.PathWidth;
			
			final int minX = (int) Math.ceil((double)(selection.getMinimumPoint().getBlockX()) / measureSize);
			final int minZ = (int) Math.ceil((double)(selection.getMinimumPoint().getBlockZ()) / measureSize);
			final int maxX = (int) Math.ceil((double)(selection.getMaximumPoint().getBlockX()) / measureSize);
			final int maxZ = (int) Math.ceil((double)(selection.getMaximumPoint().getBlockZ()) / measureSize);

			if ((maxX-minX+1)*(maxZ-minZ+1)<crafterArcadian.arcadianIds.size()) throw new IndexOutOfBoundsException("Insufficient plots for Arcadians!");
			
			HashSet<String> unavailablePlots = new HashSet<String>();
			for (int x=minX;x<=maxX;x++){
				for (int z=minZ;z<=maxZ;z++){
					if (!PlotManager.isPlotAvailable(x+";"+z, player)) {
						unavailablePlots.add(x+";"+z);}}}
			if (!unavailablePlots.isEmpty()){
				
				throw new IllegalArgumentException("Plots "+Arrays.toString(unavailablePlots.toArray())+" are unavailable!");}
			
			final World fromWorld = Bukkit.getServer().createWorld(new WorldCreator(worldName));
			final World toWorld = selection.getWorld();
			
				LinkedList<UUID> arcaderIdsQueue = new LinkedList<UUID> (crafterArcadian.arcadianIds); 
			for (int x=minX;x<=maxX;x++){
				for (int z=minZ;z<=maxZ;z++){
					if (arcaderIdsQueue.isEmpty())break;

					Plot plot = new Plot(name, PlotManager.getPlotTopLoc(toWorld, x+";"+z), PlotManager.getPlotBottomLoc(toWorld, x+";"+z), x+";"+z, PlotManager.getMap(toWorld).DaysToExpiration);
					PlotManager.getPlots(toWorld).put(x+";"+z, plot);
					SqlManager.addPlot(plot, x, z, toWorld);
					try{ 
						String arcadianName = null;
						arcadianName = CrafterWrapper.getPlayerName(arcaderIdsQueue.pop());
						if (allowed) plot.addAllowed(arcadianName);
						else plot.addDenied(arcadianName);}
					catch(Exception e){}
					
					final Location plot1Bottom = PlotManager.getPlotBottomLoc(fromWorld, modelId);
					Location plot2Bottom = PlotManager.getPlotBottomLoc(toWorld, x+";"+z);
					final Location plot1Top = PlotManager.getPlotTopLoc(fromWorld, modelId);
					
					final int distanceX = plot1Bottom.getBlockX() - plot2Bottom.getBlockX();
					final int distanceZ = plot1Bottom.getBlockZ() - plot2Bottom.getBlockZ();
					crafterArcadian.coordinator.scheduleTask((new BukkitRunnable(){public void run(){
						for(int X = plot1Bottom.getBlockX(); X <= plot1Top.getBlockX(); X++){
							for(int Z = plot1Bottom.getBlockZ(); Z <= plot1Top.getBlockZ(); Z++){
								Block plot1Block = fromWorld.getBlockAt(new Location(fromWorld, X, 0, Z));
								Block plot2Block = toWorld.getBlockAt(new Location(toWorld, X - distanceX, 0, Z - distanceZ));
								
								String plot1Biome = plot1Block.getBiome().name();
								plot2Block.setBiome(Biome.valueOf(plot1Biome));
								for(int Y = 0; Y < toWorld.getMaxHeight() ; Y++){
									plot1Block = fromWorld.getBlockAt(new Location(fromWorld, X, Y, Z));
				                    int plot1Type = plot1Block.getTypeId();
				                    byte plot1Data = plot1Block.getData();
									
									plot2Block = toWorld.getBlockAt(new Location(toWorld, X - distanceX, Y, Z - distanceZ));
									plot2Block.setTypeIdAndData(plot1Type, plot1Data, false);
									plot2Block.setData(plot1Data);}}}}}), pmi.PlotSize*pmi.PlotSize*toWorld.getMaxHeight());}}
			self=null;}
		finally{lock.unlock();}}
	
	
	private CrafterArcadia(String Name, String ModelId,String WorldName,boolean Allowed,CrafterArcadian CrafterArcadian){
		name=Name;
		modelId=ModelId;
		worldName=WorldName;
		allowed=Allowed;
		crafterArcadian=CrafterArcadian;
		self=this;}	}
