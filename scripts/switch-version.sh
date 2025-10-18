#!/bin/bash

# Minecraft Version Switcher Script
# Usage: ./scripts/switch-version.sh [1.21.4|1.21.5]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [1.21.4|1.21.5]"
    echo ""
    echo "This script switches the Minecraft version in gradle.properties"
    echo ""
    echo "Examples:"
    echo "  $0 1.21.4    # Switch to Minecraft 1.21.4"
    echo "  $0 1.21.5    # Switch to Minecraft 1.21.5"
    echo ""
    echo "If no version is specified, it will toggle between 1.21.4 and 1.21.5"
}

# Function to get current version
get_current_version() {
    if [ -f "gradle.properties" ]; then
        grep "minecraft_version=" gradle.properties | cut -d'=' -f2
    else
        print_error "gradle.properties not found!"
        exit 1
    fi
}

# Function to update version
update_version() {
    local target_version=$1
    local current_version=$(get_current_version)
    
    print_status "Current version: $current_version"
    print_status "Target version: $target_version"
    
    if [ "$current_version" = "$target_version" ]; then
        print_warning "Already on version $target_version"
        return 0
    fi
    
    print_status "Updating gradle.properties..."
    
    # Update minecraft_version
    sed -i "s/minecraft_version=.*/minecraft_version=$target_version/" gradle.properties
    
    # Update yarn_mappings
    sed -i "s/yarn_mappings=.*/yarn_mappings=$target_version+build.1/" gradle.properties
    
    # Update fabric_version
    sed -i "s/fabric_version=.*/fabric_version=0.128.2+$target_version/" gradle.properties
    
    # Update meteor_version
    sed -i "s/meteor_version=.*/meteor_version=$target_version-SNAPSHOT/" gradle.properties
    
    # Update baritone_version
    sed -i "s/baritone_version=.*/baritone_version=$target_version-SNAPSHOT/" gradle.properties
    
    # Update XaeroPlus version
    sed -i "s/xaeroplus_version=.*/xaeroplus_version=2.28.1+fabric-$target_version/" gradle.properties
    
    # Update XaeroWorldmap version
    sed -i "s/xaeros_worldmap_version=.*/xaeros_worldmap_version=1.39.12_Fabric_$target_version/" gradle.properties
    
    # Update XaeroMinimap version
    sed -i "s/xaeros_minimap_version=.*/xaeros_minimap_version=25.2.10_Fabric_$target_version/" gradle.properties
    
    print_success "Updated to Minecraft $target_version"
}

# Function to validate version
validate_version() {
    local version=$1
    case $version in
        1.21.4|1.21.5)
            return 0
            ;;
        *)
            return 1
            ;;
    esac
}

# Function to toggle version
toggle_version() {
    local current_version=$(get_current_version)
    case $current_version in
        1.21.4)
            echo "1.21.5"
            ;;
        1.21.5)
            echo "1.21.4"
            ;;
        *)
            print_warning "Unknown current version: $current_version, defaulting to 1.21.5"
            echo "1.21.5"
            ;;
    esac
}

# Main script logic
main() {
    print_status "Minecraft Version Switcher"
    print_status "========================="
    
    # Check if gradle.properties exists
    if [ ! -f "gradle.properties" ]; then
        print_error "gradle.properties not found in current directory!"
        print_error "Please run this script from the project root directory."
        exit 1
    fi
    
    # Determine target version
    local target_version
    if [ $# -eq 0 ]; then
        # No arguments provided, toggle version
        target_version=$(toggle_version)
        print_status "No version specified, toggling to: $target_version"
    else
        # Version provided as argument
        target_version=$1
        if ! validate_version "$target_version"; then
            print_error "Invalid version: $target_version"
            show_usage
            exit 1
        fi
    fi
    
    # Update version
    update_version "$target_version"
    
    # Show updated configuration
    print_status "Updated configuration:"
    echo "  Minecraft: $(grep minecraft_version gradle.properties | cut -d'=' -f2)"
    echo "  Fabric: $(grep fabric_version gradle.properties | cut -d'=' -f2)"
    echo "  Meteor: $(grep meteor_version gradle.properties | cut -d'=' -f2)"
    echo "  Baritone: $(grep baritone_version gradle.properties | cut -d'=' -f2)"
    
    print_success "Version switch completed!"
    print_status "You may want to run './gradlew clean build' to test the new configuration."
}

# Run main function with all arguments
main "$@"
