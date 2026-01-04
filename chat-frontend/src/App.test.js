import { render, screen, waitFor } from '@testing-library/react';
import App from './App';

// All mocks are in setupTests.js

test('renders chat application', async () => {
  render(<App />);
  // App renders Chat component, so we just check that it renders without errors
  await waitFor(() => {
    const container = screen.getByRole('main') || document.body;
    expect(container).toBeInTheDocument();
  });
});
