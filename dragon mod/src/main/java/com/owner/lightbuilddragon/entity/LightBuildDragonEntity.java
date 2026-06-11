package com.owner.lightbuilddragon.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AttackWithOwnerGoal;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TrackOwnerAttackerGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.world.ServerWorldAccess;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class LightBuildDragonEntity extends TameableEntity implements GeoEntity {
    public static final double WALK_SPEED = 0.2D;
    public static final double SPRINT_SPEED = 0.6D;
    public static final double FLY_SPEED = 0.7D;
    public static final float MELEE_DAMAGE = 5.0F;

    private static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().thenLoop("Idle");
    private static final RawAnimation WALK_ANIMATION = RawAnimation.begin().thenLoop("Walk");
    private static final RawAnimation SPRINT_ANIMATION = RawAnimation.begin().thenLoop("Sprint");
    private static final RawAnimation FLY_ANIMATION = RawAnimation.begin().thenLoop("fly");
    private static final RawAnimation GLIDE_ANIMATION = RawAnimation.begin().thenLoop("Glide");
    private static final RawAnimation DIVE_ANIMATION = RawAnimation.begin().thenLoop("Dive");
    private static final RawAnimation SIT_ANIMATION = RawAnimation.begin().thenLoop("Sit");

    private static final net.minecraft.entity.data.TrackedData<Integer> DRAGON_MODE =
            net.minecraft.entity.data.DataTracker.registerData(LightBuildDragonEntity.class, net.minecraft.entity.data.TrackedDataHandlerRegistry.INTEGER);

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private boolean riderWantsUp;
    private int fireChargeCooldown;
    private int controlledMeleeCooldown;

    public LightBuildDragonEntity(EntityType<? extends TameableEntity> entityType, net.minecraft.world.World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createLightBuildDragonAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 60.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, MELEE_DAMAGE)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, WALK_SPEED)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, FLY_SPEED)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.35D);
    }

    public static boolean canSpawn(EntityType<LightBuildDragonEntity> type, ServerWorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
        BlockState ground = world.getBlockState(pos.down());
        return ground.isIn(BlockTags.SAND) && world.getBaseLightLevel(pos, 0) > 8;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(DRAGON_MODE, DragonMode.FOLLOW.ordinal());
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SitGoal(this));
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.1D, true));
        this.goalSelector.add(3, new FollowOwnerUnlessWanderingGoal(this));
        this.goalSelector.add(4, new WanderIfAllowedGoal(this));
        this.goalSelector.add(5, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(6, new LookAroundGoal(this));

        this.targetSelector.add(0, new TrackOwnerAttackerGoal(this));
        this.targetSelector.add(1, new AttackWithOwnerGoal(this));
        this.targetSelector.add(2, new RevengeGoal(this));
        this.targetSelector.add(3, new PreyTargetGoal<>(this, SheepEntity.class));
        this.targetSelector.add(3, new PreyTargetGoal<>(this, CowEntity.class));
        this.targetSelector.add(3, new PreyTargetGoal<>(this, PigEntity.class));
        this.targetSelector.add(3, new PreyTargetGoal<>(this, ChickenEntity.class));
    }

    @Override
    public void tick() {
        super.tick();
        if (fireChargeCooldown > 0) {
            fireChargeCooldown--;
        }
        if (controlledMeleeCooldown > 0) {
            controlledMeleeCooldown--;
        }
        if (getControllingPassenger() == null) {
            riderWantsUp = false;
        }
        this.setNoGravity(isControlledFlying());
    }

    @Override
    public void travel(Vec3d movementInput) {
        LivingEntity rider = getControllingPassenger();
        if (this.isAlive() && rider != null) {
            syncRotationToRider(rider);
            float sideways = rider.sidewaysSpeed * 0.5F;
            float forward = rider.forwardSpeed;
            if (forward <= 0.0F) {
                forward *= 0.25F;
            }

            if (isControlledFlying()) {
                travelControlledFlying(sideways, forward, rider.isSneaking());
            } else {
                this.setMovementSpeed((float) (rider.isSprinting() ? SPRINT_SPEED : WALK_SPEED));
                super.travel(new Vec3d(sideways, movementInput.y, forward));
            }
            return;
        }
        super.travel(movementInput);
    }

    private void syncRotationToRider(LivingEntity rider) {
        this.setYaw(rider.getYaw());
        this.prevYaw = this.getYaw();
        this.setPitch(rider.getPitch() * 0.5F);
        this.setRotation(this.getYaw(), this.getPitch());
        this.bodyYaw = this.getYaw();
        this.headYaw = this.bodyYaw;
    }

    private void travelControlledFlying(float sideways, float forward, boolean descending) {
        Vec3d horizontalInput = getMountedHorizontalInput(sideways, forward);
        double verticalSpeed = riderWantsUp ? 0.35D : descending ? -0.25D : -0.03D;

        if (horizontalInput.lengthSquared() < 1.0E-5D && !riderWantsUp && !descending) {
            Vec3d damped = this.getVelocity().multiply(0.75D, 0.6D, 0.75D);
            this.setVelocity(damped.x, -0.03D, damped.z);
        } else {
            this.setVelocity(horizontalInput.x * FLY_SPEED, verticalSpeed, horizontalInput.z * FLY_SPEED);
        }

        this.move(MovementType.SELF, this.getVelocity());
    }

    private Vec3d getMountedHorizontalInput(float sideways, float forward) {
        float yawRadians = this.getYaw() * MathHelper.RADIANS_PER_DEGREE;
        Vec3d forwardVector = new Vec3d(-MathHelper.sin(yawRadians), 0.0D, MathHelper.cos(yawRadians));
        Vec3d sidewaysVector = new Vec3d(MathHelper.cos(yawRadians), 0.0D, MathHelper.sin(yawRadians));
        Vec3d input = forwardVector.multiply(forward).add(sidewaysVector.multiply(sideways));
        return input.lengthSquared() > 1.0D ? input.normalize() : input;
    }

    private boolean isControlledFlying() {
        return getControllingPassenger() != null && (riderWantsUp || !this.isOnGround());
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        Entity passenger = this.getFirstPassenger();
        if (passenger instanceof PlayerEntity player && this.isOwner(player)) {
            return player;
        }
        return null;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (this.getWorld().isClient) {
            boolean canInteract = (!this.isTamed() && isTamingItem(stack)) || (this.isTamed() && this.isOwner(player));
            return canInteract ? ActionResult.SUCCESS : ActionResult.PASS;
        }

        if (!this.isTamed() && isTamingItem(stack)) {
            consumeTamingItem(player, stack);
            if (this.random.nextInt(3) == 0) {
                this.setOwner(player);
                this.setSitting(false);
                this.setDragonMode(DragonMode.FOLLOW);
                this.navigation.stop();
                this.setTarget(null);
                this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);
            } else {
                this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_NEGATIVE_PLAYER_REACTION_PARTICLES);
            }
            return ActionResult.CONSUME;
        }

        if (this.isTamed() && this.isOwner(player)) {
            if (stack.isOf(Items.STICK)) {
                setDragonMode(getDragonMode().next());
                player.sendMessage(Text.translatable(getDragonMode().translationKey()), true);
                return ActionResult.CONSUME;
            }

            if (player.isSneaking()) {
                boolean sitting = !this.isSitting();
                this.setSitting(sitting);
                this.navigation.stop();
                this.setTarget(null);
                player.sendMessage(Text.translatable(sitting ? "message.lightbuilddragon.sit" : "message.lightbuilddragon.stand"), true);
                return ActionResult.SUCCESS;
            }

            if (!this.hasPassengers()) {
                player.startRiding(this);
                return ActionResult.SUCCESS;
            }
        }

        return super.interactMob(player, hand);
    }

    private static boolean isTamingItem(ItemStack stack) {
        return stack.isOf(Items.COD) || stack.isOf(Items.SALMON);
    }

    private static void consumeTamingItem(PlayerEntity player, ItemStack stack) {
        if (!player.getAbilities().creativeMode) {
            stack.decrement(1);
        }
    }

    public DragonMode getDragonMode() {
        return DragonMode.byId(this.dataTracker.get(DRAGON_MODE));
    }

    public void setDragonMode(DragonMode mode) {
        this.dataTracker.set(DRAGON_MODE, mode.ordinal());
    }

    public void setRiderWantsUp(boolean riderWantsUp) {
        this.riderWantsUp = riderWantsUp;
    }

    public void shootFireCharge(PlayerEntity rider) {
        if (!this.isOwner(rider) || fireChargeCooldown > 0 || this.getWorld().isClient) {
            return;
        }

        Vec3d direction = rider.getRotationVec(1.0F).normalize();
        Vec3d spawnPos = this.getEyePos().add(direction.multiply(2.0D));
        FireballEntity fireball = new FireballEntity(this.getWorld(), this, direction.x, direction.y, direction.z, 1);
        fireball.setPosition(spawnPos.x, spawnPos.y, spawnPos.z);
        this.getWorld().spawnEntity(fireball);
        this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_GHAST_SHOOT, SoundCategory.HOSTILE, 1.0F, 1.0F);
        fireChargeCooldown = 40;
    }

    public void controlledMeleeAttack(PlayerEntity rider) {
        if (!this.isOwner(rider) || controlledMeleeCooldown > 0 || this.getWorld().isClient) {
            return;
        }

        Vec3d look = rider.getRotationVec(1.0F).normalize();
        Box attackBox = this.getBoundingBox()
                .stretch(look.x * 2.0D, look.y * 2.0D, look.z * 2.0D)
                .expand(1.0D);
        List<LivingEntity> targets = this.getWorld().getEntitiesByClass(
                LivingEntity.class,
                attackBox,
                target -> target.isAlive() && target != this && target != rider && !this.isOwner(target)
        );

        DamageSource damageSource = this.getDamageSources().mobAttack(this);
        for (LivingEntity target : targets) {
            target.damage(damageSource, MELEE_DAMAGE);
        }

        if (!targets.isEmpty()) {
            this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.0F, 0.8F);
        }
        controlledMeleeCooldown = 15;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean damaged = super.damage(source, amount);
        Entity attacker = source.getAttacker();
        if (damaged && !this.getWorld().isClient && attacker instanceof LivingEntity livingAttacker && !this.isOwner(livingAttacker)) {
            this.setTarget(livingAttacker);
        }
        return damaged;
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target != null && this.isOwner(target) ? null : target);
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        return !this.isOwner(target) && super.canTarget(target);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

    @Override
    @Nullable
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_ENDER_DRAGON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ENDER_DRAGON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ENDER_DRAGON_DEATH;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("DragonMode", getDragonMode().ordinal());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        setDragonMode(DragonMode.byId(nbt.getInt("DragonMode")));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5, this::selectAnimation));
    }

    private PlayState selectAnimation(AnimationState<LightBuildDragonEntity> state) {
        if (this.isSitting()) {
            state.getController().setAnimation(SIT_ANIMATION);
        } else if (isControlledFlying()) {
            state.getController().setAnimation(this.getVelocity().y < -0.15D ? DIVE_ANIMATION : this.getVelocity().lengthSquared() < 0.01D ? GLIDE_ANIMATION : FLY_ANIMATION);
        } else if (this.isSprinting()) {
            state.getController().setAnimation(SPRINT_ANIMATION);
        } else if (state.isMoving()) {
            state.getController().setAnimation(WALK_ANIMATION);
        } else {
            state.getController().setAnimation(IDLE_ANIMATION);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    private static final class FollowOwnerUnlessWanderingGoal extends FollowOwnerGoal {
        private final LightBuildDragonEntity dragon;

        private FollowOwnerUnlessWanderingGoal(LightBuildDragonEntity dragon) {
            super(dragon, 1.05D, 8.0F, 2.0F, false);
            this.dragon = dragon;
        }

        @Override
        public boolean canStart() {
            return dragon.getDragonMode() == DragonMode.FOLLOW && super.canStart();
        }

        @Override
        public boolean shouldContinue() {
            return dragon.getDragonMode() == DragonMode.FOLLOW && super.shouldContinue();
        }
    }

    private static final class WanderIfAllowedGoal extends WanderAroundFarGoal {
        private final LightBuildDragonEntity dragon;

        private WanderIfAllowedGoal(LightBuildDragonEntity dragon) {
            super(dragon, 1.0D);
            this.dragon = dragon;
        }

        @Override
        public boolean canStart() {
            return dragon.getDragonMode() == DragonMode.WANDER && !dragon.isSitting() && super.canStart();
        }

        @Override
        public boolean shouldContinue() {
            return dragon.getDragonMode() == DragonMode.WANDER && !dragon.isSitting() && super.shouldContinue();
        }
    }

    private static final class PreyTargetGoal<T extends AnimalEntity> extends ActiveTargetGoal<T> {
        private final LightBuildDragonEntity dragon;

        private PreyTargetGoal(LightBuildDragonEntity dragon, Class<T> targetClass) {
            super(dragon, targetClass, true);
            this.dragon = dragon;
        }

        @Override
        public boolean canStart() {
            return !dragon.isSitting() && super.canStart();
        }

        @Override
        public boolean shouldContinue() {
            return !dragon.isSitting() && super.shouldContinue();
        }
    }
}
