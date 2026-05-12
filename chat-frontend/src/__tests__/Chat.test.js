import React from 'react';
import { render, screen, waitFor, fireEvent, act } from '@testing-library/react';
import Chat from '../Chat';

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
});
