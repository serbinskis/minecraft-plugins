package net.minecraft.advancements;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.util.ExtraCodecs;

public class AdvancementProgress implements Comparable<AdvancementProgress> {

    private static final DateTimeFormatter OBTAINED_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT);
    private static final Codec<Instant> OBTAINED_TIME_CODEC = ExtraCodecs.temporalCodec(AdvancementProgress.OBTAINED_TIME_FORMAT).xmap(Instant::from, (instant) -> {
        return instant.atZone(ZoneId.systemDefault());
    });
    private static final Codec<Map<String, CriterionProgress>> CRITERIA_CODEC = Codec.unboundedMap(Codec.STRING, AdvancementProgress.OBTAINED_TIME_CODEC).xmap((map) -> {
        return (Map) map.entrySet().stream().collect(Collectors.toMap(Entry::getKey, (entry) -> {
            return new CriterionProgress((Instant) entry.getValue());
        }));
    }, (map) -> {
        return (Map) map.entrySet().stream().filter((entry) -> {
            return ((CriterionProgress) entry.getValue()).isDone();
        }).collect(Collectors.toMap(Entry::getKey, (entry) -> {
            return (Instant) Objects.requireNonNull(((CriterionProgress) entry.getValue()).getObtained());
        }));
    });
    public static final Codec<AdvancementProgress> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(AdvancementProgress.CRITERIA_CODEC.optionalFieldOf("criteria", Map.of()).forGetter((advancementprogress) -> {
            return advancementprogress.criteria;
        }), Codec.BOOL.fieldOf("done").orElse(true).forGetter(AdvancementProgress::isDone)).apply(instance, (map, obool) -> {
            return new AdvancementProgress(new HashMap(map));
        });
    });
    private final Map<String, CriterionProgress> criteria;
    private AdvancementRequirements requirements;

    private AdvancementProgress(Map<String, CriterionProgress> map) {
        this.requirements = AdvancementRequirements.EMPTY;
        this.criteria = map;
    }

    public AdvancementProgress() {
        this.requirements = AdvancementRequirements.EMPTY;
        this.criteria = Maps.newHashMap();
    }

    public void update(AdvancementRequirements advancementrequirements) {
        Set<String> set = advancementrequirements.names();

        this.criteria.entrySet().removeIf((entry) -> {
            return !set.contains(entry.getKey());
        });
        Iterator iterator = set.iterator();

        while (iterator.hasNext()) {
            String s = (String) iterator.next();

            this.criteria.putIfAbsent(s, new CriterionProgress());
        }

        this.requirements = advancementrequirements;
    }

    public boolean isDone() {
        return this.requirements.test(this::isCriterionDone);
    }

    public boolean hasProgress() {
        Iterator iterator = this.criteria.values().iterator();

        CriterionProgress criterionprogress;

        do {
            if (!iterator.hasNext()) {
                return false;
            }

            criterionprogress = (CriterionProgress) iterator.next();
        } while (!criterionprogress.isDone());

        return true;
    }

    public boolean grantProgress(String s) {
        CriterionProgress criterionprogress = (CriterionProgress) this.criteria.get(s);

        if (criterionprogress != null && !criterionprogress.isDone()) {
            criterionprogress.grant();
            return true;
        } else {
            return false;
        }
    }

    public boolean revokeProgress(String s) {
        CriterionProgress criterionprogress = (CriterionProgress) this.criteria.get(s);

        if (criterionprogress != null && criterionprogress.isDone()) {
            criterionprogress.revoke();
            return true;
        } else {
            return false;
        }
    }

    public String toString() {
        String s = String.valueOf(this.criteria);

        return "AdvancementProgress{criteria=" + s + ", requirements=" + String.valueOf(this.requirements) + "}";
    }

    public void serializeToNetwork(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeMap(this.criteria, PacketDataSerializer::writeUtf, (packetdataserializer1, criterionprogress) -> {
            criterionprogress.serializeToNetwork(packetdataserializer1);
        });
    }

    public static AdvancementProgress fromNetwork(PacketDataSerializer packetdataserializer) {
        Map<String, CriterionProgress> map = packetdataserializer.readMap(PacketDataSerializer::readUtf, CriterionProgress::fromNetwork);

        return new AdvancementProgress(map);
    }

    @Nullable
    public CriterionProgress getCriterion(String s) {
        return (CriterionProgress) this.criteria.get(s);
    }

    private boolean isCriterionDone(String s) {
        CriterionProgress criterionprogress = this.getCriterion(s);

        return criterionprogress != null && criterionprogress.isDone();
    }

    public float getPercent() {
        if (this.criteria.isEmpty()) {
            return 0.0F;
        } else {
            float f = (float) this.requirements.size();
            float f1 = (float) this.countCompletedRequirements();

            return f1 / f;
        }
    }

    @Nullable
    public IChatBaseComponent getProgressText() {
        if (this.criteria.isEmpty()) {
            return null;
        } else {
            int i = this.requirements.size();

            if (i <= 1) {
                return null;
            } else {
                int j = this.countCompletedRequirements();

                return IChatBaseComponent.translatable("advancements.progress", j, i);
            }
        }
    }

    private int countCompletedRequirements() {
        return this.requirements.count(this::isCriterionDone);
    }

    public Iterable<String> getRemainingCriteria() {
        List<String> list = Lists.newArrayList();
        Iterator iterator = this.criteria.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<String, CriterionProgress> entry = (Entry) iterator.next();

            if (!((CriterionProgress) entry.getValue()).isDone()) {
                list.add((String) entry.getKey());
            }
        }

        return list;
    }

    public Iterable<String> getCompletedCriteria() {
        List<String> list = Lists.newArrayList();
        Iterator iterator = this.criteria.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<String, CriterionProgress> entry = (Entry) iterator.next();

            if (((CriterionProgress) entry.getValue()).isDone()) {
                list.add((String) entry.getKey());
            }
        }

        return list;
    }

    @Nullable
    public Instant getFirstProgressDate() {
        return (Instant) this.criteria.values().stream().map(CriterionProgress::getObtained).filter(Objects::nonNull).min(Comparator.naturalOrder()).orElse((Object) null);
    }

    public int compareTo(AdvancementProgress advancementprogress) {
        Instant instant = this.getFirstProgressDate();
        Instant instant1 = advancementprogress.getFirstProgressDate();

        return instant == null && instant1 != null ? 1 : (instant != null && instant1 == null ? -1 : (instant == null && instant1 == null ? 0 : instant.compareTo(instant1)));
    }
}
