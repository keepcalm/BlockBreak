package keepcalm.mods.events;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

import keepcalm.mods.events.asm.transformers.events.ObfuscationHelper;
import keepcalm.mods.events.events.BlockDestroyEvent;
import keepcalm.mods.events.events.CreeperExplodeEvent;
import keepcalm.mods.events.events.DispenseItemEvent;
import keepcalm.mods.events.events.LightningStrikeEvent;
import keepcalm.mods.events.events.LiquidFlowEvent;
import keepcalm.mods.events.events.PlayerDamageBlockEvent;
import keepcalm.mods.events.events.PlayerMoveEvent;
import keepcalm.mods.events.events.PlayerUseItemEvent;
import keepcalm.mods.events.events.PressurePlateInteractEvent;
import keepcalm.mods.events.events.SheepDyeEvent;
import keepcalm.mods.events.events.SignChangeEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.EnumMobType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemInWorldManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetServerHandler;
import net.minecraft.network.packet.Packet10Flying;
import net.minecraft.network.packet.Packet130UpdateSign;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLCommonHandler;

/**
 * Class of hooks to be used from ASM-injected code
 * 
 * RULES OF ADDING METHODS:
 *  - They must use the least number of arguments possible - if you need obj.x, obj.y and obj.z, just pass obj.
 *  This is for efficiency.
 *  - If they are going to be cancellable, they must return TRUE when CANCELLED, this is easier to write in ASM.
 * @author keepcalm
 *
 */
public class ForgeEventHelper {
	
	public static Packet130UpdateSign onSignChange(NetServerHandler handler, Packet130UpdateSign pack) {
		SignChangeEvent ev = new SignChangeEvent(pack.xPosition, pack.yPosition, pack.zPosition, handler.getPlayer(), pack.signLines);
		MinecraftForge.EVENT_BUS.post(ev);
		
		if (ev.isCanceled()) {
			return null;
		}
		pack.signLines = ev.lines;
		
		return pack;
	}
	
	public static boolean onLightningStrike(EntityLightningBolt entity, World world, int x, int y, int z) {
		
		LightningStrikeEvent ev = new LightningStrikeEvent(entity, world, x, y, z);
		MinecraftForge.EVENT_BUS.post(ev);
		
		return ev.isCanceled();
	}
	
	public static boolean onPressurePlateInteract(BlockPressurePlate pp, World world, int x, int y, int z) {
		Class clazz = pp.getClass();
		EnumMobType type;
		try {
			Field f = clazz.getField(ObfuscationHelper.getRelevantMappings().get("blockPressurePlate_triggerMobType_fieldName"));
			f.setAccessible(true);
			type = (EnumMobType) f.get(pp);
		} catch (Exception e) {
			System.out.println("Fatal error in posting PressurePlateInteractEvent - perhaps you are using an incorrect version of Events API for your Minecraft version?");
			e.printStackTrace();
			return false;
		}
		
		
		float var7 = 0.125F;
		
		List entities = null;
		if (type == EnumMobType.everything)
        {
            entities = world.getEntitiesWithinAABBExcludingEntity((Entity)null, AxisAlignedBB.getAABBPool().addOrModifyAABBInPool((double)((float)x + var7), (double)y, (double)((float)z + var7), (double)((float)(x + 1) - var7), (double)y + 0.25D, (double)((float)(z + 1) - var7)));
        }

        if (type == EnumMobType.mobs)
        {
            entities = world.getEntitiesWithinAABB(EntityLiving.class, AxisAlignedBB.getAABBPool().addOrModifyAABBInPool((double)((float)x + var7), (double)y, (double)((float)z + var7), (double)((float)(x + 1) - var7), (double)y + 0.25D, (double)((float)(z + 1) - var7)));
        }

        if (type == EnumMobType.players)
        {
            entities = world.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getAABBPool().addOrModifyAABBInPool((double)((float)x + var7), (double)y, (double)((float)z + var7), (double)((float)(x + 1) - var7), (double)y + 0.25D, (double)((float)(z + 1) - var7)));
        }

        Entity targetEntity = null;
        if (!entities.isEmpty())
        {
            Iterator var9 = entities.iterator();
            
            while (var9.hasNext())
            {
                targetEntity = (Entity)var9.next();

                if (!targetEntity.doesEntityNotTriggerPressurePlate())
                {
                    break;
                }
            }
        }
        
        if (targetEntity == null) {
        	// ninja?
        	return false;
        }
		
        
        PressurePlateInteractEvent ev = new PressurePlateInteractEvent(targetEntity, pp, world, x, y, z);
		
        MinecraftForge.EVENT_BUS.post(ev);
        
        if (ev.isCanceled()) {
        	return true;
        }
        
