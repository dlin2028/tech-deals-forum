<script>
  import '../app.css';
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { api } from '$lib/api.js';
  import SearchBar from '$lib/components/SearchBar.svelte';
  import { page } from '$app/stores';

  let auth = { user: null, token: null };
  authStore.subscribe(v => auth = v);

  let mobileMenuOpen = false;
  let searchValue = '';

  onMount(async () => {
    if (auth.token) {
      try {
        const res = await api.auth.me();
        authStore.setUser(res.data.user || res.data);
      } catch {
        authStore.logout();
      }
    }
  });

  function logout() {
    authStore.logout();
    window.location.href = '/';
  }

  $: searchValue = $page.url.searchParams.get('q') || '';
</script>

<div class="min-h-screen bg-gray-50 flex flex-col">
  <!-- Navbar -->
  <header class="bg-white border-b border-gray-200 sticky top-0 z-50 shadow-sm">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16 gap-4">
        <!-- Logo -->
        <a href="/" class="flex items-center gap-2 font-bold text-xl text-blue-600 whitespace-nowrap shrink-0">
          <svg class="w-7 h-7" fill="currentColor" viewBox="0 0 24 24">
            <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
          </svg>
          TechDeals
        </a>

        <!-- Search Bar (center) -->
        <div class="flex-1 hidden md:flex justify-center">
          <SearchBar bind:value={searchValue} />
        </div>

        <!-- Nav Links -->
        <nav class="hidden md:flex items-center gap-1">
          <a href="/?tab=hot" class="px-3 py-2 text-sm font-medium text-gray-700 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors">🔥 Hot Deals</a>
          <a href="/?tab=feed" class="px-3 py-2 text-sm font-medium text-gray-700 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors">📰 My Feed</a>
          <a href="/submit" class="px-3 py-2 text-sm font-medium text-gray-700 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors">➕ Submit Deal</a>
        </nav>

        <!-- Auth -->
        <div class="flex items-center gap-2 shrink-0">
          {#if auth.user}
            <a href="/profile" class="flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-gray-100 transition-colors">
              <div class="w-8 h-8 bg-blue-600 rounded-full flex items-center justify-center text-white text-sm font-bold">
                {auth.user.username?.[0]?.toUpperCase() || 'U'}
              </div>
              <span class="text-sm font-medium text-gray-700 hidden lg:block">{auth.user.username}</span>
            </a>
            <button
              on:click={logout}
              class="text-sm text-gray-500 hover:text-gray-700 px-2 py-1 rounded-lg hover:bg-gray-100 transition-colors"
            >
              Logout
            </button>
          {:else}
            <a href="/login" class="text-sm font-medium text-gray-700 px-3 py-2 rounded-lg hover:bg-gray-100 transition-colors">Login</a>
            <a href="/register" class="text-sm font-medium bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg transition-colors">Register</a>
          {/if}
          <!-- Mobile menu button -->
          <button on:click={() => mobileMenuOpen = !mobileMenuOpen} class="md:hidden p-2 rounded-lg hover:bg-gray-100">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d={mobileMenuOpen ? "M6 18L18 6M6 6l12 12" : "M4 6h16M4 12h16M4 18h16"}/>
            </svg>
          </button>
        </div>
      </div>

      <!-- Mobile Search -->
      <div class="md:hidden pb-3">
        <SearchBar bind:value={searchValue} />
      </div>
    </div>

    <!-- Mobile Menu -->
    {#if mobileMenuOpen}
      <div class="md:hidden border-t border-gray-100 bg-white px-4 py-3 space-y-1">
        <a href="/?tab=hot" class="block px-3 py-2 text-sm font-medium text-gray-700 hover:bg-blue-50 rounded-lg">🔥 Hot Deals</a>
        <a href="/?tab=feed" class="block px-3 py-2 text-sm font-medium text-gray-700 hover:bg-blue-50 rounded-lg">📰 My Feed</a>
        <a href="/submit" class="block px-3 py-2 text-sm font-medium text-gray-700 hover:bg-blue-50 rounded-lg">➕ Submit Deal</a>
      </div>
    {/if}
  </header>

  <!-- Main Content -->
  <main class="flex-1">
    <slot />
  </main>

  <!-- Footer -->
  <footer class="bg-white border-t border-gray-200 mt-auto">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="flex flex-col md:flex-row justify-between items-center gap-4">
        <div class="flex items-center gap-2 font-bold text-lg text-blue-600">
          <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
            <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
          </svg>
          TechDeals
        </div>
        <p class="text-sm text-gray-500">The best tech deals, ranked by the community.</p>
        <div class="flex gap-4 text-sm text-gray-500">
          <a href="/" class="hover:text-gray-700">Home</a>
          <a href="/search" class="hover:text-gray-700">Browse</a>
          <a href="/submit" class="hover:text-gray-700">Submit</a>
        </div>
      </div>
      <p class="text-center text-xs text-gray-400 mt-4">© {new Date().getFullYear()} TechDeals. Community-powered tech deals.</p>
    </div>
  </footer>
</div>
