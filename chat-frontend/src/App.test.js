import { render, screen, waitFor } from '@testing-library/react';
import App from './App';

// All mocks are in setupTests.js

test('renders chat application', async () => {
  // Ensure fetch is properly mocked
  if (typeof fetch === 'undefined' || !fetch.mockImplementation) {
    global.fetch = jest.fn(() => 
      Promise.resolve({
        json: () => Promise.resolve([]),
        ok: true,
        status: 200,
        statusText: 'OK'
      })
    );
  } else {
    fetch.mockImplementation(() => 
      Promise.resolve({
        json: () => Promise.resolve([]),
        ok: true,
        status: 200,
        statusText: 'OK'
      })
    );
  }

  render(<App />);
  // App renders Chat component, so we check that it renders the chat interface
  await waitFor(() => {
    // Check for the chat title which is always rendered
    expect(screen.getByText(/chat using websocket/i)).toBeInTheDocument();
  });
});
