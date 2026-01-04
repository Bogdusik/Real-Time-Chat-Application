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
  // App renders Chat component, so we just check that it renders without errors
  await waitFor(() => {
    const container = screen.getByRole('main') || document.body;
    expect(container).toBeInTheDocument();
  });
});
