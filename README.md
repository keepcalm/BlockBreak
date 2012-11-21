BlockBreak Event for MinecraftForge
===================================

This adds an API for modders to use to listen for a block being broken.

The transformer itself was written by <a href="http://www.minecraftforum.net/topic/1009577-">bspkrs</a> for use in his treecaptitator mod.

*For modders:*
You simply need to register a forge event handler the usual way, with a BlockBreakEvent as the argument, for example:<br />
<pre>
@ForgeSubscribe
public void onBlockBreak(BlockBreakEvent ev) {
	System.out.println("Break!");
}
</pre>

and then to register,
<code> MinecraftForge.EVENT\_BUS.register(new ForgeEventHandler())</code> in your init.

<a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB"><img alt="Creative Commons Licence" style="border-width:0" src="http://i.creativecommons.org/l/by-nc-sa/3.0/88x31.png" /></a><br />This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB">Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License</a>.
