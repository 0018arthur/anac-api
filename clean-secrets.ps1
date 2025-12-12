# Script to clean Hugging Face secrets from Git history
# This will rewrite Git history to remove the exposed token

Write-Host "🔒 Cleaning secrets from Git history..." -ForegroundColor Yellow

# Create a backup branch
Write-Host "`n📦 Creating backup branch..." -ForegroundColor Cyan
git branch backup-before-secret-clean

# Method 1: Using git filter-branch (built-in Git command)
Write-Host "`n🧹 Removing secret lines from all commits..." -ForegroundColor Cyan

# This removes any lines containing Hugging Face tokens (hf_*)
git filter-branch --force --tree-filter `
  'if [ -f "src/main/resources/application.properties" ]; then sed -i "/ai\.huggingface\.api-token=hf_/d" "src/main/resources/application.properties"; fi' `
  --prune-empty --tag-name-filter cat -- --all

# Clean up refs
Write-Host "`n🗑️  Cleaning up Git references..." -ForegroundColor Cyan
git for-each-ref --format="delete %(refname)" refs/original | git update-ref --stdin
git reflog expire --expire=now --all
git gc --prune=now --aggressive

Write-Host "`n✅ Git history cleaned!" -ForegroundColor Green
Write-Host "`n⚠️  Next steps:" -ForegroundColor Yellow
Write-Host "1. Review the changes with: git log --oneline -10" -ForegroundColor White
Write-Host "2. Force push with: git push origin main --force" -ForegroundColor White
Write-Host "3. IMPORTANT: Revoke the exposed Hugging Face token and create a new one!" -ForegroundColor Red
