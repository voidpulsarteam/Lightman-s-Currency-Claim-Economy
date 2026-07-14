package dev.voidpulsar.lc_claim_economy.client;

import dev.voidpulsar.lc_claim_economy.network.ChunkUserPermissionEntry;

import java.util.Comparator;
import java.util.List;

public final class ClientChunkUserPermissions {
    private static String activeChunkKey = "";
    private static boolean canManage;
    private static List<ChunkUserPermissionEntry> entries = List.of();

    private ClientChunkUserPermissions() {
    }

    public static void update(String chunkKey, boolean manage, List<ChunkUserPermissionEntry> list) {
        activeChunkKey = chunkKey == null ? "" : chunkKey;
        canManage = manage;
        entries = list.stream()
                .sorted(Comparator.comparing(ChunkUserPermissionEntry::displayName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public static String activeChunkKey() {
        return activeChunkKey;
    }

    public static boolean canManage() {
        return canManage;
    }

    public static List<ChunkUserPermissionEntry> entries() {
        return entries;
    }
}
