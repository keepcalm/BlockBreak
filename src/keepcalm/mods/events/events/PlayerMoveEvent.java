package keepcalm.mods.events.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.Cancelable;
import net.minecraftforge.event.entity.player.PlayerEvent;

@Cancelable
public class PlayerMoveEvent extends PlayerEvent {
	final int oldX;
	final int oldY;
	final int oldZ;
	final int newX;
	final int newY;
	final int newZ;
	
	final boolean flying;

	public PlayerMoveEvent(EntityPlayer player, int oldX, int oldY, int oldZ, int newX, int newY, int newZ, boolean flying) {
		super(player);
		this.oldX = oldX;
		this.oldY = oldY;
		this.oldZ = oldZ;
		this.newX = newX;
		this.newY = newY;
		this.newZ = newZ;
		
		this.flying = flying;
		
	}

}
