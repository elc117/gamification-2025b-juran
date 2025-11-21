FROM mcr.microsoft.com/devcontainers/base:ubuntu

# Install dependencies
RUN apt-get update && apt-get install -y \
    curl wget unzip sqlite3 openjdk-17-jdk \
    && apt-get clean

# Install SDKMAN
RUN curl -s "https://get.sdkman.io" | bash
ENV SDKMAN_DIR=/root/.sdkman
RUN bash -c "source $SDKMAN_DIR/bin/sdkman-init.sh && sdk install java 17.0.9-open"

# Default workdir
WORKDIR /workspace
