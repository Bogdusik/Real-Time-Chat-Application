const express = require('express');
const http = require('http');
const sockjs = require('sockjs');
const cors = require('cors');
const chatController = require('./controllers/chatController');
const websocketHandler = require('./websocket/websocketHandler');
const { initializeDatabase } = require('./database/db');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 8080;

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

// Create HTTP server
const server = http.createServer(app);

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
    console.log(`🚀 Chat server running on port ${PORT}`);
    console.log(`📡 WebSocket endpoint: http://localhost:${PORT}/ws`);
    console.log(`📊 Health check: http://localhost:${PORT}/health`);
  });
}

module.exports = app;
