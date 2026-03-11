<script>
  import { goto } from '$app/navigation';
  import { api } from '$lib/api.js';
  import { authStore } from '$lib/stores/auth.js';

  let email = '';
  let password = '';
  let loading = false;
  let error = null;

  async function handleLogin(e) {
    e.preventDefault();
    loading = true;
    error = null;
    try {
      const res = await api.auth.login(email, password);
      const { user, token } = res.data;
      authStore.login(user, token);
      goto('/');
    } catch (e) {
      error = e.response?.data?.message || 'Invalid email or password';
    } finally {
      loading = false;
    }
  }
</script>

<svelte:head><title>Login - TechDeals</title></svelte:head>

<div class="min-h-[calc(100vh-200px)] flex items-center justify-center px-4 py-12">
  <div class="w-full max-w-md">
    <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
      <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center w-14 h-14 bg-blue-100 rounded-2xl mb-4">
          <svg class="w-7 h-7 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
          </svg>
        </div>
        <h1 class="text-2xl font-bold text-gray-900">Welcome back</h1>
        <p class="text-gray-500 mt-1">Sign in to your account</p>
      </div>

      {#if error}
        <div class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6 text-sm">
          {error}
        </div>
      {/if}

      <form on:submit={handleLogin} class="space-y-5">
        <div>
          <label for="email" class="block text-sm font-semibold text-gray-700 mb-1.5">Email</label>
          <input
            id="email"
            type="email"
            bind:value={email}
            required
            autocomplete="email"
            class="w-full border border-gray-300 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            placeholder="you@example.com"
          />
        </div>
        <div>
          <label for="password" class="block text-sm font-semibold text-gray-700 mb-1.5">Password</label>
          <input
            id="password"
            type="password"
            bind:value={password}
            required
            autocomplete="current-password"
            class="w-full border border-gray-300 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            placeholder="••••••••"
          />
        </div>
        <button
          type="submit"
          disabled={loading}
          class="w-full bg-blue-600 hover:bg-blue-700 disabled:opacity-60 text-white font-semibold py-3 px-4 rounded-xl transition-colors"
        >
          {loading ? 'Signing in...' : 'Sign In'}
        </button>
      </form>

      <p class="text-center text-sm text-gray-500 mt-6">
        Don't have an account?
        <a href="/register" class="text-blue-600 hover:underline font-semibold">Create one</a>
      </p>
    </div>
  </div>
</div>
