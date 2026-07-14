package dev.voidpulsar.lc_claim_economy.service;

import dev.ftb.mods.ftbchunks.api.Protection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;

public final class ChunkPermissionFlags {
    public static final int BLOCK_EDIT = 1;
    public static final int BLOCK_INTERACT = 1 << 1;
    public static final int ENTITY_INTERACT = 1 << 2;
    public static final int PVP = 1 << 3;

    public static final int ALL = BLOCK_EDIT | BLOCK_INTERACT | ENTITY_INTERACT | PVP;

    private static final Map<Protection, Integer> FLAGS_BY_PROTECTION = new IdentityHashMap<>();

    static {
        register("EDIT_BLOCK", BLOCK_EDIT);
        register("INTERACT_BLOCK", BLOCK_INTERACT);
        register("EDIT_AND_INTERACT_BLOCK", BLOCK_EDIT | BLOCK_INTERACT);
        register("EDIT_FLUID", BLOCK_EDIT);
        register("INTERACT_ENTITY", ENTITY_INTERACT);
        register("ATTACK_NONLIVING_ENTITY", PVP);
    }

    private ChunkPermissionFlags() {
    }

    private static void register(String fieldName, int flags) {
        try {
            Field field = Protection.class.getField(fieldName);
            Object value = field.get(null);
            if (value instanceof Protection protection) {
                FLAGS_BY_PROTECTION.put(protection, flags);
            }
        } catch (Throwable ignored) {
        }
    }

    public static int sanitize(int flags) {
        return flags & ALL;
    }

    public static int fromProtection(Protection protection) {
        if (protection == null) {
            return 0;
        }

        Integer direct = FLAGS_BY_PROTECTION.get(protection);
        if (direct != null) {
            return direct;
        }

        String fieldName = resolveFieldName(protection);
        if (fieldName != null) {
            String id = fieldName.toUpperCase(Locale.ROOT);
            if (id.contains("EDIT") && id.contains("INTERACT") && id.contains("BLOCK")) {
                return BLOCK_EDIT | BLOCK_INTERACT;
            }
            if (id.contains("INTERACT") && id.contains("BLOCK")) {
                return BLOCK_INTERACT;
            }
            if ((id.contains("EDIT") || id.contains("BREAK") || id.contains("PLACE")) && (id.contains("BLOCK") || id.contains("FLUID"))) {
                return BLOCK_EDIT;
            }
            if (id.contains("INTERACT") && id.contains("ENTITY")) {
                return ENTITY_INTERACT;
            }
            if (id.contains("PVP") || id.contains("ATTACK")) {
                return PVP;
            }
        }

        String name = String.valueOf(protection).toUpperCase(Locale.ROOT);
        if (name.contains("PVP") || name.contains("ATTACK")) {
            return PVP;
        }
        if (name.contains("BLOCK") && name.contains("INTERACT")) {
            return BLOCK_INTERACT;
        }
        if (name.contains("BLOCK") && (name.contains("EDIT") || name.contains("BREAK") || name.contains("PLACE"))) {
            return BLOCK_EDIT;
        }
        if (name.contains("ENTITY") && name.contains("INTERACT")) {
            return ENTITY_INTERACT;
        }
        return 0;
    }

    private static String resolveFieldName(Protection protection) {
        try {
            for (Field field : Protection.class.getFields()) {
                if (field.getType() == Protection.class && Modifier.isStatic(field.getModifiers())) {
                    Object value = field.get(null);
                    if (value == protection) {
                        return field.getName();
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }
}
