# Multi-stage build for Node.js backend
# node:22-alpine LTS - fixes CVE-2025-* (race condition, symlink, directory traversal, etc.)
FROM node:22-alpine AS backend-builder

WORKDIR /app

# Copy package files
COPY server/package*.json ./

# Install dependencies
RUN npm ci --only=production

# Copy source code
COPY server/src ./src

# Runtime stage
FROM node:22-alpine

WORKDIR /app

# Copy dependencies and source from builder
COPY --from=backend-builder /app/node_modules ./node_modules
COPY --from=backend-builder /app/src ./src
COPY server/package.json ./

# Expose port
EXPOSE 8080

# Run the application
CMD ["node", "src/index.js"]

