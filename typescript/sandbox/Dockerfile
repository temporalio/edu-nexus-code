# Sandbox image for the TypeScript Nexus workshop.
# Build context is typescript/ so this can COPY decouple-monolith/.
#
#   docker buildx build --platform linux/amd64 \
#     -f sandbox/Dockerfile -t ghcr.io/nadvolod/edu-nexus-typescript-sandbox:latest --push .
#
# linux/amd64 is required. An arm64 image (the Apple Silicon default) fails to
# start on Instruqt with an empty log and no error.

FROM node:22-bookworm

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
# documented language support. code-server gives real VS Code.
#
# No language extension is installed, deliberately: TypeScript support is built
# into VS Code. The language service is wanted here — it is what surfaces the
# compile error in challenge 2 as a red squiggle, live, which is the lesson.
ARG CODE_SERVER_VERSION=4.96.4
RUN curl -fsSL https://code-server.dev/install.sh | sh -s -- --version ${CODE_SERVER_VERSION}

# Seed settings into BOTH user-data-dirs. This is load-bearing, not cosmetic.
#
# security.workspace.trust.enabled MUST be false. Workspace Trust is on by
# default, and code-server renders a blank page instead of the workbench until
# the folder is trusted. That was the cause of the editor tabs showing white.
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
        '    "editor.tabSize": 2,' \
        '    "telemetry.telemetryLevel": "off",' \
        '    "files.autoSave": "afterDelay",' \
        '    "files.autoSaveDelay": 300,' \
        '    "typescript.tsdk": "/opt/workshop/exercise/node_modules/typescript/lib",' \
        '    "typescript.disableAutomaticTypeAcquisition": true,' \
        '    "typescript.inlayHints.parameterNames.enabled": "literals",' \
        '    "typescript.inlayHints.functionLikeReturnTypes.enabled": true,' \
        '    "typescript.inlayHints.propertyDeclarationTypes.enabled": true,' \
        '    "typescript.preferences.importModuleSpecifier": "relative",' \
        '    "javascript.suggestionActions.enabled": false,' \
        '    "editor.stickyScroll.enabled": true,' \
        '    "editor.minimap.enabled": false,' \
        '    "editor.inlayHints.fontSize": 11,' \
        '    "workbench.editor.enablePreview": false,' \
        '    "explorer.compactFolders": false,' \
        '    "files.exclude": { "**/node_modules": true, "**/package-lock.json": true },' \
        '    "errorLens.enabledDiagnosticLevels": ["error", "warning"],' \
        '    "errorLens.messageMaxChars": 0' \
        '}' > "$d/User/settings.json"; \
    done

# --- Editor extensions ---------------------------------------------------------
# Error Lens renders diagnostics inline at the end of the offending line instead of
# only underlining them. That is not decoration here: challenge 2 ends on a
# deliberate compile error, and the whole lesson is that the contract is enforced
# before anything runs. An error the learner has to hover to read is a lesson
# half-delivered.
#
# code-server resolves extensions from Open VSX, not the VS Code marketplace.
# Installed into the default extensions dir so the runtime invocation needs no
# --extensions-dir flag.
RUN code-server --install-extension usernamehw.errorlens || \
      echo "==> WARNING: Error Lens did not install; diagnostics will still underline, just not inline."

# --- Architecture diagram -----------------------------------------------------
# Lives outside the challenge folders on purpose: Instruqt parses every file
# inside a challenge directory as a lifecycle script.
COPY diagrams/ /opt/diagrams/
COPY sandbox/serve-diagrams.mjs /opt/serve-diagrams.mjs

# --- Bake the workshop source -------------------------------------------------
COPY decouple-monolith/ /opt/workshop/

# --- Pre-warm npm -------------------------------------------------------------
# Installs both trees so the first command in the lab is fast, and typechecks
# them so a broken port fails the image build instead of the workshop.
#
# Both trees must typecheck. The exercise's open TODOs are comments and a
# throwing stub, so it compiles cleanly with the work still undone. Keep these
# strict: a failure here is a real defect, not an expected state.
WORKDIR /opt/workshop/solution
RUN npm ci && npx tsc --noEmit

WORKDIR /opt/workshop/exercise
RUN npm ci && npx tsc --noEmit

# --- Shell environment --------------------------------------------------------
RUN echo 'export TEMPORAL_ADDRESS="${TEMPORAL_ADDRESS:-127.0.0.1:7233}"' >> /root/.bashrc \
    && echo 'export WORKSHOP_REPO=/root/workshop' >> /root/.bashrc

WORKDIR /root
CMD ["/bin/bash"]
