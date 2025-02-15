package io.izzel.arclight.common.mixin.core.world.entity.decoration;

import com.google.common.collect.Lists;
import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.entity.player.ServerPlayerEntityBridge;
import io.izzel.arclight.common.bridge.core.world.WorldBridge;
import io.izzel.arclight.common.mixin.core.world.entity.LivingEntityMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(net.minecraft.world.entity.decoration.ArmorStand.class)
public abstract class ArmorStandMixin extends LivingEntityMixin {

    // @formatter:off
    @Shadow private boolean invisible;
    @Shadow public abstract ItemStack getItemBySlot(net.minecraft.world.entity.EquipmentSlot slotIn);
    @Shadow @Final private NonNullList<ItemStack> handItems;
    @Shadow @Final private NonNullList<ItemStack> armorItems;
    // @formatter:on

    @Override
    public float getBukkitYaw() {
        return this.getYRot();
    }

    @Inject(method = "hurt", cancellable = true, at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/entity/decoration/ArmorStand;kill()V"))
    public void arclight$damageDropOut(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (CraftEventFactory.handleNonLivingEntityDamageEvent((net.minecraft.world.entity.decoration.ArmorStand) (Object) this, source, amount)) {
            cir.setReturnValue(false);
        } else {
            arclight$callEntityDeath(source);
        }
    }

    @Inject(method = "hurt", cancellable = true, at = @At(value = "FIELD", target = "Lnet/minecraft/tags/DamageTypeTags;IS_EXPLOSION:Lnet/minecraft/tags/TagKey;"))
    public void arclight$damageNormal(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (CraftEventFactory.handleNonLivingEntityDamageEvent((net.minecraft.world.entity.decoration.ArmorStand) (Object) this, source, amount, true, this.invisible)) {
            cir.setReturnValue(false);
        }
    }

    @Redirect(method = "hurt", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/decoration/ArmorStand;invisible:Z"))
    private boolean arclight$softenCondition(net.minecraft.world.entity.decoration.ArmorStand entity) {
        return false;
    }

    @Inject(method = "hurt", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/entity/decoration/ArmorStand;kill()V"))
    private void arclight$damageDeath0(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        arclight$callEntityDeath(source);
    }

    @Inject(method = "hurt", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/entity/decoration/ArmorStand;kill()V"))
    private void arclight$damageDeath1(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        arclight$callEntityDeath(source);
    }

    @Inject(method = "hurt", at = @At(value = "INVOKE", ordinal = 2, target = "Lnet/minecraft/world/entity/decoration/ArmorStand;kill()V"))
    private void arclight$damageDeath2(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        arclight$callEntityDeath(source);
    }

    @Inject(method = "causeDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/ArmorStand;kill()V"))
    private void arclight$deathEvent2(ServerLevel serverLevel, DamageSource damageSource, float f, CallbackInfo ci) {
        arclight$callEntityDeath(damageSource);
    }

    @Redirect(method = "brokenByAnything", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/ArmorStand;dropAllDeathLoot(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;)V"))
    private void arclight$dropLater(net.minecraft.world.entity.decoration.ArmorStand instance, ServerLevel serverLevel, DamageSource damageSource) {
    }

    @Redirect(method = "brokenByAnything", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V"))
    private void arclight$captureDropsDeath(Level worldIn, BlockPos pos, ItemStack stack) {
        arclight$tryCaptureDrops(worldIn, pos, stack);
    }

    @Inject(method = "brokenByAnything", at = @At("RETURN"))
    private void arclight$spawnLast(ServerLevel serverLevel, DamageSource damageSource, CallbackInfo ci) {
        this.dropAllDeathLoot(serverLevel, damageSource);
    }

    @Override
    protected boolean shouldDropExperience() {
        return true;
    }

    private void arclight$tryCaptureDrops(Level worldIn, BlockPos pos, ItemStack stack) {
        if (!worldIn.isClientSide && !stack.isEmpty() && worldIn.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && !((WorldBridge) worldIn).bridge$forge$restoringBlockSnapshots()) { // do not drop items while restoring blockstates, prevents item dupe
            ItemEntity itementity = new ItemEntity(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack);
            if (!this.bridge$common$isCapturingDrops()) {
                this.bridge$common$startCaptureDrops();
            }
            this.bridge$common$captureDrop(itementity);
        }
    }

    private void arclight$callEntityDeath(DamageSource damageSource) {
        this.bridge$pushEntityRemoveCause(EntityRemoveEvent.Cause.DEATH);
        Collection<ItemEntity> captureDrops = this.bridge$common$getCapturedDrops();
        List<org.bukkit.inventory.ItemStack> drops;
        if (captureDrops == null) {
            drops = new ArrayList<>();
        } else if (captureDrops instanceof List) {
            drops = Lists.transform((List<ItemEntity>) captureDrops, e -> CraftItemStack.asCraftMirror(e.getItem()));
        } else {
            drops = captureDrops.stream().map(ItemEntity::getItem).map(CraftItemStack::asCraftMirror).collect(Collectors.toList());
        }
        CraftEventFactory.callEntityDeathEvent((net.minecraft.world.entity.decoration.ArmorStand) (Object) this, (damageSource == null ? this.damageSources().genericKill() : damageSource), drops);
    }

    @Inject(method = "swapItem", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;hasInfiniteMaterials()Z"))
    public void arclight$manipulateEvent(net.minecraft.world.entity.player.Player playerEntity, net.minecraft.world.entity.EquipmentSlot slotType, ItemStack itemStack, InteractionHand hand, CallbackInfoReturnable<Boolean> cir) {
        ItemStack itemStack1 = this.getItemBySlot(slotType);

        org.bukkit.inventory.ItemStack armorStandItem = CraftItemStack.asCraftMirror(itemStack1);
        org.bukkit.inventory.ItemStack playerHeldItem = CraftItemStack.asCraftMirror(itemStack);

        Player player = ((ServerPlayerEntityBridge) playerEntity).bridge$getBukkitEntity();
        ArmorStand self = (ArmorStand) ((EntityBridge) this).bridge$getBukkitEntity();

        EquipmentSlot slot = CraftEquipmentSlot.getSlot(slotType);
        EquipmentSlot bukkitHand = CraftEquipmentSlot.getHand(hand);
        PlayerArmorStandManipulateEvent event = new PlayerArmorStandManipulateEvent(player, self, playerHeldItem, armorStandItem, slot, bukkitHand);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public void onEquipItem(net.minecraft.world.entity.EquipmentSlot slotIn, ItemStack stack, boolean silent) {
        switch (slotIn.getType()) {
            case HAND ->
                this.bridge$playEquipSound(slotIn, this.handItems.set(slotIn.getIndex(), stack), stack, silent);
            case HUMANOID_ARMOR ->
                this.bridge$playEquipSound(slotIn, this.armorItems.set(slotIn.getIndex(), stack), stack, silent);
        }

    }
}
