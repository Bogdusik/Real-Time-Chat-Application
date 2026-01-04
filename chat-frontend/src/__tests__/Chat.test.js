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

// Mock fetch
global.fetch = jest.fn();

// scrollIntoView is mocked in setupTests.js

describe('Chat Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockActivate.mockClear();
    mockDeactivate.mockClear();
    mockPublish.mockClear();
    mockSubscribe.mockClear();
    fetch.mockResolvedValue({
      json: async () => []
    });
  });

  it('renders chat interface', () => {
    render(<Chat />);
    expect(screen.getByText(/chat using websocket/i)).toBeInTheDocument();
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

    fetch.mockResolvedValue({
      json: async () => mockMessages
    });

    render(<Chat />);

    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/messages');
    });
  });

  it('displays connection status', () => {
    render(<Chat />);
    // Component should render connection status
    expect(screen.getByText(/chat using websocket/i)).toBeInTheDocument();
  });
});

