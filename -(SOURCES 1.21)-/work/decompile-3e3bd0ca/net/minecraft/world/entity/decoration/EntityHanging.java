package net.minecraft.world.entity.decoration;

import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockDiodeAbstract;
import net.minecraft.world.level.block.EnumBlockMirror;
import net.minecraft.world.level.block.EnumBlockRotation;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;
import org.apache.commons.lang3.Validate;

public abstract class EntityHanging extends BlockAttachedEntity {

    protected static final Predicate<Entity> HANGING_ENTITY = (entity) -> {
        return entity instanceof EntityHanging;
    };
    protected EnumDirection direction;

    protected EntityHanging(EntityTypes<? extends EntityHanging> entitytypes, World world) {
        super(entitytypes, world);
        this.direction = EnumDirection.SOUTH;
    }

    protected EntityHanging(EntityTypes<? extends EntityHanging> entitytypes, World world, BlockPosition blockposition) {
        this(entitytypes, world);
        this.pos = blockposition;
    }

    public void setDirection(EnumDirection enumdirection) {
        Objects.requireNonNull(enumdirection);
        Validate.isTrue(enumdirection.getAxis().isHorizontal());
        this.direction = enumdirection;
        this.setYRot((float) (this.direction.get2DDataValue() * 90));
        this.yRotO = this.getYRot();
        this.recalculateBoundingBox();
    }

    @Override
    protected final void recalculateBoundingBox() {
        if (this.direction != null) {
            AxisAlignedBB axisalignedbb = this.calculateBoundingBox(this.pos, this.direction);
            Vec3D vec3d = axisalignedbb.getCenter();

            this.setPosRaw(vec3d.x, vec3d.y, vec3d.z);
            this.setBoundingBox(axisalignedbb);
        }
    }

    protected abstract AxisAlignedBB calculateBoundingBox(BlockPosition blockposition, EnumDirection enumdirection);

    @Override
    public boolean survives() {
        if (!this.level().noCollision((Entity) this)) {
            return false;
        } else {
            boolean flag = BlockPosition.betweenClosedStream(this.calculateSupportBox()).allMatch((blockposition) -> {
                IBlockData iblockdata = this.level().getBlockState(blockposition);

                return iblockdata.isSolid() || BlockDiodeAbstract.isDiode(iblockdata);
            });

            return !flag ? false : this.level().getEntities((Entity) this, this.getBoundingBox(), EntityHanging.HANGING_ENTITY).isEmpty();
        }
    }

    protected AxisAlignedBB calculateSupportBox() {
        return this.getBoundingBox().move(this.direction.step().mul(-0.5F)).deflate(1.0E-7D);
    }

    @Override
    public EnumDirection getDirection() {
        return this.direction;
    }

    public abstract void playPlacementSound();

    @Override
    public EntityItem spawnAtLocation(ItemStack itemstack, float f) {
        EntityItem entityitem = new EntityItem(this.level(), this.getX() + (double) ((float) this.direction.getStepX() * 0.15F), this.getY() + (double) f, this.getZ() + (double) ((float) this.direction.getStepZ() * 0.15F), itemstack);

        entityitem.setDefaultPickUpDelay();
        this.level().addFreshEntity(entityitem);
        return entityitem;
    }

    @Override
    public float rotate(EnumBlockRotation enumblockrotation) {
        if (this.direction.getAxis() != EnumDirection.EnumAxis.Y) {
            switch (enumblockrotation) {
                case CLOCKWISE_180:
                    this.direction = this.direction.getOpposite();
                    break;
                case COUNTERCLOCKWISE_90:
                    this.direction = this.direction.getCounterClockWise();
                    break;
                case CLOCKWISE_90:
                    this.direction = this.direction.getClockWise();
            }
        }

        float f = MathHelper.wrapDegrees(this.getYRot());
        float f1;

        switch (enumblockrotation) {
            case CLOCKWISE_180:
                f1 = f + 180.0F;
                break;
            case COUNTERCLOCKWISE_90:
                f1 = f + 90.0F;
                break;
            case CLOCKWISE_90:
                f1 = f + 270.0F;
                break;
            default:
                f1 = f;
        }

        return f1;
    }

    @Override
    public float mirror(EnumBlockMirror enumblockmirror) {
        return this.rotate(enumblockmirror.getRotation(this.direction));
    }
}
