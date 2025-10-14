#!/bin/bash
# Bash script to create a new version branch
# Usage: ./create-version-branch.sh <minecraft_version> [mod_version] [yarn_build] [fabric_version]
# Example: ./create-version-branch.sh 1.21.4

set -e

# Check if minecraft version is provided
if [ -z "$1" ]; then
    echo "Usage: ./create-version-branch.sh <minecraft_version> [mod_version] [yarn_build] [fabric_version]"
    echo "Example: ./create-version-branch.sh 1.21.4"
    exit 1
fi

MINECRAFT_VERSION=$1
MOD_VERSION=${2:-"3-$MINECRAFT_VERSION"}
YARN_BUILD=${3:-"1"}
FABRIC_VERSION=${4:-"0.128.2+$MINECRAFT_VERSION"}

BRANCH_NAME="version/$MINECRAFT_VERSION"
YARN_MAPPINGS="$MINECRAFT_VERSION+build.$YARN_BUILD"

echo ""
echo "=== Creating Version Branch ==="
echo "Branch Name: $BRANCH_NAME"
echo "Minecraft Version: $MINECRAFT_VERSION"
echo "Mod Version: $MOD_VERSION"
echo "Yarn Mappings: $YARN_MAPPINGS"
echo "Fabric Version: $FABRIC_VERSION"
echo ""

if [ "$FABRIC_VERSION" == "0.128.2+$MINECRAFT_VERSION" ]; then
    echo "⚠️  Note: Using placeholder for fabric_version. You may need to update this manually."
    echo ""
fi

# Create and checkout new branch
echo "Creating branch..."
if ! git checkout -b "$BRANCH_NAME"; then
    echo "❌ Failed to create branch. It may already exist."
    echo "Use 'git checkout $BRANCH_NAME' to switch to it."
    exit 1
fi

# Update gradle.properties using sed
sed -i.bak \
    -e "s/^minecraft_version=.*/minecraft_version=$MINECRAFT_VERSION/" \
    -e "s/^yarn_mappings=.*/yarn_mappings=$YARN_MAPPINGS/" \
    -e "s/^mod_version=.*/mod_version=$MOD_VERSION/" \
    -e "s/^fabric_version=.*/fabric_version=$FABRIC_VERSION/" \
    gradle.properties

# Remove backup file
rm -f gradle.properties.bak

echo "✅ Updated gradle.properties"
echo ""

# Show changes
echo "Changes made:"
git diff gradle.properties
echo ""

# Prompt for commit
read -p "Do you want to commit and push these changes? (y/n): " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
    git add gradle.properties
    git commit -m "Initialize version branch for Minecraft $MINECRAFT_VERSION"
    
    echo "Pushing to GitHub..."
    if git push origin "$BRANCH_NAME"; then
        echo ""
        echo "=== Success! ==="
        echo "✅ Branch '$BRANCH_NAME' has been created and pushed."
        echo "✅ GitHub Actions will now build your mod automatically."
        echo ""
        echo "View the build at: https://github.com/CoziSoftware/cozisAddon/actions"
    else
        echo ""
        echo "❌ Failed to push. You may need to push manually:"
        echo "  git push origin $BRANCH_NAME"
    fi
else
    echo ""
    echo "Changes not committed. Review the changes and commit manually:"
    echo "  git add gradle.properties"
    echo "  git commit -m 'Initialize version branch for Minecraft $MINECRAFT_VERSION'"
    echo "  git push origin $BRANCH_NAME"
fi

