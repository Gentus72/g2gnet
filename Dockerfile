FROM eclipse-temurin:21-jdk-jammy

# Install VNC server, window manager, and system dependencies
RUN apt-get update && apt-get install -y \
    tigervnc-standalone-server \
    tigervnc-common \
    fluxbox \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libgl1-mesa-glx \
    libgtk-3-0 \
    xfonts-base \
    wget \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Download and install JavaFX 21
RUN wget -O /tmp/javafx.zip https://download2.gluonhq.com/openjfx/21.0.1/openjfx-21.0.1_linux-x64_bin-sdk.zip && \
    unzip /tmp/javafx.zip -d /opt/ && \
    mv /opt/javafx-sdk-21.0.1 /opt/javafx && \
    rm /tmp/javafx.zip

# Copy VNC startup script
COPY vnc-startup.sh /app/vnc-startup.sh
RUN chmod +x /app/vnc-startup.sh

# Create VNC directory and set password
RUN mkdir -p ~/.vnc && \
    echo "password" | vncpasswd -f > ~/.vnc/passwd && \
    chmod 600 ~/.vnc/passwd

WORKDIR /app

# Copy your JAR file
COPY target/*.jar /app

# Copy additional resources
COPY ccserver/ /app/ccserver
COPY server/ /app/server
COPY client/res/ /app/client/res

# Expose VNC port
EXPOSE 5901
EXPOSE 7000

# Default command (can be overridden in docker-compose)
CMD ["./vnc-startup.sh"]