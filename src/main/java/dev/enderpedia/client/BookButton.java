package dev.enderpedia.client;

import dev.enderpedia.Config;
import dev.enderpedia.Enderpedia;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** The inventory button. Follows the inventory panel, which slides when the recipe book opens. */
public final class BookButton extends Button {
    public static final ResourceLocation ICON = ResourceLocation.fromNamespaceAndPath(Enderpedia.MODID, "textures/gui/button.png");
    private final AbstractContainerScreen<?> screen;
    private final boolean creative;

    public BookButton(AbstractContainerScreen<?> screen, boolean creative) {
        super(0, 0, 20, 18, Component.empty(), b -> Minecraft.getInstance().setScreen(new IndexScreen(screen)), DEFAULT_NARRATION);
        this.screen = screen;
        this.creative = creative;
        setTooltip(Tooltip.create(Component.translatable("enderpedia.button")));
        place();
    }

    private void place() {
        if (creative) {
            // Right of the creative panel, level with the search bar row.
            setPosition(screen.getGuiLeft() + screen.getXSize() + 2 + Config.BUTTON_X.get(), screen.getGuiTop() + 4 + Config.BUTTON_Y.get());
        } else {
            // Beside the recipe book button, between the crafting grid and the main inventory.
            setPosition(screen.getGuiLeft() + 126 + Config.BUTTON_X.get(), screen.getGuiTop() + 61 + Config.BUTTON_Y.get());
        }
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        place();
        super.renderWidget(g, mouseX, mouseY, partialTick);
        g.blit(ICON, getX() + 2, getY() + 1, 0, 0, 16, 16, 16, 16);
    }
}
