const express = require('express');
const http = require('http');
const https = require('https');
const fs = require('fs');
const sockjs = require('sockjs');
const cors = require('cors');
const helmet = require('helmet');
const chatController = require('./controllers/chatController');
const websocketHandler = require('./websocket/websocketHandler');
const { initializeDatabase } = require('./database/db');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 8080;

// Security: hide X-Powered-By, set secure headers (CWE-200)
app.use(helmet({ contentSecurityPolicy: false }));
app.disable('x-powered-by');

// Middleware
app.use(cors({
  origin: process.env.CORS_ORIGIN || 'http://localhost:3000',
  credentials: true
}));
app.use(express.json());

// Initialize database (skip in test environment)
if (process.env.NODE_ENV !== 'test') {
  initializeDatabase().catch(err => {
    console.error('Failed to initialize database:', err);
    process.exit(1);
  });
}

// Routes
app.get('/api/messages', chatController.getMessages);
app.get('/health', (req, res) => {
  res.json({ status: 'ok', message: 'Chat API is running' });
});

// CWE-319: Use HTTPS when TLS certs are provided (production), else HTTP (dev)
const useHttps = process.env.SSL_KEY_PATH && process.env.SSL_CERT_PATH;
let server;
if (useHttps) {
  try {
    server = https.createServer(
      {
        key: fs.readFileSync(process.env.SSL_KEY_PATH),
        cert: fs.readFileSync(process.env.SSL_CERT_PATH),
      },
      app
    );
  } catch (err) {
    console.error('HTTPS config failed, falling back to HTTP:', err.message);
    server = http.createServer(app);
  }
} else {
  server = http.createServer(app);
}

// SockJS server for WebSocket connections
const sockjsServer = sockjs.createServer({
  sockjs_url: 'https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js',
  log: (severity, message) => {
    if (severity === 'error') {
      console.error('SockJS:', message);
    }
  }
});

// Handle SockJS connections
websocketHandler(sockjsServer, chatController);

// Install SockJS handlers
sockjsServer.installHandlers(server, { prefix: '/ws' });

// Start server only if not in test environment
if (process.env.NODE_ENV !== 'test') {
  server.listen(PORT, () => {
    const protocol = useHttps ? 'https' : 'http';
    console.log(`🚀 Chat server running on port ${PORT} (${protocol})`);
    console.log(`📡 WebSocket endpoint: ${protocol}://localhost:${PORT}/ws`);
    console.log(`📊 Health check: ${protocol}://localhost:${PORT}/health`);
  });
}

module.exports = app;
