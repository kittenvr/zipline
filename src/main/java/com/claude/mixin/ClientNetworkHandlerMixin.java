package com.claude.mixin;

import com.claude.Zipline;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientNetworkHandlerMixin {
	@Inject(method = "onChunkData", at = @At("TAIL"))
	private void onChunkLoad(ChunkDataS2CPacket packet, CallbackInfo ci) {
		ChunkPos pos = new ChunkPos(packet.getChunkX(), packet.getChunkZ());
		Zipline.LOGGER.debug("Chunk loaded at {}, {}", pos.x, pos.z);
		Zipline.EXECUTOR.getChunkTracker().markChunkLoaded(pos);
	}
}
