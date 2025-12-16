#!/bin/bash
set -e

# Define the backup directory and filename
BACKUP_DIR="backups"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="${BACKUP_DIR}/src-${TIMESTAMP}.tar.gz"

# Create the backup directory if it doesn't exist
mkdir -p "$BACKUP_DIR"

# Generate git-info.txt with current branch and hash
echo "Generating git info..."
echo "Branch: $(git rev-parse --abbrev-ref HEAD)" > git-info.txt
echo "Hash: $(git rev-parse HEAD)" >> git-info.txt

echo "Starting backup to ${BACKUP_FILE}..."

# Create the tar.gz archive
# Using --exclude to skip specified directories
tar -czf "$BACKUP_FILE" \
    --exclude='./.idea' \
    --exclude='./backups' \
    --exclude='./environments/op-generali' \
    --exclude='./environments/op-prod' \
    --exclude='./environments/op-uat' \
    --exclude='./environments/prod' \
    --exclude='./environments/uat' \
    --exclude='./logs (env)' \
    --exclude='./tmp' \
    .

# Clean up the temporary git-info.txt file
rm git-info.txt

echo "Backup completed successfully: ${BACKUP_FILE}"