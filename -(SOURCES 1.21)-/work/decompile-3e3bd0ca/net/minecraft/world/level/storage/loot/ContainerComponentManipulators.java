package net.minecraft.world.level.storage.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.ItemContainerContents;

public interface ContainerComponentManipulators {

    ContainerComponentManipulator<ItemContainerContents> CONTAINER = new ContainerComponentManipulator<ItemContainerContents>() {
        @Override
        public DataComponentType<ItemContainerContents> type() {
            return DataComponents.CONTAINER;
        }

        public Stream<ItemStack> getContents(ItemContainerContents itemcontainercontents) {
            return itemcontainercontents.stream();
        }

        @Override
        public ItemContainerContents empty() {
            return ItemContainerContents.EMPTY;
        }

        public ItemContainerContents setContents(ItemContainerContents itemcontainercontents, Stream<ItemStack> stream) {
            return ItemContainerContents.fromItems(stream.toList());
        }
    };
    ContainerComponentManipulator<BundleContents> BUNDLE_CONTENTS = new ContainerComponentManipulator<BundleContents>() {
        @Override
        public DataComponentType<BundleContents> type() {
            return DataComponents.BUNDLE_CONTENTS;
        }

        @Override
        public BundleContents empty() {
            return BundleContents.EMPTY;
        }

        public Stream<ItemStack> getContents(BundleContents bundlecontents) {
            return bundlecontents.itemCopyStream();
        }

        public BundleContents setContents(BundleContents bundlecontents, Stream<ItemStack> stream) {
            BundleContents.a bundlecontents_a = (new BundleContents.a(bundlecontents)).clearItems();

            Objects.requireNonNull(bundlecontents_a);
            stream.forEach(bundlecontents_a::tryInsert);
            return bundlecontents_a.toImmutable();
        }
    };
    ContainerComponentManipulator<ChargedProjectiles> CHARGED_PROJECTILES = new ContainerComponentManipulator<ChargedProjectiles>() {
        @Override
        public DataComponentType<ChargedProjectiles> type() {
            return DataComponents.CHARGED_PROJECTILES;
        }

        @Override
        public ChargedProjectiles empty() {
            return ChargedProjectiles.EMPTY;
        }

        public Stream<ItemStack> getContents(ChargedProjectiles chargedprojectiles) {
            return chargedprojectiles.getItems().stream();
        }

        public ChargedProjectiles setContents(ChargedProjectiles chargedprojectiles, Stream<ItemStack> stream) {
            return ChargedProjectiles.of(stream.toList());
        }
    };
    Map<DataComponentType<?>, ContainerComponentManipulator<?>> ALL_MANIPULATORS = (Map) Stream.of(ContainerComponentManipulators.CONTAINER, ContainerComponentManipulators.BUNDLE_CONTENTS, ContainerComponentManipulators.CHARGED_PROJECTILES).collect(Collectors.toMap(ContainerComponentManipulator::type, (containercomponentmanipulator) -> {
        return containercomponentmanipulator;
    }));
    Codec<ContainerComponentManipulator<?>> CODEC = BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec().comapFlatMap((datacomponenttype) -> {
        ContainerComponentManipulator<?> containercomponentmanipulator = (ContainerComponentManipulator) ContainerComponentManipulators.ALL_MANIPULATORS.get(datacomponenttype);

        return containercomponentmanipulator != null ? DataResult.success(containercomponentmanipulator) : DataResult.error(() -> {
            return "No items in component";
        });
    }, ContainerComponentManipulator::type);
}
