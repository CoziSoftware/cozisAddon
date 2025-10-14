# PowerShell script to create a new version branch
# Usage: .\create-version-branch.ps1 -MinecraftVersion "1.21.4" -ModVersion "3-1.21.4"

param(
    [Parameter(Mandatory=$true)]
    [string]$MinecraftVersion,
    
    [Parameter(Mandatory=$false)]
    [string]$ModVersion,
    
    [Parameter(Mandatory=$false)]
    [string]$YarnBuild = "1",
    
    [Parameter(Mandatory=$false)]
    [string]$FabricVersion
)

# If ModVersion not specified, use format: 3-{MinecraftVersion}
if (-not $ModVersion) {
    $ModVersion = "3-$MinecraftVersion"
}

# Set default Fabric version if not provided
if (-not $FabricVersion) {
    Write-Host "Note: Using placeholder for fabric_version. You may need to update this manually." -ForegroundColor Yellow
    $FabricVersion = "0.128.2+$MinecraftVersion"
}

$BranchName = "version/$MinecraftVersion"
$YarnMappings = "$MinecraftVersion+build.$YarnBuild"

Write-Host "`n=== Creating Version Branch ===" -ForegroundColor Cyan
Write-Host "Branch Name: $BranchName" -ForegroundColor Green
Write-Host "Minecraft Version: $MinecraftVersion" -ForegroundColor Green
Write-Host "Mod Version: $ModVersion" -ForegroundColor Green
Write-Host "Yarn Mappings: $YarnMappings" -ForegroundColor Green
Write-Host "Fabric Version: $FabricVersion" -ForegroundColor Green
Write-Host ""

# Create and checkout new branch
Write-Host "Creating branch..." -ForegroundColor Yellow
git checkout -b $BranchName

if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to create branch. It may already exist." -ForegroundColor Red
    Write-Host "Use 'git checkout $BranchName' to switch to it." -ForegroundColor Yellow
    exit 1
}

# Read gradle.properties
$gradlePropsPath = "gradle.properties"
$content = Get-Content $gradlePropsPath -Raw

# Update versions
$content = $content -replace "minecraft_version=.*", "minecraft_version=$MinecraftVersion"
$content = $content -replace "yarn_mappings=.*", "yarn_mappings=$YarnMappings"
$content = $content -replace "mod_version=.*", "mod_version=$ModVersion"
$content = $content -replace "fabric_version=.*", "fabric_version=$FabricVersion"

# Write back
Set-Content -Path $gradlePropsPath -Value $content -NoNewline

Write-Host "`nUpdated gradle.properties" -ForegroundColor Green

# Show changes
Write-Host "`nChanges made:" -ForegroundColor Cyan
git diff gradle.properties

# Prompt for commit
Write-Host "`nDo you want to commit and push these changes? (y/n): " -NoNewline -ForegroundColor Yellow
$response = Read-Host

if ($response -eq 'y' -or $response -eq 'Y') {
    git add gradle.properties
    git commit -m "Initialize version branch for Minecraft $MinecraftVersion"
    
    Write-Host "`nPushing to GitHub..." -ForegroundColor Yellow
    git push origin $BranchName
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n=== Success! ===" -ForegroundColor Green
        Write-Host "Branch '$BranchName' has been created and pushed." -ForegroundColor Green
        Write-Host "GitHub Actions will now build your mod automatically." -ForegroundColor Green
        Write-Host "`nView the build at: https://github.com/CoziSoftware/cozisAddon/actions" -ForegroundColor Cyan
    } else {
        Write-Host "`nFailed to push. You may need to push manually:" -ForegroundColor Red
        Write-Host "  git push origin $BranchName" -ForegroundColor Yellow
    }
} else {
    Write-Host "`nChanges not committed. Review the changes and commit manually:" -ForegroundColor Yellow
    Write-Host "  git add gradle.properties" -ForegroundColor Gray
    Write-Host "  git commit -m 'Initialize version branch for Minecraft $MinecraftVersion'" -ForegroundColor Gray
    Write-Host "  git push origin $BranchName" -ForegroundColor Gray
}

