package info.u_team.music_player.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import info.u_team.music_player.init.MusicPlayerEvents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;

@Mixin(Gui.class)
abstract class GuiMixin {
	
	@Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "TAIL"))
	private void musicplayer$renderTextInGui(GuiGraphicsExtractor guiGraphics, DeltaTracker partialTick, CallbackInfo info) {
		MusicPlayerEvents.onRenderGameOverlay(guiGraphics, partialTick);
	}
	
}
