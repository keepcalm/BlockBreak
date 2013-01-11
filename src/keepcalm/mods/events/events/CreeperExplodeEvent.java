package keepcalm.mods.events.events;

import net.minecraft.entity.monster.EntityCreeper;
import net.minecraftforge.event.Cancelable;
import net.minecraftforge.event.entity.EntityEvent;

@Cancelable
public class CreeperExplodeEvent extends EntityEvent {

	public final int explosionRadius;
	
	public final EntityCreeper creeper;
	
	public CreeperExplodeEvent(EntityCreeper entity, int explodeRadius) {
		super(entity);
		
		creeper = entity;
		explosionRadius = explodeRadius;
		
		
	}

}
