#!/bin/bash

# Build and Deploy Script for MonieBank Services
set -e

# Get command line arguments
VERSION=${1:-1.0.1}
SPECIFIC_SERVICE=$2

echo "============================================"
echo "MonieBank Build Script"
echo "============================================"
echo "Version: $VERSION"

if [ ! -z "$SPECIFIC_SERVICE" ]; then
    echo "Building only: $SPECIFIC_SERVICE"
else
    echo "Building: ALL services"
fi
echo "============================================"

# Function to build a service
build_service() {
    SERVICE_NAME=$1
    SERVICE_DIR=$2

    echo "📦 Building $SERVICE_NAME..."

    # Check if directory exists
    if [ ! -d "$SERVICE_DIR" ]; then
        echo "❌ Error: Directory $SERVICE_DIR does not exist!"
        return 1
    fi

    # Navigate to service directory
    cd $SERVICE_DIR

    # Clean and build JAR
    echo "🔨 Running mvn clean package..."
    mvn clean package -DskipTests

    # Build Docker image
    echo "🐳 Building Docker image..."
    docker build -t moniebank/$SERVICE_NAME:$VERSION .

    # Tag as latest as well (remove this if you only want version tags)
    docker tag moniebank/$SERVICE_NAME:$VERSION moniebank/$SERVICE_NAME:latest

    echo "✅ $SERVICE_NAME built successfully!"
    cd ..
    echo ""
}

# Function to build frontend
build_frontend() {
    echo "🌐 Building frontend..."

    if [ ! -d "./moniebank-frontend" ]; then
        echo "❌ Error: Frontend directory does not exist!"
        return 1
    fi

    cd ./moniebank-frontend
    docker build -t moniebank/frontend:$VERSION \
      --build-arg VITE_API_BASE_URL=http://localhost:8080/api \
      --build-arg VITE_GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID} .
    docker tag moniebank/frontend:$VERSION moniebank/frontend:latest
    cd ..
    echo "✅ Frontend built successfully!"
    echo ""
}

# List of all services
SERVICES=(
    "auth-service:./auth-service"
#    "account-service:./account-service"
#    "api-gateway:./api-gateway"
#    "eureka-server:./eureka-server"
#    "notification-service:./notification-service"
#    "transaction-service:./transaction-service"
)

# Main build logic
if [ -z "$SPECIFIC_SERVICE" ]; then
    # Build all services
    echo "🚀 Starting build process for ALL services..."
    echo ""

    # Build each service
    for service in "${SERVICES[@]}"; do
        SERVICE_NAME=$(echo $service | cut -d':' -f1)
        SERVICE_DIR=$(echo $service | cut -d':' -f2)
        build_service "$SERVICE_NAME" "$SERVICE_DIR"
    done

    # Build frontend
#    build_frontend

else
    # Build specific service
    echo "🎯 Building specific service: $SPECIFIC_SERVICE"
    echo ""

    # Check if it's frontend
    if [ "$SPECIFIC_SERVICE" = "frontend" ]; then
        build_frontend
    else
        # Find the service in our list
        FOUND=false
        for service in "${SERVICES[@]}"; do
            SERVICE_NAME=$(echo $service | cut -d':' -f1)
            SERVICE_DIR=$(echo $service | cut -d':' -f2)

            if [ "$SERVICE_NAME" = "$SPECIFIC_SERVICE" ]; then
                build_service "$SERVICE_NAME" "$SERVICE_DIR"
                FOUND=true
                break
            fi
        done

        if [ "$FOUND" = false ]; then
            echo "❌ Error: Service '$SPECIFIC_SERVICE' not found!"
            echo "Available services:"
            for service in "${SERVICES[@]}"; do
                echo "  - $(echo $service | cut -d':' -f1)"
            done
            echo "  - frontend"
            exit 1
        fi
    fi
fi

echo "============================================"
echo "🎉 Build completed successfully!"
echo "============================================"
echo "Images created:"
docker images | grep moniebank | head -20
echo "============================================"

# Show usage examples
echo "Usage examples:"
echo "  ./build.sh                       # Build all with version 1.0.1"
echo "  ./build.sh 2.0.0                # Build all with version 2.0.0"
echo "  ./build.sh 1.0.1 auth-service   # Build only auth-service"
echo "  ./build.sh 2.0.0 frontend       # Build only frontend"
echo "============================================"