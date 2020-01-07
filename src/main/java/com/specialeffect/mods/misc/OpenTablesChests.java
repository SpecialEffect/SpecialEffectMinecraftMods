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

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
//import com.specialeffect.messages.ActivateBlockAtPosition;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.java.games.input.Keyboard;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.WorkbenchBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.SubscribeEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(OpenTablesChests.MODID)
public class OpenTablesChests 
extends BaseClassWithCallbacks
implements ChildModWithConfig
{

	public static final String MODID = "opentableschests";
	public static final String NAME = "OpenTablesChests";

    public static Configuration mConfig;
	private static KeyBinding mOpenChestKB;
	private static KeyBinding mOpenCraftingTableKB;	
	
    //FIXME for 1.14 public static SimpleNetworkWrapper network;
    
    private static int mRadius = 5;

	@SubscribeEvent
	@SuppressWarnings("static-access")
	public void preInit(FMLPreInitializationEvent event) {    
		MinecraftForge.EVENT_BUS.register(this);    	
		
		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Add key bindings to open nearby chests/crafting tables.");
		ModUtils.setAsParent(event, EyeGaze.MODID);
		
        //FIXME network = NetworkRegistry.INSTANCE.newSimpleChannel(this.NAME);
        //FIXME network.registerMessage(ActivateBlockAtPosition.Handler.class, 
        						ActivateBlockAtPosition.class, 0, Side.SERVER);
        
		// Set up config
    	mConfig = new Configuration(event.getSuggestedConfigurationFile());
    	this.syncConfig();
	}

	@SubscribeEvent
	public void init(FMLInitializationEvent event)
	{
		// Register for config changes from parent
		EyeGaze.registerForConfigUpdates((ChildModWithConfig)this);
				
		// Register key bindings	
		mOpenChestKB = new KeyBinding("Open chest", GLFW.GLFW_KEY_LBRACKET, CommonStrings.EYEGAZE_EXTRA);
		mOpenCraftingTableKB = new KeyBinding("Open crafting table", GLFW.GLFW_KEY_RBRACKET, CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mOpenChestKB);
		ClientRegistry.registerKeyBinding(mOpenCraftingTableKB);
	}

	public void syncConfig() {
        mRadius = EyeGaze.mRadiusChests;
	}

	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			this.processQueuedCallbacks(event);
		}			
	}
	
	// Search for closest block of a certain class, within maximum radius
	private static BlockPos findClosestBlockOfType(String className, PlayerEntity player, World world, int radius) {
		BlockPos playerPos = player.getPosition();		
	    Class<?> classType;
    	BlockPos closestBlockPos = null;

		try {
			classType = Class.forName(className);
			
	    	// Find closest chest (within radius)
	    	double closestDistanceSq = Double.MAX_VALUE;
	    	for (int x = -radius; x <= radius; x++) {
	    		for (int z = -radius; z <= radius; z++) {
	    			for (int y = -radius; y <= radius; y++) { 

	    				BlockPos blockPos = playerPos.add(x, y, z);

	    				// Check if block is appropriate class
	    				Block block = world.getBlockState(blockPos).getBlock();
	    				if (classType.isInstance(block)) {
	    					double distSq = playerPos.distanceSq(new Vec3i(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
	    					if (distSq < closestDistanceSq) {
	    						closestBlockPos = blockPos;
	    						closestDistanceSq = distSq;
	    					}
	    				}
	    			}
	    		}
	    	}
		} catch (ClassNotFoundException e) {
			System.out.println("Could not find class: " + className);
		}
	    return closestBlockPos;
	}

	
	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		if(mOpenChestKB.isPressed()) {
			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
				@Override
				public void onLiving(LivingUpdateEvent event) {
					PlayerEntity player = (PlayerEntity) event.getEntityLiving();
					World world = Minecraft.getInstance().world;

					BlockPos closestBlockPos = OpenTablesChests.findClosestBlockOfType(
							ChestBlock.class.getName(), player, world, mRadius);
					
					// Ask server to open 
					if (null == closestBlockPos) {
						player.sendMessage(new StringTextComponent(
								"No chests found in range"));
					}
					else {
						//FIXME OpenTablesChests.network.sendToServer(
								//new ActivateBlockAtPosition(closestBlockPos));
					}
				}
			}));
		}
		else if(mOpenCraftingTableKB.isPressed()) {
			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
				@Override
				public void onLiving(LivingUpdateEvent event) {
					PlayerEntity player = (PlayerEntity) event.getEntityLiving();
					World world = Minecraft.getInstance().world;

					BlockPos closestBlockPos = OpenTablesChests.findClosestBlockOfType(
							WorkbenchBlock.class.getName(), player, world, mRadius);

					// Ask server to open 
					if (null == closestBlockPos) {
						player.sendMessage(new StringTextComponent(
								"No crafting tables found in range"));
					}
					else {
						//FIXME OpenTablesChests.network.sendToServer(
								//new ActivateBlockAtPosition(closestBlockPos));
					}
				}
			}));
		}
	}

}