package dev.anarchy.waifuhax.api.mixins.events;

import dev.anarchy.waifuhax.client.WaifuHax;
import dev.anarchy.waifuhax.client.events.ChunkOcclusionEvent;
import net.minecraft.client.render.chunk.ChunkOcclusionDataBuilder;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkOcclusionDataBuilder.class)
public class ChunkOcclusionDataBuilderMixin {

    @Inject(method = "markClosed", at = @At("HEAD"), cancellable = true)
    private void onMarkClosed(BlockPos pos, CallbackInfo info) {
        ChunkOcclusionEvent event = WaifuHax.EVENT_BUS.post(ChunkOcclusionEvent.get());
        if (event.isCancelled()) info.cancel();
    }
}
