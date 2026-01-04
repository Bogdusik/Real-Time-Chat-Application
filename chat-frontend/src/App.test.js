import { render, screen } from '@testing-library/react';
import App from './App';

test('renders chat application', () => {
  render(<App />);
  // App renders Chat component, so we just check that it renders without errors
  const container = screen.getByRole('main') || document.body;
  expect(container).toBeInTheDocument();
});
