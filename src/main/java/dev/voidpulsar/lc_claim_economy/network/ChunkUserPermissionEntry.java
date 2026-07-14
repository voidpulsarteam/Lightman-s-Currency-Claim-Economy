package dev.voidpulsar.lc_claim_economy.network;

import java.util.UUID;

public record ChunkUserPermissionEntry(UUID playerId, String displayName, int flags) {
}
