import React, { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import {
  TextField,
  Button,
  List,
  ListItem,
  Container,
  Box,
  Alert,
  Card,
  CardContent,
  Typography,
  Paper
} from '@mui/material';

let stompClient = null;

export default function Chat() {
  const [message, setMessage] = useState('');
  const [messages, setMessages] = useState([]);
  const [connected, setConnected] = useState(false);
  const messagesEndRef = useRef(null);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      onConnect: () => {
        console.log('✅ Connected to STOMP!');
        setConnected(true);
        client.subscribe('/topic/messages', (msg) => {
          const messageOutput = JSON.parse(msg.body);
          setMessages(prev => [...prev, messageOutput]);
        });
      },
      onDisconnect: () => {
        console.log('❌ STOMP disconnected.');
        setConnected(false);
      },
      onStompError: (frame) => {
        console.error('Broker error: ', frame.headers['message']);
      },
      debug: (str) => {
        console.log(str);
      }
    });
    client.activate();
    stompClient = client;

    return () => client.deactivate();
  }, []);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const sendMessage = () => {
    if (stompClient && stompClient.connected && message.trim() !== '') {
      const msgObject = {
        content: message,
        user: { id: 1 }
      };
      stompClient.publish({
        destination: '/app/sendMessage',
        body: JSON.stringify(msgObject)
      });
      setMessage('');
    } else {
      alert('❗ WebSocket is not connected yet or message is empty.');
    }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
      }}
    >
      <Container maxWidth="sm">
        {!connected && <Alert severity="warning" sx={{ mb: 2 }}>No connection to WebSocket. Waiting for connection...</Alert>}

        <Card sx={{ p: 3, boxShadow: 4 }}>
          <CardContent>
            <Typography variant="h4" gutterBottom align="center">
              Chat using WebSocket + React 💬
            </Typography>

            <Box display="flex" gap={2} mb={2}>
              <TextField
                fullWidth
                label="Enter message"
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                disabled={!connected}
              />
              <Button variant="contained" onClick={sendMessage} disabled={!connected}>
                Send
              </Button>
            </Box>

            <Paper sx={{ maxHeight: 300, overflowY: 'auto', p: 1 }}>
              <List>
                {messages.map((msg, index) => (
                  <ListItem key={index} sx={{ mb: 1, p: 1, borderRadius: 2, backgroundColor: '#f5f5f5' }}>
                    <Box>
                      <Typography variant="subtitle2" color="primary">
                        {msg.user?.username || 'Anonymous'}:
                      </Typography>
                      <Typography variant="body1">{msg.content}</Typography>
                      <Typography variant="caption" color="text.secondary">
                        {msg.timestamp ? `${msg.timestamp}` : ''}
                      </Typography>
                    </Box>
                  </ListItem>
                ))}
                <div ref={messagesEndRef} />
              </List>
            </Paper>
          </CardContent>
        </Card>
      </Container>
    </Box>
  );
}
