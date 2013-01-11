package keepcalm.mods.events.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraftforge.event.Cancelable;
import net.minecraftforge.event.entity.EntityEvent;

@Cancelable
public class CreeperExplodeEvent extends EntityEvent {

	final int explosionRadius;
	
	final EntityCreeper creeper;
	
	public CreeperExplodeEvent(EntityCreeper entity, int explodeRadius) {
		super(entity);
		
		creeper = entity;
		explosionRadius = explodeRadius;
		
		
	}

}
