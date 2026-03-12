<script>
  import { onMount } from 'svelte';
  import { page } from '$app/stores';
  import { goto } from '$app/navigation';
  import { api } from '$lib/api.js';
  import { authStore } from '$lib/stores/auth.js';
  import DealCard from '$lib/components/DealCard.svelte';
  import DealSkeleton from '$lib/components/DealSkeleton.svelte';

  let deals = [];
  let loading = true;
  let error = null;
  let hasMore = false;
  let currentPage = 1;
  let loadingMore = false;
  let auth = { user: null, token: null };
  authStore.subscribe(v => auth = v);

  $: activeTab = $page.url.searchParams.get('tab') || 'hot';

  async function loadDeals(tab = 'hot', pageNum = 1, append = false) {
    if (pageNum === 1) loading = true;
    error = null;
    try {
      let res;
      if (tab === 'feed' && auth.token) {
        res = await api.deals.feed();
      } else if (tab === 'new') {
        res = await api.deals.list({ sort: 'newest', page: pageNum, limit: 20 });
      } else {
        res = await api.deals.hot();
      }
      const data = res.data;
      const newDeals = Array.isArray(data) ? data : (data.deals || data.data || []);
      if (append) {
        deals = [...deals, ...newDeals];
      } else {
        deals = newDeals;
      }
      hasMore = newDeals.length === 20;
    } catch (e) {
      error = e.response?.data?.message || 'Failed to load deals';
    } finally {
      loading = false;
      loadingMore = false;
    }
  }

  async function loadMore() {
    loadingMore = true;
    currentPage++;
    await loadDeals(activeTab, currentPage, true);
  }

  function setTab(tab) {
    if (tab === 'feed' && !auth.token) {
      goto('/login');
      return;
    }
    currentPage = 1;
    goto(`/?tab=${tab}`, { replaceState: true });
  }

  onMount(() => loadDeals(activeTab));

  $: if (typeof window !== 'undefined') {
    currentPage = 1;
    loadDeals(activeTab);
  }
</script>

<svelte:head>
  <title>TechDeals - Best Tech Deals Ranked by the Community</title>
</svelte:head>

<!-- Hero -->
<section class="bg-gradient-to-r from-blue-700 to-blue-500 text-white py-12 px-4">
  <div class="max-w-4xl mx-auto text-center">
    <h1 class="text-3xl md:text-5xl font-extrabold mb-4 leading-tight">
      The Best Tech Deals,<br/>
      <span class="text-blue-200">Ranked by the Community</span>
    </h1>
    <p class="text-blue-100 text-lg mb-6">Find CPUs, GPUs, Laptops, and more — with benchmark scores and price history.</p>
    <a href="/submit" class="inline-block bg-white text-blue-700 font-bold px-8 py-3 rounded-full hover:bg-blue-50 transition-colors shadow-lg">
      Submit a Deal
    </a>
  </div>
</section>

<!-- Content -->
<div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
  <!-- Tab Switcher -->
  <div class="flex gap-1 bg-gray-100 p-1 rounded-xl w-fit mb-8">
    {#each [['hot', '🔥 Hot Deals'], ['new', '🆕 New Deals'], ['feed', '📰 My Feed']] as [tab, label]}
      <button
        on:click={() => setTab(tab)}
        class="px-5 py-2.5 rounded-lg text-sm font-semibold transition-all
          {activeTab === tab ? 'bg-white shadow-sm text-blue-600' : 'text-gray-600 hover:text-gray-900'}"
      >
        {label}
      </button>
    {/each}
  </div>

  <!-- Error -->
  {#if error}
    <div class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6">
      {error}
    </div>
  {/if}

  <!-- Deals Grid -->
  {#if loading}
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
      {#each Array(8) as _}
        <DealSkeleton />
      {/each}
    </div>
  {:else if deals.length === 0}
    <div class="text-center py-16 text-gray-500">
      <svg class="w-16 h-16 mx-auto mb-4 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/>
      </svg>
      <p class="text-lg font-medium">No deals found</p>
      <p class="mt-1">Be the first to <a href="/submit" class="text-blue-600 hover:underline">submit a deal</a>!</p>
    </div>
  {:else}
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
      {#each deals as deal (deal.id)}
        <DealCard {deal} />
      {/each}
    </div>

    {#if hasMore}
      <div class="text-center mt-8">
        <button
          on:click={loadMore}
          disabled={loadingMore}
          class="bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white font-semibold px-8 py-3 rounded-full transition-colors"
        >
          {loadingMore ? 'Loading...' : 'Load More Deals'}
        </button>
      </div>
    {/if}
  {/if}
</div>
