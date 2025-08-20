#!/bin/bash

echo "Building WPI Planner GWT Application..."

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GWT_HOME="$SCRIPT_DIR/gwt-2.11.0"
PROJECT_ROOT="$SCRIPT_DIR"
WAR_DIR="$PROJECT_ROOT/war"
SRC_DIR="$PROJECT_ROOT/src"
CLASSES_DIR="$WAR_DIR/WEB-INF/classes"

# Check if GWT SDK exists
if [ ! -f "$GWT_HOME/gwt-dev.jar" ]; then
    echo "ERROR: GWT SDK not found at $GWT_HOME"
    echo "Please ensure GWT 2.11.0 is installed in the gwt-2.11.0 directory"
    exit 1
fi

echo "Using GWT SDK at: $GWT_HOME"

# Create classes directory if it doesn't exist
mkdir -p "$CLASSES_DIR"

# Compile Java source to classes
echo
echo "Compiling Java source files..."
javac -cp "$GWT_HOME/gwt-user.jar:$GWT_HOME/gwt-dev.jar:$SRC_DIR" \
      -d "$CLASSES_DIR" \
      -sourcepath "$SRC_DIR" \
      "$SRC_DIR"/edu/wpi/scheduler/client/*.java \
      "$SRC_DIR"/edu/wpi/scheduler/client/controller/*.java \
      "$SRC_DIR"/edu/wpi/scheduler/client/courseselection/*.java \
      "$SRC_DIR"/edu/wpi/scheduler/client/generator/*.java \
      "$SRC_DIR"/edu/wpi/scheduler/client/permutation/*.java \
      "$SRC_DIR"/edu/wpi/scheduler/client/permutation/view/*.java \
      "$SRC_DIR"/edu/wpi/scheduler/client/storage/*.java \
      "$SRC_DIR"/edu/wpi/scheduler/client/tabs/*.java \
      "$SRC_DIR"/edu/wpi/scheduler/client/timechooser/*.java \
      "$SRC_DIR"/edu/wpi/scheduler/client/welcome/*.java \
      "$SRC_DIR"/edu/wpi/scheduler/shared/model/*.java

if [ $? -ne 0 ]; then
    echo "ERROR: Java compilation failed"
    exit 1
fi

# Copy non-Java files to classes directory
echo
echo "Copying resources..."
cp -r "$SRC_DIR/edu/wpi/scheduler" "$CLASSES_DIR/edu/wpi/"

# Run GWT Compiler
echo
echo "Running GWT Compiler..."
java -cp "$GWT_HOME/gwt-dev.jar:$GWT_HOME/gwt-user.jar:$SRC_DIR:$CLASSES_DIR" \
     com.google.gwt.dev.Compiler \
     -war "$WAR_DIR" \
     -logLevel INFO \
     edu.wpi.scheduler.Scheduler

if [ $? -ne 0 ]; then
    echo "ERROR: GWT compilation failed"
    exit 1
fi

echo
echo "Build completed successfully!"
echo "You can now serve the application from the war directory."
echo "For development, use: java -cp gwt-dev.jar com.google.gwt.dev.codeserver.CodeServer edu.wpi.scheduler.Scheduler"