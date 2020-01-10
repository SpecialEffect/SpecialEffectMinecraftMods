/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.utils;

import java.awt.Point;
import java.util.UUID;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ModUtils {

	// This is where we specify the version that all our mods use
	public static final String VERSION  = "1.0.18";

	// version of optikey to use
	// for now we're keeping them in lockstep
	public static final String OPTIKEY_VERSION = ModUtils.VERSION;

	// A helper function to replace mcmod.info. Must be called with
	// a pre-init event.
	public static void setupModInfo(FMLCommonSetupEvent event, String modId, String modName,
			String modDescription) {
		
//		// Adding info here avoids having to maintain a mcmod.info file.
//		ModMetadata m = event.getModMetadata();		
//		m.autogenerated = false;
//		m.modId = modId;
//		m.version = VERSION;
//		m.name = modName;
//		m.url = "https://github.com/kirstymcnaught/SpecialEffectMinecraftMods";
//		m.description = modDescription;
//		m.description += "\n\nFor eye control, use EyeMine version " + OPTIKEY_VERSION;
//		m.authorList.add("Kirsty McNaught");
//		m.credits = "Written in collaboration with SpecialEffect";
	}

	public static void setAsParent(FMLCommonSetupEvent event, String parentModID) {
//		ModMetadata m = event.getModMetadata();
//		m.parent = parentModID;
	}

	// Check if LivingEntity is the current player (and not another
	// player on the network, for instance)
	public static boolean entityIsMe(Entity entity) {				
		if (entity instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) entity;
			UUID playerUUID = player.getUniqueID();
			PlayerEntity myself = Minecraft.getInstance().player;
			if (null == myself) {
				return false;
			}
			UUID myUUID = myself.getUniqueID();

			return (playerUUID.equals(myUUID));
		} else {
			return false;
		}
	}

	// Get the x, y point corresponding to one of 8 compass points
	// 0 = N, 1 = NE, 2 = E, etc ...
	public static Point getCompassPoint(int i) {
		Point p = new Point(0, 0);
		i = i % 8;
		switch (i) {
		case 0:
			p.setLocation(0, +1);
			break;
		case 1:
			p.setLocation(+1, +1);
			break;
		case 2:
			p.setLocation(+1, 0);
			break;
		case 3:
			p.setLocation(+1, -1);
			break;
		case 4:
			p.setLocation(0, -1);
			break;
		case 5:
			p.setLocation(-1, -1);
			break;
		case 6:
			p.setLocation(-1, 0);
			break;
		default:
			p.setLocation(-1, +1);
			break;
		}
		return p;
	}
//
//	public static Point getScaledDisplaySize(Minecraft mc) {
//		Point p = new Point(0, 0);
//		ScaledResolution res = new ScaledResolution(mc);
//		p.setLocation(res.getScaledWidth(), res.getScaledHeight());
//
//		return p;
//
//	}

	public static void drawTexQuad(double x, double y, double width, double height, float alpha) {
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, alpha);
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();		

		int z = 10;
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos(x, y + height, z).tex(0.0, 1.0).endVertex();
		bufferbuilder.pos(x + width, y + height, z).tex(1.0, 1.0).endVertex();
		bufferbuilder.pos(x + width, y, z).tex(1.0, 0.0).endVertex();
		bufferbuilder.pos(x, y, z).tex(0.0, 0.0).endVertex();

		tessellator.draw();
		
