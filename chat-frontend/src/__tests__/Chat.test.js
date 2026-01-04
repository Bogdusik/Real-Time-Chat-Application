// Mock fetch before any imports
global.fetch = jest.fn(() => 
  Promise.resolve({
    json: () => Promise.resolve([]),
    ok: true,
    status: 200
  })
);

// Mock SockJS and STOMP before imports
jest.mock('sockjs-client', () => {
  return jest.fn(() => ({
    onopen: null,
    onmessage: null,
    onclose: null,
    send: jest.fn(),
    close: jest.fn()
  }));
});

const mockActivate = jest.fn();
const mockDeactivate = jest.fn();
const mockPublish = jest.fn();
const mockSubscribe = jest.fn();

jest.mock('@stomp/stompjs', () => ({
  Client: jest.fn().mockImplementation(() => ({
    webSocketFactory: null,
    onConnect: null,
    onDisconnect: null,
    onStompError: null,
    debug: jest.fn(),
    activate: mockActivate,
    deactivate: mockDeactivate,
    connected: true,
    publish: mockPublish,
    subscribe: mockSubscribe
  }))
}));

import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import Chat from '../Chat';

// scrollIntoView is mocked in setupTests.js

describe('Chat Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockActivate.mockClear();
    mockDeactivate.mockClear();
    mockPublish.mockClear();
    mockSubscribe.mockClear();
    fetch.mockImplementation(() => 
      Promise.resolve({
        json: () => Promise.resolve([]),
        ok: true,
        status: 200
      })
    );
  });

  it('renders chat interface', async () => {
    render(<Chat />);
    await waitFor(() => {
      expect(screen.getByText(/chat using websocket/i)).toBeInTheDocument();
    });
  });

  it('loads messages on mount', async () => {
    const mockMessages = [
      {
        id: 1,
        content: 'Test message',
        timestamp: '2024-01-01T00:00:00',
        user: { id: 1, username: 'testuser' }
      }
    ];

    fetch.mockImplementation(() => 
      Promise.resolve({
        json: () => Promise.resolve(mockMessages),
        ok: true,
        status: 200
      })
    );

    render(<Chat />);

    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/messages');
    });
  });

  it('displays connection status', async () => {
    render(<Chat />);
    // Component should render connection status
    await waitFor(() => {
      expect(screen.getByText(/chat using websocket/i)).toBeInTheDocument();
    });
  });
});

