package dev.voidpulsar.lc_claim_economy;

import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Validates that companion mods match the versions this hook was built against.
 */
public final class ModCompatibility {
    /** Must match {@code ftb_chunks_version} in gradle.properties. */
    public static final String REQUIRED_FTB_CHUNKS_VERSION = "2101.1.20";
    /** Must match {@code opc_version} in gradle.properties. */
    public static final String REQUIRED_OPC_VERSION = "0.27.5";
    /** Must match {@code lightmanscurrency_version} in gradle.properties. */
    public static final String REQUIRED_LIGHTMANS_CURRENCY_VERSION = "1.21-2.3.0.5";

    private ModCompatibility() {
    }

    public static void validateOrThrow() {
        List<String> errors = new ArrayList<>();
        requireExactVersion("lightmanscurrency", "Lightman's Currency", REQUIRED_LIGHTMANS_CURRENCY_VERSION, errors);

        boolean ftbPresent = ModList.get().isLoaded("ftbchunks");
        boolean opcPresent = ModList.get().isLoaded("openpartiesandclaims");

        if (!ftbPresent && !opcPresent) {
            errors.add(
                    "No supported claim backend found. Install either FTB Chunks + FTB Teams + FTB Library, "
                            + "or Open Parties and Claims."
            );
        } else if (ftbPresent) {
            requireExactVersion("ftbchunks", "FTB Chunks", REQUIRED_FTB_CHUNKS_VERSION, errors);
        } else {
            requireExactVersion("openpartiesandclaims", "Open Parties and Claims", REQUIRED_OPC_VERSION, errors);
        }

        if (errors.isEmpty()) {
            return;
        }

        for (String error : errors) {
            LcClaimEconomy.LOGGER.error(error);
        }

        throw new IllegalStateException(
                "Lightman's Currency: FTB Claim Economy cannot load with incompatible mod versions. "
                        + "Details: "
                        + String.join(" | ", errors)
        );
    }

    private static void requireExactVersion(
            String modId,
            String displayName,
            String requiredVersion,
            List<String> errorsOut
    ) {
        Optional<String> installed = ModList.get()
                .getModContainerById(modId)
                .map(container -> container.getModInfo().getVersion().toString());
        if (installed.isEmpty()) {
            errorsOut.add(displayName + " (" + modId + ") is missing.");
            return;
        }

        if (!requiredVersion.equals(installed.get())) {
            errorsOut.add(displayName + " version mismatch: required " + requiredVersion + ", found " + installed.get() + ".");
        }
    }
}