//		
//		final float uScale = 1f / 0x100;
//        final float vScale = 1f / 0x100;
//
//        Tessellator tessellator = Tessellator.getInstance();
//        BufferBuilder wr = tessellator.getBuffer();
//        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
//        wr.pos(x        , y + height, zLevel).tex( u          * uScale, ((v + height) * vScale)).endVertex();
//        wr.pos(x + width, y + height, zLevel).tex((u + width) * uScale, ((v + height) * vScale)).endVertex();
//        wr.pos(x + width, y         , zLevel).tex((u + width) * uScale, ( v           * vScale)).endVertex();
//        wr.pos(x        , y         , zLevel).tex( u          * uScale, ( v           * vScale)).endVertex();
//        tessellator.draw();
//        
        
		
//		//---
//		 Tessellator tessellator = Tessellator.getInstance();
//	      BufferBuilder bufferbuilder = tessellator.getBuffer();
//	      float f = this.mc.player.getBrightness();
//	      GlStateManager.color4f(f, f, f, 0.1F);
//	      GlStateManager.enableBlend();
//	      GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//	      GlStateManager.pushMatrix();
//	      float f1 = 4.0F;
//	      float f2 = -1.0F;
//	      float f3 = 1.0F;
//	      float f4 = -1.0F;
//	      float f5 = 1.0F;
//	      float f6 = -0.5F;
//	      float f7 = -this.mc.player.rotationYaw / 64.0F;
//	      float f8 = this.mc.player.rotationPitch / 64.0F;
//	      bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
//	      bufferbuilder.pos(-1.0D, -1.0D, -0.5D).tex((double)(4.0F + f7), (double)(4.0F + f8)).endVertex();
//	      bufferbuilder.pos(1.0D, -1.0D, -0.5D).tex((double)(0.0F + f7), (double)(4.0F + f8)).endVertex();
//	      bufferbuilder.pos(1.0D, 1.0D, -0.5D).tex((double)(0.0F + f7), (double)(0.0F + f8)).endVertex();
//	      bufferbuilder.pos(-1.0D, 1.0D, -0.5D).tex((double)(4.0F + f7), (double)(0.0F + f8)).endVertex();
//	      tessellator.draw();
//
//	      GlStateManager.popMatrix();
//	      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
//	      GlStateManager.disableBlend();

	}

	// Find an item in the hotbar which matches the given class
	// (this includes all subclasses)
	public static int findItemInHotbar(PlayerInventory inventory, Class<?> itemClass) {
		int itemId = -1;
		int currentItemId = inventory.currentItem;
		NonNullList<ItemStack> items = inventory.mainInventory;
		if (items != null) {
			for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
				ItemStack stack = items.get(i);
				if (stack != null && stack.getItem() != null) {
					Item item = stack.getItem();
					if (itemClass.isInstance(item)) {
						itemId = i;
						// Ideally we'd keep the current item if it
						// happens to match the spec.
						if (itemId == currentItemId) {
							return itemId;
						}
					}
				}
			}
		}
		return itemId;
	}

	// should be run from onliving
	// maybe also needs running from server??
	public static void moveItemToHotbarAndSelect(PlayerInventory inventory, ItemStack item) {
		// stick the item in an arbitrary non-hotbar slot, then let the
		// inventory
		// figure out how best to move it to the hotbar (e.g. to an empty slot).
		int slotId = 12;
		inventory.setInventorySlotContents(slotId, item);
		inventory.pickItem(slotId);
	}
	
	public static void sendPlayerMessage(String msg) {
		PlayerEntity player = Minecraft.getInstance().player;
		if (null != player) {			
			player.sendMessage(new StringTextComponent(msg));
		}
	}
	
	public static EntityRayTraceResult getMouseOverEntity() {	
		// Returns the entity the mouse is over, or null
		RayTraceResult result = Minecraft.getInstance().objectMouseOver;	
		if (result != null && result instanceof EntityRayTraceResult) {
			return (EntityRayTraceResult)result;
		}
		else {
			return null;
		}
	}
	
	public static BlockRayTraceResult getMouseOverBlock() {	
		// Returns the block the mouse is over, or null
		RayTraceResult result = Minecraft.getInstance().objectMouseOver;	
		if (result != null && result instanceof BlockRayTraceResult) {
			return (BlockRayTraceResult)result;
		}
		else {
			return null;
		}
	}
	
}
