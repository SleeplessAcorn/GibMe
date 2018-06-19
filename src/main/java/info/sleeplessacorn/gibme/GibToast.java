package info.sleeplessacorn.gibme;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@RequiredArgsConstructor
final class GibToast implements IToast {
    private static final String TITLE = "toast.gibme.title";
    private static final String DESC = "toast.gibme.description";

    private final ItemStack stack;
    private long timestamp = -1;

    @Override
    public Visibility draw(GuiToast toast, long delta) {
        if (timestamp < 0) timestamp = delta;
        if (stack.isEmpty()) return Visibility.HIDE;

        final Minecraft mc = toast.getMinecraft();
        final FontRenderer fr = mc.fontRenderer;

        mc.getTextureManager().bindTexture(TEXTURE_TOASTS);
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        toast.drawTexturedModalRect(0, 0, 0, 32, 160, 32);
        fr.drawString(I18n.format(GibToast.TITLE), 30, 7, 0xFF500050);
        fr.drawString(I18n.format(GibToast.DESC, stack.getDisplayName()), 30, 18, 0xFF000000);
        RenderHelper.enableGUIStandardItemLighting();
        mc.getRenderItem().renderItemAndEffectIntoGUI(null, stack, 8, 8);

        return delta - timestamp >= 5000L ? Visibility.HIDE : Visibility.SHOW;
    }
}