		return false;
	}
	
	public static boolean onCreeperExplode(EntityCreeper creep) {
		NBTTagCompound nbt = new NBTTagCompound();
		creep.writeEntityToNBT(nbt);
		CreeperExplodeEvent ev = new CreeperExplodeEvent(creep, nbt.getByte("ExplosionRadius"));
		MinecraftForge.EVENT_BUS.post(ev);
		
		if (ev.isCanceled()) {
			return true;
		}
		return false;
	}
	
	public static void onItemUse(ItemStack stack, EntityPlayer who, World world, int x, int y, int z, int blockFace) {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient())
			// not on client
			return;
		PlayerUseItemEvent ev = new PlayerUseItemEvent(stack, who, world, x, y, z, ForgeDirection.getOrientation(blockFace));
		MinecraftForge.EVENT_BUS.post(ev);
	}
	
	public static boolean onBlockFlow(Block blck, World world, int flowX, int flowY, int flowZ) {
		
		Block flowingBlck = Block.blocksList[blck.blockID + 1];
		
		// get original liquid
		int origX = flowX;
		int origY = flowY;
		int origZ = flowZ;
		
		for (ForgeDirection i : ForgeDirection.values()) {
			
			origX += i.offsetX;
			origY += i.offsetY;
			origZ += i.offsetZ;
			
			if (world.getBlockId(origX, origY, origZ) == blck.blockID) {
				break;
			}
			origX -= i.offsetX;
			origY -= i.offsetY;
			origZ -= i.offsetZ;
		}
		
		LiquidFlowEvent ev = new LiquidFlowEvent(blck, world, flowX, flowY, flowZ, origX, origY, origZ);
		MinecraftForge.EVENT_BUS.post(ev);
		
		if (ev.isCanceled()) {
			return true;
		}
		
		return false;
	}
	public static boolean onPlayerMove(Packet10Flying pack, NetServerHandler handler) {
		PlayerMoveEvent ev = new PlayerMoveEvent(handler.playerEntity,
				MathHelper.floor_double(handler.playerEntity.posX), 
				MathHelper.floor_double(handler.playerEntity.posY),
				MathHelper.floor_double(handler.playerEntity.posZ), 
				MathHelper.floor_double(pack.xPosition), 
				MathHelper.floor_double(pack.yPosition), 
				MathHelper.floor_double(pack.zPosition), 
				!pack.onGround);
		
		MinecraftForge.EVENT_BUS.post(ev);
		
		if (ev.isCanceled()) {
			return true;
		}
		
		return false;
	}
	
	public static boolean onBlockDamage(ItemInWorldManager man) {
		
		// mcp has ridiculously long names
		
		if (man.curblockDamage % 2 == 1) {
			return false;
		}
		
		PlayerDamageBlockEvent ev = new PlayerDamageBlockEvent(man.thisPlayerMP, man.partiallyDestroyedBlockX,
				man.partiallyDestroyedBlockY, man.partiallyDestroyedBlockZ,
				man.theWorld, man.curblockDamage, man.durabilityRemainingOnBlock);
		
		MinecraftForge.EVENT_BUS.post(ev);
		
		if (ev.isCanceled()) {
			return true;
		}
		return false;
		
	}
	
	/**
	 * 
	 * @param block
	 * @param itemToDispense
	 * @param x
	 * @param y
	 * @param z
	 * @return whether to cancel or not (true == cancelled)
	 */
	public static boolean onDispenseItem(World world, int x, int y, int z, ItemStack itemToDispense) {
		DispenseItemEvent ev = new DispenseItemEvent(x, y, z, world, itemToDispense);
		MinecraftForge.EVENT_BUS.post(ev);
		
		if (ev.isCanceled()) {
			return true;
		}
		
		return false;
	}
	
	public static boolean onSheepDye(EntitySheep sheep, int newColour, byte oldColour) {
		SheepDyeEvent ev = new SheepDyeEvent(sheep, newColour, oldColour);
		MinecraftForge.EVENT_BUS.post(ev);
		
		if (ev.isCanceled()) return true;
		
		return false;
	}
	
	public static void onBlockBreak(final World world, final int x, final int y, final int z, final int id, final int data) {
		Chunk cnk = world.getChunkFromBlockCoords(x, z);
		if (!cnk.isTerrainPopulated || !cnk.isChunkLoaded) {
			return;
		}
		try {
			throw new RuntimeException("nobody saw this");
		}
		catch (RuntimeException ex) {
			boolean foundIIWM = false;
			int a = 0;
			//System.out.println("StackTrace count: " + ex.getStackTrace().length);
			for (StackTraceElement i : ex.getStackTrace()) {
				if (a == 1) {
					a++;
					continue;
				}
				//System.out.println("Class found: " + i.getClassName());
				if (i.getClassName().toLowerCase().contains("iteminworldmanager") || i.getClassName().toLowerCase().equals("ir")) {
					foundIIWM = true;
					break;
				}
				if (i.getMethodName().toLowerCase().contains("updateflow")) {
					foundIIWM = true;
					break;
				}
				if (i.getMethodName().toLowerCase().contains("l") && i.getClassName().toLowerCase().equals("aky")) {
					foundIIWM = true;
					break;
				}
				// it was us cancelling - or us doing something else
				if (i.getClassName().contains("keepcalm.mods.bukkit") || i.getClassName().contains("keepcalm.mods")) {
					foundIIWM = true;
					break;
				}
				a++;
			}
			
			if (foundIIWM) {// block break got this, or it's something else
				//System.out.println("Cancelled.");
				return;
			}
			if (id == 0) // no point - air got broken
				return;
			//System.out.println("This is a break!");
			Runnable run = new Runnable() {
				
				@Override
				public void run() {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {}
					BlockDestroyEvent ev = new BlockDestroyEvent(world, x, y, z, id, data);
					MinecraftForge.EVENT_BUS.post(ev);
					
					if (ev.isCanceled()) {
						world.setBlockAndMetadata(x, y, z, id, data);
					}
				}
				
			};
			Thread thr = new Thread(run);
			thr.start();
		}
	}
	
}
