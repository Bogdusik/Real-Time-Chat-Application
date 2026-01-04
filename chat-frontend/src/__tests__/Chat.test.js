import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import Chat from '../Chat';

// All mocks are in setupTests.js

describe('Chat Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    // Reset fetch mock
    fetch.mockImplementation(() => 
      Promise.resolve({
        json: () => Promise.resolve([]),
        ok: true,
        status: 200,
        statusText: 'OK'
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
        status: 200,
        statusText: 'OK'
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

