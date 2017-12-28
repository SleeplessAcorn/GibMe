package info.sleeplessacorn.gibme;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class GibToast implements IToast {

    private final ItemStack stack;
    private long timestamp = -1;

    protected GibToast(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public Visibility draw(GuiToast toast, long delta) {
        if (timestamp < 0)
            timestamp = delta;

        if (!stack.isEmpty()) {
            Minecraft mc = toast.getMinecraft();
            mc.getTextureManager().bindTexture(TEXTURE_TOASTS);
            GlStateManager.color(1.0F, 1.0F, 1.0F);
            toast.drawTexturedModalRect(0, 0, 0, 32, 160, 32);
            mc.fontRenderer.drawString(I18n.format("toast.gibme.title"), 30, 7, 0xff500050);
            mc.fontRenderer.drawString(I18n.format("toast.gibme.description", stack.getDisplayName()), 30, 18, 0xff000000);
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(null, stack, 8, 8);
            return delta - timestamp >= 5000L ? Visibility.HIDE : Visibility.SHOW;
        }
        return Visibility.HIDE;
    }

}
