const chatController = require('../controllers/chatController');

// Store subscriptions for each connection
const subscriptions = new Map();

function parseStompFrame(data) {
  const text = data.toString();
  const lines = text.split('\n');
  
  if (lines.length === 0) return null;
  
  const command = lines[0].trim();
  if (!command) return null;
  
  const headers = {};
  let body = '';
  let bodyStart = false;
  
  for (let i = 1; i < lines.length; i++) {
    const line = lines[i];
    
    if (line === '') {
      bodyStart = true;
      continue;
    }
    
    if (bodyStart) {
      // Remove null terminator if present
      if (line.endsWith('\0')) {
        body += line.slice(0, -1);
      } else {
        body += line;
      }
    } else {
      const colonIndex = line.indexOf(':');
      if (colonIndex > 0) {
        const key = line.substring(0, colonIndex).trim();
        const value = line.substring(colonIndex + 1).trim();
        headers[key] = value;
      }
    }
  }
  
  return { command, headers, body };
}

function createStompFrame(command, headers = {}, body = '') {
  let frame = command + '\n';
  
  for (const [key, value] of Object.entries(headers)) {
    frame += `${key}:${value}\n`;
  }
  
  frame += '\n';
  if (body) {
    frame += body;
  }
  frame += '\0';
  
  return frame;
}

function websocketHandler(sockjsServer, chatController) {
  sockjsServer.on('connection', (conn) => {
    console.log('✅ New SockJS connection');
    
    const clientId = `client-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
    subscriptions.set(conn, new Map());

    // Handle incoming messages
    conn.on('data', async (data) => {
      try {
        const frame = parseStompFrame(data);
        
        if (!frame) return;

        if (frame.command === 'CONNECT') {
          // Send CONNECTED frame
          const connectedFrame = createStompFrame('CONNECTED', {
            'server': 'nodejs-chat-server',
            'version': '1.2',
            'heart-beat': '0,0'
          });
          conn.write(connectedFrame);
          console.log(`✅ Client ${clientId} connected`);
          return;
        }

        if (frame.command === 'SUBSCRIBE') {
          const subscriptionId = frame.headers.id;
          const destination = frame.headers.destination;
          
          subscriptions.get(conn).set(subscriptionId, destination);
          conn.subscriptionId = subscriptionId;
          conn.subscriptionDestination = destination;
          console.log(`📥 Client ${clientId} subscribed to ${destination} (sub-id: ${subscriptionId})`);
          return;
        }

        if (frame.command === 'UNSUBSCRIBE') {
          const subscriptionId = frame.headers.id;
          subscriptions.get(conn).delete(subscriptionId);
          console.log(`📤 Client ${clientId} unsubscribed from ${subscriptionId}`);
          return;
        }

        if (frame.command === 'SEND') {
          const destination = frame.headers.destination;
          
          if (destination === '/app/sendMessage') {
            // Parse message body
            const messageData = JSON.parse(frame.body);
            
            // Save message to database
            const savedMessage = await chatController.sendMessage(messageData);
            
            // Broadcast to all connected clients subscribed to /topic/messages
            const broadcastBody = JSON.stringify(savedMessage);
            
            // Get all connections from sockjs server
            const allConnections = Array.from(subscriptions.keys());
            
            allConnections.forEach((clientConn) => {
              if (clientConn.readyState === 1) {
                const clientSubs = subscriptions.get(clientConn);
                if (clientSubs) {
                  for (const [subId, dest] of clientSubs.entries()) {
                    if (dest === '/topic/messages') {
                      const messageFrame = createStompFrame('MESSAGE', {
                        'subscription': subId,
                        'message-id': `msg-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
                        'destination': '/topic/messages',
                        'content-type': 'application/json'
                      }, broadcastBody);
                      clientConn.write(messageFrame);
                    }
                  }
                }
              }
            });
          }
        }

        if (frame.command === 'DISCONNECT') {
          subscriptions.delete(conn);
          conn.close();
        }
      } catch (error) {
        console.error('Error handling WebSocket message:', error);
        const errorFrame = createStompFrame('ERROR', {
          'message': error.message
        }, JSON.stringify({ error: error.message }));
        conn.write(errorFrame);
      }
    });

    conn.on('close', () => {
      console.log(`❌ SockJS connection closed for ${clientId}`);
      subscriptions.delete(conn);
    });
  });
}

module.exports = websocketHandler;

