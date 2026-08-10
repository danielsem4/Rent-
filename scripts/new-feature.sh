#!/usr/bin/env bash
#
# new-feature.sh — scaffold a new feature module split into the three
# Clean-Architecture layers (domain / data / presentation).
#
# Usage:
#   ./scripts/new-feature.sh <name>
#
# Example:
#   ./scripts/new-feature.sh auth
#     -> feature/auth/domain, feature/auth/data, feature/auth/presentation
#        + include(...) lines added to settings.gradle.kts
#
# Each layer gets a thin, catalog-driven build.gradle.kts (the convention
# plugins do the heavy lifting) and a Placeholder.kt so the commonMain source
# set exists. Fill in real code afterwards.

set -euo pipefail

# --- resolve repo root (works from any cwd) ---------------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# --- args -------------------------------------------------------------------
if [[ $# -ne 1 ]]; then
    echo "Usage: $0 <name>" >&2
    echo "  <name> = feature name, lowercase (e.g. auth, booking, profile)" >&2
    exit 1
fi

NAME="$1"

if [[ ! "$NAME" =~ ^[a-z][a-z0-9]*$ ]]; then
    echo "Error: feature name must be lowercase alphanumeric and start with a letter (got '$NAME')." >&2
    exit 1
fi

FEATURE_DIR="$ROOT/feature/$NAME"
PKG_PATH="org/example/rent/$NAME"
SETTINGS="$ROOT/settings.gradle.kts"

if [[ -e "$FEATURE_DIR" ]]; then
    echo "Error: $FEATURE_DIR already exists — aborting so nothing is clobbered." >&2
    exit 1
fi

# --- helper: write a Placeholder.kt for a layer -----------------------------
write_placeholder() {
    local layer="$1"
    local dir="$FEATURE_DIR/$layer/src/commonMain/kotlin/$PKG_PATH/$layer"
    mkdir -p "$dir"
    cat > "$dir/Placeholder.kt" <<EOF
package org.example.rent.$NAME.$layer

// Placeholder so the commonMain source set exists.
// Replace with real ${layer}-layer code for the "$NAME" feature.
EOF
}

# --- domain layer -----------------------------------------------------------
write_placeholder "domain"
cat > "$FEATURE_DIR/domain/build.gradle.kts" <<EOF
plugins {
    alias(libs.plugins.convention.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
        }
    }
}
EOF

# --- data layer -------------------------------------------------------------
write_placeholder "data"
cat > "$FEATURE_DIR/data/build.gradle.kts" <<EOF
plugins {
    alias(libs.plugins.convention.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.data)
            implementation(projects.feature.$NAME.domain)
            implementation(libs.bundles.ktor.common)
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}
EOF

# --- presentation layer -----------------------------------------------------
# convention.cmp.feature auto-wires core:presentation, core:designsystem, Koin,
# lifecycle, navigation AND the compose runtime/resources/uiToolingPreview — so
# none of those are repeated here.
write_placeholder "presentation"
cat > "$FEATURE_DIR/presentation/build.gradle.kts" <<EOF
plugins {
    alias(libs.plugins.convention.cmp.feature)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.feature.$NAME.domain)
        }
    }
}
EOF

# --- register in settings.gradle.kts ----------------------------------------
added_any=0
for layer in domain data presentation; do
    line="include(\":feature:$NAME:$layer\")"
    if grep -qF "$line" "$SETTINGS"; then
        echo "settings.gradle.kts already contains: $line (skipped)"
    else
        printf '%s\n' "$line" >> "$SETTINGS"
        added_any=1
    fi
done

# --- summary ----------------------------------------------------------------
echo
echo "Created feature '$NAME':"
echo "  feature/$NAME/domain        (convention.kmp.library)"
echo "  feature/$NAME/data          (convention.kmp.library + ktor + koin)"
echo "  feature/$NAME/presentation  (convention.cmp.feature)"
if [[ "$added_any" -eq 1 ]]; then
    echo "  + include(...) lines added to settings.gradle.kts"
fi
echo
echo "Next steps:"
echo "  - Sync Gradle, then flesh out each layer's package (replace Placeholder.kt)."
echo "  - Verify:  ./gradlew projects   (should list :feature:$NAME:{domain,data,presentation})"
