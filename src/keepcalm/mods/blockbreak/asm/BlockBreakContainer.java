package keepcalm.mods.blockbreak.asm;

import java.util.Arrays;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.ModMetadata;

public class BlockBreakContainer extends DummyModContainer {
	
	private ModMetadata myMeta;
	
	public BlockBreakContainer() {
		myMeta = super.getMetadata();
		myMeta.name = "Block Break Event";
		myMeta.modId = "BlockBreakEvent";
		myMeta.credits = "bspkrs, who wrote the ASM code";
		myMeta.description = "Adds a block break event for modders to use.";
		myMeta.version = "1.4.5-0";
		myMeta.authorList = Arrays.asList( new String[] {"keepcalm"});
	}
	
}
