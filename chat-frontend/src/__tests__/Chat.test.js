import React from 'react';
import { render, screen, waitFor, fireEvent, act } from '@testing-library/react';
import Chat from '../Chat';
import { Client } from '@stomp/stompjs';

describe('Chat Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    fetch.mockImplementation(() =>
      Promise.resolve({
        json: () => Promise.resolve([]),
        ok: true,
        status: 200,
        statusText: 'OK'
      })
    );
  });

  it('renders username entry form initially', async () => {
    await act(async () => {
      render(<Chat />);
    });
    expect(screen.getByText(/enter your name to join/i)).toBeInTheDocument();
  });

  it('join button is disabled when username is empty', async () => {
    await act(async () => {
      render(<Chat />);
    });
    expect(screen.getByRole('button', { name: /join/i })).toBeDisabled();
  });

  it('join button is enabled when username is entered', async () => {
    await act(async () => {
      render(<Chat />);
    });
    const input = screen.getByLabelText(/your name/i);
    fireEvent.change(input, { target: { value: 'testuser' } });
    expect(screen.getByRole('button', { name: /join/i })).not.toBeDisabled();
  });

  it('loads messages history on mount', async () => {
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

    await act(async () => {
      render(<Chat />);
    });

    expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/messages');
  });

  describe('after joining', () => {
    const history = [
      { id: 1, content: 'Morning! Deploy is green', timestamp: '2026-11-19T16:10:00', user: { id: 2, username: 'Bob' } },
      { id: 2, content: 'Great, merging now', timestamp: '2026-11-19T16:11:00', user: { id: 1, username: 'Alice' } }
    ];

    const join = async () => {
      // CRA resets mock implementations before each test, so give the STOMP client one here
      Client.mockImplementation(() => ({
        activate: jest.fn(),
        deactivate: jest.fn(),
        subscribe: jest.fn(),
        publish: jest.fn(),
        connected: true
      }));
      fetch.mockImplementation((url) =>
        Promise.resolve({
          ok: true,
          json: () => Promise.resolve(String(url).endsWith('/api/users/register') ? { id: 1, username: 'Alice' } : history)
        })
      );
      await act(async () => {
        render(<Chat />);
      });
      fireEvent.change(screen.getByLabelText(/your name/i), { target: { value: 'Alice' } });
      await act(async () => {
        fireEvent.click(screen.getByRole('button', { name: /join/i }));
      });
    };

    it('shows the connection state and switches to connected', async () => {
      await join();
      expect(screen.getByText(/connecting/i)).toBeInTheDocument();
      const config = Client.mock.calls[Client.mock.calls.length - 1][0];
      await act(async () => {
        config.onConnect();
      });
      expect(screen.getByText(/^connected$/i)).toBeInTheDocument();
    });

    it('puts your own messages on the right and others on the left', async () => {
      await join();
      const own = screen.getByText('Great, merging now').closest('[data-own]');
      const other = screen.getByText('Morning! Deploy is green').closest('[data-own]');
      expect(own).toHaveAttribute('data-own', 'true');
      expect(other).toHaveAttribute('data-own', 'false');
      expect(screen.getByText('Bob')).toBeInTheDocument();
    });

    it('shows times as hours and minutes, not raw ISO strings', async () => {
      await join();
      expect(screen.queryByText(/2026-11-19T16:10:00/)).not.toBeInTheDocument();
      expect(screen.getByText(/16:10/)).toBeInTheDocument();
    });
  });
});
