async function request(path, options) {
  const response = await fetch(`/api/v1${path}`, { headers: { 'Content-Type': 'application/json' }, ...options });
  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    throw new Error(error.message || `Request failed (${response.status})`);
  }
  return response.json();
}

export const api = {
  alerts: () => request('/alerts'),
  alert: (id) => request(`/alerts/${id}`),
  accounts: () => request('/accounts'),
  transactions: () => request('/transactions'),
  ingest: (payload) => request('/transactions', { method: 'POST', body: JSON.stringify(payload) }),
  dispose: (id, payload) => request(`/alerts/${id}/status`, { method: 'PATCH', body: JSON.stringify(payload) })
};
