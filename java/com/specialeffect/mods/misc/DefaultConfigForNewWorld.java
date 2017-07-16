/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.mods.misc;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.specialeffect.messages.UseDoorAtPositionMessage;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.ModUtils;
import com.specialeffect.utils.OpenableBlock;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = DefaultConfigForNewWorld.MODID, 
version = ModUtils.VERSION,
name = DefaultConfigForNewWorld.NAME)
public class DefaultConfigForNewWorld 
{
	public static final String MODID = "specialeffect.defaultconfig";
	public static final String NAME = "DefaultConfig";

    public static SimpleNetworkWrapper network;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);
		
		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Apply default config to new worlds");
		ModUtils.setAsParent(event, SpecialEffectMisc.MODID);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
	}
		
	@EventHandler
    public void onWorldLoad(FMLServerStartedEvent event) {

		WorldServer worldServer = DimensionManager.getWorld(0); // default world
        GameRules gameRules = worldServer.getGameRules();                
        printGameRules(gameRules);
        
        // The first time the world loads, we set our preferred game rules
        // Users may override them manually later.        
        if (worldServer.getTotalWorldTime() < 10) {
        	setDefaultGameRules(gameRules);
        }
    }
	
	private void setDefaultGameRules(GameRules rules) {
    	rules.setOrCreateGameRule("doDaylightCycle", "False");        	  	
    	rules.setOrCreateGameRule("doWeatherCycle", "False");        	  	
    	rules.setOrCreateGameRule("keepInventory", "True");        	  	
	}
	
	private void printGameRules(GameRules rules) {
		System.out.println("Game rules:");
		String[] keys = rules.getRules();
        for (String key : keys) {
        	System.out.println(key + ": " + rules.getString(key));   	
        }      	  	
	}
}
