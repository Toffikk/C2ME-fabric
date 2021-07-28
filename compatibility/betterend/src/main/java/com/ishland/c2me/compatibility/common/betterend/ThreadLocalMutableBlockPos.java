package com.ishland.c2me.compatibility.common.betterend;

import net.minecraft.core.AxisCycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Rotation;

public class ThreadLocalMutableBlockPos extends BlockPos.MutableBlockPos {

    private final ThreadLocal<BlockPos.MutableBlockPos> delegate = ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);

    @Override
    public BlockPos offset(double d, double e, double f) {
        return delegate.get().offset(d, e, f);
    }

    @Override
    public BlockPos offset(int i, int j, int k) {
        return delegate.get().offset(i, j, k);
    }

    @Override
    public BlockPos multiply(int i) {
        return delegate.get().multiply(i);
    }

    @Override
    public BlockPos relative(Direction direction, int i) {
        return delegate.get().relative(direction, i);
    }

    @Override
    public BlockPos relative(Direction.Axis axis, int i) {
        return delegate.get().relative(axis, i);
    }

    @Override
    public BlockPos rotate(Rotation rotation) {
        return delegate.get().rotate(rotation);
    }

    @Override
    public MutableBlockPos set(int x, int y, int z) {
        return delegate.get().set(x, y, z);
    }

    @Override
    public MutableBlockPos set(double x, double y, double z) {
        return delegate.get().set(x, y, z);
    }

    @Override
    public MutableBlockPos set(Vec3i pos) {
        return delegate.get().set(pos);
    }

    @Override
    public MutableBlockPos set(long pos) {
        return delegate.get().set(pos);
    }

    @Override
    public MutableBlockPos set(AxisCycle axis, int x, int y, int z) {
        return delegate.get().set(axis, x, y, z);
    }

    @Override
    public MutableBlockPos setWithOffset(Vec3i pos, Direction direction) {
        return delegate.get().setWithOffset(pos, direction);
    }

    @Override
    public MutableBlockPos setWithOffset(Vec3i pos, int x, int y, int z) {
        return delegate.get().setWithOffset(pos, x, y, z);
    }

    @Override
    public MutableBlockPos setWithOffset(Vec3i vec1, Vec3i vec2) {
        return delegate.get().setWithOffset(vec1, vec2);
    }

    @Override
    public MutableBlockPos move(Direction direction) {
        return delegate.get().move(direction);
    }

    @Override
    public MutableBlockPos move(Direction direction, int distance) {
        return delegate.get().move(direction, distance);
    }

    @Override
    public MutableBlockPos move(int dx, int dy, int dz) {
        return delegate.get().move(dx, dy, dz);
    }

    @Override
    public MutableBlockPos move(Vec3i vec) {
        return delegate.get().move(vec);
    }

    @Override
    public MutableBlockPos clamp(Direction.Axis axis, int min, int max) {
        return delegate.get().clamp(axis, min, max);
    }

    @Override
    public MutableBlockPos setX(int i) {
        return delegate.get().setX(i);
    }

    @Override
    public MutableBlockPos setY(int i) {
        return delegate.get().setY(i);
    }

    @Override
    public MutableBlockPos setZ(int i) {
        return delegate.get().setZ(i);
    }

    @Override
    public BlockPos immutable() {
        return delegate.get().immutable();
    }

    @Override
    public long asLong() {
        return delegate.get().asLong();
    }

    @Override
    public BlockPos offset(Vec3i vec3i) {
        return delegate.get().offset(vec3i);
    }

    @Override
    public BlockPos subtract(Vec3i vec3i) {
        return delegate.get().subtract(vec3i);
    }

    @Override
    public BlockPos above() {
        return delegate.get().above();
    }

    @Override
    public BlockPos above(int distance) {
        return delegate.get().above(distance);
    }

    @Override
    public BlockPos below() {
        return delegate.get().below();
    }

    @Override
    public BlockPos below(int i) {
        return delegate.get().below(i);
    }

    @Override
    public BlockPos north() {
        return delegate.get().north();
    }

    @Override
    public BlockPos north(int distance) {
        return delegate.get().north(distance);
    }

    @Override
    public BlockPos south() {
        return delegate.get().south();
    }

    @Override
    public BlockPos south(int distance) {
        return delegate.get().south(distance);
    }

    @Override
    public BlockPos west() {
        return delegate.get().west();
    }

    @Override
    public BlockPos west(int distance) {
        return delegate.get().west(distance);
    }

    @Override
    public BlockPos east() {
        return delegate.get().east();
    }

    @Override
    public BlockPos east(int distance) {
        return delegate.get().east(distance);
    }

    @Override
    public BlockPos relative(Direction direction) {
        return delegate.get().relative(direction);
    }

    @Override
    public BlockPos cross(Vec3i pos) {
        return delegate.get().cross(pos);
    }

    @Override
    public BlockPos atY(int y) {
        return delegate.get().atY(y);
    }

    @Override
    public MutableBlockPos mutable() {
        return delegate.get().mutable();
    }

    @Override
    public boolean equals(Object o) {
        return delegate.get().equals(o);
    }

    @Override
    public int hashCode() {
        return delegate.get().hashCode();
    }

    @Override
    public int compareTo(Vec3i vec3i) {
        return delegate.get().compareTo(vec3i);
    }

    @Override
    public int getX() {
        return delegate.get().getX();
    }

    @Override
    public int getY() {
        return delegate.get().getY();
    }

    @Override
    public int getZ() {
        return delegate.get().getZ();
    }

    @Override
    public boolean closerThan(Vec3i vec, double distance) {
        return delegate.get().closerThan(vec, distance);
    }

    @Override
    public boolean closerThan(Position pos, double distance) {
        return delegate.get().closerThan(pos, distance);
    }

    @Override
    public double distSqr(Vec3i vec) {
        return delegate.get().distSqr(vec);
    }

    @Override
    public double distSqr(Position pos, boolean treatAsBlockPos) {
        return delegate.get().distSqr(pos, treatAsBlockPos);
    }

    @Override
    public double distSqr(Vec3i vec, boolean treatAsBlockPos) {
        return delegate.get().distSqr(vec, treatAsBlockPos);
    }

    @Override
    public double distSqr(double x, double y, double z, boolean treatAsBlockPos) {
        return delegate.get().distSqr(x, y, z, treatAsBlockPos);
    }

    @Override
    public int distManhattan(Vec3i vec) {
        return delegate.get().distManhattan(vec);
    }

    @Override
    public int get(Direction.Axis axis) {
        return delegate.get().get(axis);
    }

    @Override
    public String toString() {
        return delegate.get().toString();
    }

    @Override
    public String toShortString() {
        return delegate.get().toShortString();
    }

}
