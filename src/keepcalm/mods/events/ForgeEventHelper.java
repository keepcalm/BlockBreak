package keepcalm.mods.events;

import keepcalm.mods.bukkit.bukkitAPI.scheduler.BukkitDummyPlugin;
import keepcalm.mods.bukkit.forgeHandler.ForgeEventHandler;
import keepcalm.mods.events.events.BlockDestroyEvent;
import keepcalm.mods.events.events.DispenseItemEvent;
import keepcalm.mods.events.events.LiquidFlowEvent;
import keepcalm.mods.events.events.PlayerDamageBlockEvent;
import keepcalm.mods.events.events.PlayerMoveEvent;
import keepcalm.mods.events.events.PlayerUseItemEvent;
import keepcalm.mods.events.events.SheepDyeEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemInWorldManager;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetServerHandler;
import net.minecraft.network.packet.Packet10Flying;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;

import org.bukkit.Bukkit;

import cpw.mods.fml.common.FMLCommonHandler;

/**
 * Class of hooks to be used from ASM-injected code
 * 
 * RULES OF ADDING METHODS:
 *  - They must use the least number of arguments possible - if you need obj.x, obj.y and obj.z, just pass obj.
 *  This is for efficiency.
 *  - If they are going to be cancelable, they must return TRUE when CANCELLED, this is easier to write in ASM.
 * @author keepcalm
 *
 */
public class ForgeEventHelper {
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
		if (!ForgeEventHandler.ready)
			return;
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
				if (i.getClassName().contains("keepcalm.mods.bukkit")) {
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
			Bukkit.getScheduler().runTaskLater(BukkitDummyPlugin.INSTANCE, new Runnable() {
				
				@Override
				public void run() {
					BlockDestroyEvent ev = new BlockDestroyEvent(world, x, y, z, id, data);
					MinecraftForge.EVENT_BUS.post(ev);
					
					if (ev.isCanceled()) {
						world.setBlockAndMetadata(x, y, z, id, data);
					}
				}
				
			}, 10);
		}
	}
	
}
