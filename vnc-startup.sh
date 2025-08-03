#!/bin/bash

echo "Starting VNC server setup..."

# Kill any existing VNC servers
vncserver -kill :1 2>/dev/null || true

# Clean up any existing X11 locks
rm -rf /tmp/.X1-lock /tmp/.X11-unix/X1 2>/dev/null || true

# Start VNC server on display :1
echo "Starting VNC server on display :1..."
vncserver :1 -geometry 1280x720 -depth 24 -localhost no

# Wait for VNC server to start
sleep 3

# Set DISPLAY environment variable
export DISPLAY=:1

# Start window manager in background
echo "Starting Fluxbox window manager..."
fluxbox &

# Wait for window manager to start
sleep 2

# Start your JavaFX application with proper module path
echo "Starting JavaFX application..."
java --module-path /opt/javafx/lib \
     --add-modules javafx.controls,javafx.fxml \
     -Djava.awt.headless=false \
     -cp /app/g2gnet-1.0-SNAPSHOT.jar \
     org.geooo.Client

# Keep the container running even if the JavaFX app exits
echo "Application finished. Keeping VNC server running..."
tail -f /dev/null