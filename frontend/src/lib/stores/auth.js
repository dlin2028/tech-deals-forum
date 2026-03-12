import { writable } from 'svelte/store';

const defaultState = { user: null, token: null };

function createAuthStore() {
  // Load persisted state
  let initial = defaultState;
  if (typeof localStorage !== 'undefined') {
    try {
      const stored = localStorage.getItem('auth');
      if (stored) initial = JSON.parse(stored);
    } catch {}
  }

  const { subscribe, set, update } = writable(initial);

  return {
    subscribe,
    login(user, token) {
      const state = { user, token };
      set(state);
      if (typeof localStorage !== 'undefined') {
        localStorage.setItem('auth', JSON.stringify(state));
        localStorage.setItem('token', token);
      }
    },
    logout() {
      set(defaultState);
      if (typeof localStorage !== 'undefined') {
        localStorage.removeItem('auth');
        localStorage.removeItem('token');
      }
    },
    setUser(user) {
      update(s => {
        const next = { ...s, user };
        if (typeof localStorage !== 'undefined') localStorage.setItem('auth', JSON.stringify(next));
        return next;
      });
    }
  };
}

export const authStore = createAuthStore();
