# WPI Planner - Build Instructions

## Prerequisites

- **Java JDK 8 or higher** (Java 21 tested and working)
- **GWT SDK 2.11.0** (automatically downloaded to `gwt-2.11.0/` directory)

## Quick Start

### 1. Build the Application

**Windows:**
```batch
build.bat
```

**Linux/Mac:**
```bash
./build.sh
```

### 2. Serve the Application

**Windows:**
```batch
serve.bat
```

**Linux/Mac:**
```bash
python3 -m http.server 8000 -d war
```

Then open your browser to: http://localhost:8000/

### 3. Development Mode (Optional)

For live development with automatic recompilation:

**Windows:**
```batch
dev.bat
```

This will start the GWT SuperDev Mode server. Access your application at:
- Application: http://localhost:8888/Scheduler.html  
- Development Console: http://localhost:9876/

## Project Structure

- `src/` - Java source code
- `war/` - Compiled web application (deploy this directory)
- `gwt-2.11.0/` - GWT SDK
- `build.bat` / `build.sh` - Build scripts
- `dev.bat` - Development server script
- `serve.bat` - HTTP server script

## Files Generated

After building, the following files are generated in `war/scheduler/`:
- `*.cache.js` - Compiled JavaScript modules
- `scheduler.nocache.js` - Bootstrap loader
- `scheduler.devmode.js` - Development mode loader

## Troubleshooting

1. **Build fails with Java compilation errors**: Check that Java 8+ is installed and in PATH
2. **GWT compilation fails**: Ensure all Java files compile successfully first
3. **Application doesn't load**: Check browser console for errors, clear cache
4. **Development mode issues**: Make sure ports 8888 and 9876 are available

## Deployment

To deploy to a web server, copy the contents of the `war/` directory to your web server's document root.