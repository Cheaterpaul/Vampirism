package de.teamlapen.vampirism.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.teamlapen.lib.lib.client.gui.widget.ScrollableListWidget;
import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.network.RequestMinionSelectPacket;
import de.teamlapen.vampirism.network.SelectMinionTaskPacket;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@OnlyIn(Dist.CLIENT)
public class SelectMinionScreen extends Screen {
    private final Integer[] minionIds;
    private final ITextComponent[] minionNames;
    private final RequestMinionSelectPacket.Action action;

    public SelectMinionScreen(RequestMinionSelectPacket.Action a, List<Pair<Integer, ITextComponent>> minions) {
        super(new StringTextComponent(""));
        this.action = a;
        this.minionIds = minions.stream().map(Pair::getLeft).toArray(Integer[]::new);
        this.minionNames = minions.stream().map(Pair::getRight).toArray(ITextComponent[]::new);
    }

    @Override
    public void render(MatrixStack mStack, int p_render_1_, int p_render_2_, float p_render_3_) {
        renderBackground(mStack);
        super.render(mStack, p_render_1_, p_render_2_, p_render_3_);
    }

    @Override
    protected void init() {
        super.init();

        int w = 100;
        int maxH = 5;
        this.addButton(new ScrollableListWidget<>((this.width - w) / 2, (this.height - maxH * 20) / 2, w, Math.min(maxH * 20, 20 * minionNames.length), 20, this::getItems, (item, list) -> new ScrollableListWidget.TextComponentItem<>(item, list, SelectMinionScreen.this::onMinionSelected)));
    }

    private Collection<Pair<Integer, ITextComponent>> getItems(){
        List<ITextComponent> list = Arrays.asList(this.minionNames);
        return list.stream().map(item -> Pair.of(list.indexOf(item), item)).collect(Collectors.toList());
    }

    private void onMinionSelected(int id) {
        int selectedMinion = minionIds[id];
        if (action == RequestMinionSelectPacket.Action.CALL) {
            VampirismMod.dispatcher.sendToServer(new SelectMinionTaskPacket(selectedMinion, SelectMinionTaskPacket.RECALL));
        }
        this.closeScreen();
    }
}