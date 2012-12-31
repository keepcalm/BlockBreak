package keepcalm.mods.blockbreak;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class EventFactory {
	//toInject.add(new MethodInsnNode(INVOKESTATIC, "keepcalm/mods/blockbreak/EventFactory", "onBlockHarvested", "(L" + (String) hm.get("worldJavaClassName") + ";IIIL" + (String) hm.get("blockJavaClassName") + ";IL" + (String) hm.get("entityPlayerJavaClassName") + ";)V"));
	public static void onBlockHarvested(World world, int x, int y, int z, Block block, int metadata, EntityPlayer entityPlayer) {
		PlayerBreakBlockEvent ev = new PlayerBreakBlockEvent(world,x,y,z,block,metadata,entityPlayer);
		MinecraftForge.EVENT_BUS.post(ev);
	}
}
