package dev.voidpulsar.lc_claim_economy.service;

import dev.ftb.mods.ftbchunks.api.Protection;

public final class ChunkPermissionFlags {
    public static final int BLOCK_EDIT = 1;
    public static final int BLOCK_INTERACT = 1 << 1;
    public static final int ENTITY_INTERACT = 1 << 2;
    public static final int PVP = 1 << 3;

    public static final int ALL = BLOCK_EDIT | BLOCK_INTERACT | ENTITY_INTERACT | PVP;

    private ChunkPermissionFlags() {
    }

    public static int sanitize(int flags) {
        return flags & ALL;
    }

    public static int fromProtection(Protection protection) {
        if (protection == null) {
            return 0;
        }

        String name = String.valueOf(protection).toUpperCase(java.util.Locale.ROOT);
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
}
