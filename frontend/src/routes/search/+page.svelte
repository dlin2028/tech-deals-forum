<script>
  import { onMount } from 'svelte';
  import { page } from '$app/stores';
  import { goto } from '$app/navigation';
  import { api } from '$lib/api.js';
  import DealCard from '$lib/components/DealCard.svelte';
  import DealSkeleton from '$lib/components/DealSkeleton.svelte';
  import FilterSidebar from '$lib/components/FilterSidebar.svelte';

  let deals = [];
  let loading = true;
  let error = null;
  let total = 0;
  let sidebarOpen = false;

  $: searchQuery = $page.url.searchParams.get('q') || '';
  $: filters = {
    q: searchQuery,
    category: $page.url.searchParams.getAll('category'),
    minPrice: $page.url.searchParams.get('minPrice') || '',
    maxPrice: $page.url.searchParams.get('maxPrice') || '',
    retailer: $page.url.searchParams.getAll('retailer'),
    sortBy: $page.url.searchParams.get('sortBy') || 'hot_score'
  };

  async function search(f) {
    loading = true;
    error = null;
    try {
      const params = {};
      if (f.q) params.q = f.q;
      if (f.category?.length) params.category = f.category.join(',');
      if (f.minPrice) params.min_price = f.minPrice;
      if (f.maxPrice) params.max_price = f.maxPrice;
      if (f.retailer?.length) params.retailer = f.retailer.join(',');
      if (f.sortBy) params.sort = f.sortBy;

      const res = await api.deals.search(params);
      const data = res.data;
      deals = Array.isArray(data) ? data : (data.deals || data.data || []);
      total = data.total || deals.length;
    } catch (e) {
      error = e.response?.data?.message || 'Search failed';
      deals = [];
    } finally {
      loading = false;
    }
  }

  function handleFilterChange(e) {
    const f = e.detail;
    const params = new URLSearchParams();
    if (f.q) params.set('q', f.q);
    f.category?.forEach(c => params.append('category', c));
    if (f.minPrice) params.set('minPrice', f.minPrice);
    if (f.maxPrice) params.set('maxPrice', f.maxPrice);
    f.retailer?.forEach(r => params.append('retailer', r));
    if (f.sortBy) params.set('sortBy', f.sortBy);
    goto(`/search?${params.toString()}`, { replaceState: true });
    search(f);
  }

  onMount(() => search(filters));
</script>

<svelte:head>
  <title>{searchQuery ? `Search: ${searchQuery}` : 'Browse Deals'} - TechDeals</title>
</svelte:head>

<div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
  <!-- Header -->
  <div class="flex items-center justify-between mb-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">
        {searchQuery ? `Results for "${searchQuery}"` : 'Browse All Deals'}
      </h1>
      {#if !loading}
        <p class="text-sm text-gray-500 mt-1">{total} deal{total !== 1 ? 's' : ''} found</p>
      {/if}
    </div>
    <button
      on:click={() => sidebarOpen = !sidebarOpen}
      class="lg:hidden flex items-center gap-2 bg-white border border-gray-200 rounded-lg px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
    >
      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2a1 1 0 01-.293.707L13 13.414V19a1 1 0 01-.553.894l-4 2A1 1 0 017 21v-7.586L3.293 6.707A1 1 0 013 6V4z"/>
      </svg>
      Filters
    </button>
  </div>

  <div class="flex gap-6">
    <!-- Sidebar -->
    <aside class="{sidebarOpen ? 'block' : 'hidden'} lg:block w-full lg:w-72 shrink-0">
      <FilterSidebar {filters} on:filter-change={handleFilterChange} />
    </aside>

    <!-- Results -->
    <div class="flex-1 min-w-0">
      {#if error}
        <div class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6">{error}</div>
      {/if}

      {#if loading}
        <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-6">
          {#each Array(6) as _}
            <DealSkeleton />
          {/each}
        </div>
      {:else if deals.length === 0}
        <div class="text-center py-16 bg-white rounded-xl border border-gray-100">
          <svg class="w-16 h-16 mx-auto mb-4 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
          </svg>
          <p class="text-lg font-medium text-gray-700">No deals found</p>
          <p class="text-gray-500 mt-1">Try adjusting your search or filters</p>
        </div>
      {:else}
        <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-6">
          {#each deals as deal (deal.id)}
            <DealCard {deal} />
          {/each}
        </div>
      {/if}
    </div>
  </div>
</div>
