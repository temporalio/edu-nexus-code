# Sandbox image for the Kotlin Nexus workshop.
# Build context is kotlin/ so this can COPY decouple-monolith/.
#
#   docker buildx build --platform linux/amd64 \
#     -f sandbox/Dockerfile -t ghcr.io/nadvolod/edu-nexus-kotlin-sandbox:latest --push .
#
# linux/amd64 is required. An arm64 image (the Apple Silicon default) fails to
# start on Instruqt with an empty log and no error.

FROM eclipse-temurin:21-jdk-jammy

RUN apt-get update && apt-get install -y --no-install-recommends \
      curl wget git vim nano less unzip ca-certificates bash-completion jq procps \
    && rm -rf /var/lib/apt/lists/*

# --- Temporal CLI -------------------------------------------------------------
ARG TEMPORAL_CLI_VERSION=1.7.2
RUN curl -sSL \
      "https://github.com/temporalio/cli/releases/download/v${TEMPORAL_CLI_VERSION}/temporal_cli_${TEMPORAL_CLI_VERSION}_linux_amd64.tar.gz" \
    | tar -xz -C /usr/local/bin temporal \
    && chmod +x /usr/local/bin/temporal

# --- code-server (VS Code in the browser) -------------------------------------
# The native Instruqt `type: code` tab is "a simple code editor" with no
# documented language support, and this is a Kotlin workshop. code-server gives
# real VS Code with Kotlin syntax highlighting.
#
# mathiasfrohlich.kotlin is syntax highlighting only: no JVM language server, so
# no Gradle indexing wait and no second JVM competing with the two Workers.
# Consequence: no autocomplete and no error squiggles. Assignment text must not
# tell learners to rely on either.
ARG CODE_SERVER_VERSION=4.96.4
RUN curl -fsSL https://code-server.dev/install.sh | sh -s -- --version ${CODE_SERVER_VERSION}
RUN code-server --extensions-dir /root/.cs-extensions \
      --install-extension mathiasfrohlich.kotlin

# Seed settings into BOTH user-data-dirs. This is load-bearing, not cosmetic.
#
# security.workspace.trust.enabled MUST be false. Workspace Trust is on by
# default, and code-server renders a blank page instead of the workbench until
# the folder is trusted. That was the cause of the editor tabs showing white.
# Copied from temporal-ai-agents-python-v4, the one Temporal track known to run
# code-server through Instruqt's proxy successfully.
#
# Each instance needs its own copy because each runs with its own
# --user-data-dir, which is what stops them sharing a last-opened workspace.
RUN for d in /root/.cs-exercise /root/.cs-solution; do \
      mkdir -p "$d/User" && printf '%s\n' \
        '{' \
        '    "security.workspace.trust.enabled": false,' \
        '    "workbench.colorTheme": "Default Dark+",' \
        '    "workbench.startupEditor": "none",' \
        '    "editor.fontSize": 14,' \
        '    "editor.tabSize": 4,' \
        '    "telemetry.telemetryLevel": "off"' \
        '}' > "$d/User/settings.json"; \
    done

# --- Architecture diagram -----------------------------------------------------
# Served by jwebserver (ships with the JDK, so no extra package). Lives outside
# the challenge folders on purpose: Instruqt parses every file inside a challenge
# directory as a lifecycle script.
COPY diagrams/ /opt/diagrams/

# --- Bake the workshop source -------------------------------------------------
# exercise/ and solution/ each carry their own Gradle wrapper.
COPY decouple-monolith/ /opt/workshop/

# --- Pre-warm Gradle ----------------------------------------------------------
# Downloads the Gradle distribution, resolves the Temporal SDK, and compiles both
# trees so the first ./gradlew in the lab is fast. Compiling here also means a
# broken port fails the image build instead of the workshop.
#
# Both trees compile. The exercise's TODO(...) bodies return Nothing, so they
# type-check and fail at runtime instead. Keep these strict: a compile failure
# here is a real defect, not an expected state.
WORKDIR /opt/workshop/solution
RUN ./gradlew --no-daemon compileKotlin

WORKDIR /opt/workshop/exercise
RUN ./gradlew --no-daemon compileKotlin

# Drop build output. The Gradle caches in /root/.gradle are what we want to keep.
RUN rm -rf /opt/workshop/solution/build /opt/workshop/exercise/build \
           /opt/workshop/solution/.gradle /opt/workshop/exercise/.gradle

# --- Shell environment --------------------------------------------------------
RUN echo 'export TEMPORAL_ADDRESS="${TEMPORAL_ADDRESS:-127.0.0.1:7233}"' >> /root/.bashrc \
    && echo 'export WORKSHOP_REPO=/root/workshop' >> /root/.bashrc

WORKDIR /root
CMD ["/bin/bash"]
