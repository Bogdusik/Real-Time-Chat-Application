// Ensure fetch is mocked before any imports
global.fetch = jest.fn(() => 
  Promise.resolve({
    json: () => Promise.resolve([]),
    ok: true,
    status: 200,
    statusText: 'OK'
  })
);

import { render, screen, waitFor } from '@testing-library/react';
import App from './App';

test('renders chat application', async () => {
  // Reset fetch mock
  fetch.mockResolvedValue({
    json: () => Promise.resolve([]),
    ok: true,
    status: 200,
    statusText: 'OK'
  });

  render(<App />);
  // App renders Chat component, so we just check that it renders without errors
  await waitFor(() => {
    const container = screen.getByRole('main') || document.body;
    expect(container).toBeInTheDocument();
  });
});
