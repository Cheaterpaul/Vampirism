package de.teamlapen.vampirism.client.gui.overlay;

import de.teamlapen.faction.client.gui.overlay.BaseOverlay;
import de.teamlapen.vampirism.api.util.VIdentifier;
import de.teamlapen.vampirism.common.network.packets.client.ClientboundDraculaEventPacket;
import de.teamlapen.vampirism.common.world.entity.dracula.DraculaEvent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class DraculaEventOverlay extends BaseOverlay {

    private static final Identifier BACKGROUND = VIdentifier.mod("textures/misc/dracula_event.png");
    private static final Identifier TEXTURE = VIdentifier.mod("textures/misc/dracula_event_overlay.png");

    @Nullable
    private DraculaEvent event;

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (this.event != null) {
            guiGraphics.nextStratum();
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, 182, 34, 0,0);
        }
    }

    public void handle(ClientboundDraculaEventPacket packet) {
        switch (packet.operation()) {
            case ClientboundDraculaEventPacket.AddOperation addOperation -> {
                this.event = DraculaEvent.fromOperation(addOperation);
            }
            case ClientboundDraculaEventPacket.RemoveOperation removeOperation -> {
                if (event != null && event.id() == removeOperation.id()) {
                    this.event = null;
                }
            }
            case ClientboundDraculaEventPacket.UpdateOperation updateOperation -> {
                if (event != null && event.id() == updateOperation.id()) {
                    event.setPercentage(updateOperation.percentage());
                    event.setStage(updateOperation.stage());
                    event.setInVulnerable(updateOperation.vulnerable());
                }
            }
            case ClientboundDraculaEventPacket.UpdateProgressOperation updateProgressOperation -> {
                if (event != null && event.id() == updateProgressOperation.id()) {
                    event.setPercentage(updateProgressOperation.percentage());
                }
            }
            case ClientboundDraculaEventPacket.UpdateStageOperation updateStageOperation -> {
                if (event != null && event.id() == updateStageOperation.id()) {
                    event.setStage(updateStageOperation.stage());
                }
            }
            case ClientboundDraculaEventPacket.UpdateVulnerableOperation updateVulnerableOperation -> {
                if (event != null && event.id() == updateVulnerableOperation.id()) {
                    event.setInVulnerable(updateVulnerableOperation.vulnerable());
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + packet.operation());
        }
    }


}
