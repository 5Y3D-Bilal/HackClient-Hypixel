package keystrokesmod.module.impl.hypixelpit;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.SliderSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class SpadeSwapper extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();
    private static SliderSetting swapDelay; // Delay between swaps
    private final SliderSetting swapSlot1;
    private final SliderSetting swapSlot2;

    private long lastSwapTime = 0; // Time of the last swap in milliseconds

    public SpadeSwapper() {
        super("SpadeSwapper", Module.category.hypixelpit, 0);
        this.registerSetting(swapDelay = new SliderSetting("Swap Delay", 0.01D, 0.01D, 0.5D, 0.01D));
        this.registerSetting(swapSlot1 = new SliderSetting("Select Slot 1", 1, 1, 9, 1)); // Slot 1
        this.registerSetting(swapSlot2 = new SliderSetting("Select Slot 2", 2, 1, 9, 1)); // Slot 2
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null)
            return;

        long currentTime = System.currentTimeMillis(); // Get current time in milliseconds
        long delayInMillis = (long) (swapDelay.getInput() * 1000); // Convert seconds to milliseconds

        // Check if the delay has passed
        if (currentTime - lastSwapTime > delayInMillis) {
            swapItems();
            lastSwapTime = currentTime;
        }
    }

    private void swapItems() {
        EntityPlayerSP player = mc.thePlayer;

        // Swap items in the selected hotbar slots
        int slot1 = (int) swapSlot1.getInput() - 1; // Convert to 0-based index
        int slot2 = (int) swapSlot2.getInput() - 1; // Convert to 0-based index

        int currentItem = player.inventory.currentItem;
        if (currentItem == slot1) {
            player.inventory.currentItem = slot2;
        } else if (currentItem == slot2) {
            player.inventory.currentItem = slot1;
        }
    }

    @Override
    public void enable() {
        super.enable();
        sendToggleMessage("SpadeSwapper enabled.", EnumChatFormatting.GREEN);
    }

    @Override
    public void disable() {
        super.disable();
        sendToggleMessage("SpadeSwapper disabled.", EnumChatFormatting.RED);
    }

    private void sendToggleMessage(String message, EnumChatFormatting color) {
        if (mc.thePlayer != null) {
            ChatComponentText chatMessage = new ChatComponentText(color + message);
            mc.thePlayer.addChatMessage(chatMessage);
        }
    }
}
