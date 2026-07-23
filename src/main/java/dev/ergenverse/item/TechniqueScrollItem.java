package dev.ergenverse.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * TechniqueScrollItem — a cultivation technique scroll (功法卷轴).
 *
 * <p>Right-click to study the scroll: consumes it, applies a temporary buff
 * representing the technique, plays a study sound, and spawns particles.
 *
 * <p>Canon: technique scrolls are the primary currency of cultivation knowledge
 * in Renegade Immortal. Wang Lin steals the restriction art scroll from Heng Yue
 * Sect's library (RI Ch.~30). Sects guard their technique scrolls as the most
 * valuable treasures — more precious than spirit stones or pills. A scroll is
 * compressed knowledge — reading it takes years in canon; here we compress it
 * into a right-click for gameplay.
 */
public class TechniqueScrollItem extends Item {

    private final ScrollType scrollType;

    public TechniqueScrollItem(ScrollType scrollType, Properties props) {
        super(props);
        this.scrollType = scrollType;
    }

    public ScrollType getScrollType() { return scrollType; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(stack);

        if (level instanceof ServerLevel sl) {
            // Study the scroll: apply the technique effect
            scrollType.applyEffect(player);
            // Consume the scroll (one-time study)
            stack.shrink(1);
            // Study sound
            player.playSound(scrollType.getSound(), 1.0F, 0.8F);
            // Visual: particles spiraling upward
            sl.sendParticles(scrollType.getParticle(),
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    15, 0.5, 1.0, 0.5, 0.05);
            // Chat message confirming the technique studied
            player.sendSystemMessage(Component.literal("\u00A7a\u00A7oYou studied the scroll and grasped the essence of: \u00A76" + scrollType.getDisplayName()));
            // Cooldown to prevent spam
            player.getCooldowns().addCooldown(this, 60);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("\u00A7e" + scrollType.getDisplayName()));
        tooltip.add(Component.literal("\u00A77" + scrollType.getDescription()));
        tooltip.add(Component.literal("\u00A78Right-click to study (consumed)"));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Scrolls shimmer slightly compared to normal items
        return true;
    }
}
