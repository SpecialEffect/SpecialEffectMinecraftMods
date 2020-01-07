/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.mods.moving;

import org.lwjgl.glfw.GLFW;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.gui.JoystickControlOverlay;
import com.specialeffect.gui.StateOverlay;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.mods.mousehandling.MouseHandler;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.java.games.input.Keyboard;
import net.java.games.input.Mouse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.SubscribeEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MoveWithGaze2.MODID)
public class MoveWithGaze2 
extends BaseClassWithCallbacks 
implements ChildModWithConfig
{
	public static final String MODID = "movewithgaze2";
    public static final String NAME = "MoveWithGaze2";

    private static KeyBinding mToggleAutoWalkKB;
    
    public static Configuration mConfig;

    private static boolean mMoveWhenMouseStationary = false;
    private static float mCustomSpeedFactor = 0.8f;

    public MoveWithGaze2() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
	}
	
	private void setup(final FMLCommonSetupEvent event) {
		
		MinecraftForge.EVENT_BUS.register(this);  
    	
    	ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Add key binding to start/stop walking continuously, with direction controlled by mouse/eyetracker");
    	ModUtils.setAsParent(event, EyeGaze.MODID);
    	
    	mOverlay = new JoystickControlOverlay(Minecraft.getInstance());
    
    	// postInit
		MinecraftForge.EVENT_BUS.register(mOverlay);
	}
	
    @SubscribeEvent
    public void init(FMLInitializationEvent event)
    {	

    	// Subscribe to parent's config changes
    	EyeGaze.registerForConfigUpdates((ChildModWithConfig) this);
    	
    	// Register key bindings	
    	mToggleAutoWalkKB = new KeyBinding("Start/stop walking (simple mode)", GLFW.GLFW_KEY_B, CommonStrings.EYEGAZE_COMMON);
        ClientRegistry.registerKeyBinding(mToggleAutoWalkKB);
        
		// Register an icon for the overlay
		mIconIndex = StateOverlay.registerTextureLeft("specialeffect:icons/legacy-mode.png");
    }
    
    private static int mIconIndex;
    
	private static JoystickControlOverlay mOverlay;

	public static void stop() {
		if (mDoingAutoWalk) {
			mDoingAutoWalk = false;
			StateOverlay.setStateLeftIcon(mIconIndex, false);        	
    		mOverlay.setVisible(false);
		}
    }
	
	public void syncConfig() {
        mMoveWhenMouseStationary = EyeGaze.moveWhenMouseStationary;
        mCustomSpeedFactor = EyeGaze.customSpeedFactor;
	}
	
	// Some hard-coded fudge factors for maximums.
	// TODO: make configurable?
	private final float mMaxForward = 1.5f;
	private final float mMaxBackward = 0.5f;
	private final int mMaxYaw = 100; // at 100% sensitivity
    
    @SubscribeEvent
    public void onLiving(LivingUpdateEvent event) {
    	if (ModUtils.entityIsMe(event.getEntityLiving())) {

    		PlayerEntity player = (PlayerEntity)event.getEntityLiving();    		
    		if (mDoingAutoWalk && 
            		null == Minecraft.getInstance().currentScreen && // no gui visible
            		(mMoveWhenMouseStationary || MouseHandler.hasPendingEvent()) ) {
    			
    			// Y gives distance to walk forward/back.
    			float walkForwardAmount = 0.0f;
    			float h = (float)Minecraft.getInstance().currentScreen.height;
    			float h3 = h/3.0f;

    			if (lastMouseY < h3) {
    				walkForwardAmount = -mMaxBackward*(h3-lastMouseY)/h3;
    			}
    			else if (lastMouseY > 2*h3) {
    				walkForwardAmount = mMaxForward*(lastMouseY-2*h3)/h3;
    			}

    			// scaled by mCustomSpeedFactor 
    			walkForwardAmount *= mCustomSpeedFactor;

    			// X gives how far to rotate viewpoint
    			float w = (float)Minecraft.getInstance().currentScreen.width;
    			float w2 = w/2.0f;

    			float yawAmount = (lastMouseX - w2)/w2;
    			yawAmount*= mMaxYaw;
    			    			
    			// scaled by user sensitivity
    			// TODO: sensitivity isn't linear :-S
    			float sens = (float) Minecraft.getInstance().gameSettings.mouseSensitivity;
    			yawAmount *= Math.max(sens, 0.05);
    			
    			// TODO: Scale by user sensitivity?
    			
    			player.rotationYaw += yawAmount;
    			//FIXME player.moveEntityWithHeading(0.0f, walkForwardAmount);
    			
    		}
			this.processQueuedCallbacks(event);
			
    	}
    }
    
	private static boolean mDoingAutoWalk = false;

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        
        if(mToggleAutoWalkKB.isPressed()) {
        	mDoingAutoWalk = !mDoingAutoWalk;        	
        	MouseHandler.setLegacyWalking(mDoingAutoWalk);
        	
        	mOverlay.setVisible(mDoingAutoWalk);
			StateOverlay.setStateLeftIcon(mIconIndex, mDoingAutoWalk);
        	this.queueChatMessage("Auto walk: " + (mDoingAutoWalk ? "ON" : "OFF"));
        }
    }
    
    private int lastMouseX = 0;
    private int lastMouseY = 0;
    
    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
    	if (mDoingAutoWalk) {
    		if (Mouse.isGrabbed()) {
	    		// when mouse is captured, x and y pos are encoded in deltas.
	    		lastMouseX = Minecraft.getInstance().currentScreen.width/2 + Mouse.getEventDX();
	    		lastMouseY = Minecraft.getInstance().currentScreen.height/2 + Mouse.getEventDY();
    		}
    		else {
    			lastMouseX = Mouse.getEventX();
	    		lastMouseY = Mouse.getEventY();
    		}
    	}
    	else {
    		lastMouseX = Minecraft.getInstance().currentScreen.width/2;
    		lastMouseY = Minecraft.getInstance().currentScreen.height/2;
    	}
    	
    	// TODO: do we need to reset mouse to (0,0) when we're done? otherwise next mouse event
    	// after turning this off will snap us round (probably fine with eye gaze though)
    }
}

