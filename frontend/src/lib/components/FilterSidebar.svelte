<script>
  import { createEventDispatcher } from 'svelte';

  export let filters = {};
  const dispatch = createEventDispatcher();

  const categories = ['Laptops', 'CPUs', 'GPUs', 'Monitors', 'Storage', 'Peripherals', 'Pre-built PCs', 'Other'];
  const retailers = ['Amazon', 'Best Buy', 'Newegg', 'B&H', 'Micro Center', 'Other'];
  const sortOptions = [
    { value: 'hot_score', label: 'Hot Score' },
    { value: 'newest', label: 'Newest' },
    { value: 'price_asc', label: 'Price (Low to High)' },
    { value: 'price_desc', label: 'Price (High to Low)' },
    { value: 'most_votes', label: 'Most Votes' }
  ];

  let local = {
    category: [],
    minPrice: '',
    maxPrice: '',
    retailer: [],
    cpuBrand: [],
    gpuBrand: [],
    minBenchmark: '',
    sortBy: 'hot_score',
    ...filters
  };

  function toggleArray(arr, val) {
    return arr.includes(val) ? arr.filter(v => v !== val) : [...arr, val];
  }

  function applyFilters() {
    dispatch('filter-change', { ...local });
  }

  function resetFilters() {
    local = { category: [], minPrice: '', maxPrice: '', retailer: [], cpuBrand: [], gpuBrand: [], minBenchmark: '', sortBy: 'hot_score' };
    dispatch('filter-change', { ...local });
  }
</script>

<div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5 space-y-6">
  <h3 class="font-bold text-gray-900 text-lg">Filters</h3>

  <!-- Sort -->
  <div>
    <label class="block text-sm font-semibold text-gray-700 mb-2">Sort By</label>
    <select bind:value={local.sortBy} class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
      {#each sortOptions as opt}
        <option value={opt.value}>{opt.label}</option>
      {/each}
    </select>
  </div>

  <!-- Category -->
  <div>
    <label class="block text-sm font-semibold text-gray-700 mb-2">Category</label>
    <div class="space-y-1.5">
      {#each categories as cat}
        <label class="flex items-center gap-2 cursor-pointer">
          <input
            type="checkbox"
            checked={local.category.includes(cat)}
            on:change={() => local.category = toggleArray(local.category, cat)}
            class="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
          />
          <span class="text-sm text-gray-700">{cat}</span>
        </label>
      {/each}
    </div>
  </div>

  <!-- Price Range -->
  <div>
    <label class="block text-sm font-semibold text-gray-700 mb-2">Price Range</label>
    <div class="flex gap-2">
      <input
        type="number"
        bind:value={local.minPrice}
        placeholder="Min"
        min="0"
        class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
      />
      <input
        type="number"
        bind:value={local.maxPrice}
        placeholder="Max"
        min="0"
        class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
      />
    </div>
  </div>

  <!-- Retailer -->
  <div>
    <label class="block text-sm font-semibold text-gray-700 mb-2">Retailer</label>
    <div class="space-y-1.5">
      {#each retailers as ret}
        <label class="flex items-center gap-2 cursor-pointer">
          <input
            type="checkbox"
            checked={local.retailer.includes(ret)}
            on:change={() => local.retailer = toggleArray(local.retailer, ret)}
            class="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
          />
          <span class="text-sm text-gray-700">{ret}</span>
        </label>
      {/each}
    </div>
  </div>

  <!-- CPU Brand -->
  <div>
    <label class="block text-sm font-semibold text-gray-700 mb-2">CPU Brand</label>
    <div class="flex gap-3">
      {#each ['Intel', 'AMD'] as brand}
        <label class="flex items-center gap-2 cursor-pointer">
          <input
            type="checkbox"
            checked={local.cpuBrand.includes(brand)}
            on:change={() => local.cpuBrand = toggleArray(local.cpuBrand, brand)}
            class="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
          />
          <span class="text-sm text-gray-700">{brand}</span>
        </label>
      {/each}
    </div>
  </div>

  <!-- GPU Brand -->
  <div>
    <label class="block text-sm font-semibold text-gray-700 mb-2">GPU Brand</label>
    <div class="flex gap-3 flex-wrap">
      {#each ['NVIDIA', 'AMD', 'Intel'] as brand}
        <label class="flex items-center gap-2 cursor-pointer">
          <input
            type="checkbox"
            checked={local.gpuBrand.includes(brand)}
            on:change={() => local.gpuBrand = toggleArray(local.gpuBrand, brand)}
            class="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
          />
          <span class="text-sm text-gray-700">{brand}</span>
        </label>
      {/each}
    </div>
  </div>

  <!-- Min Benchmark -->
  <div>
    <label class="block text-sm font-semibold text-gray-700 mb-2">
      Min Benchmark Score: {local.minBenchmark || 0}
    </label>
    <input
      type="range"
      bind:value={local.minBenchmark}
      min="0"
      max="30000"
      step="500"
      class="w-full accent-blue-600"
    />
  </div>

  <div class="flex gap-2 pt-2">
    <button
      on:click={applyFilters}
      class="flex-1 bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2 px-4 rounded-lg transition-colors text-sm"
    >
      Apply Filters
    </button>
    <button
      on:click={resetFilters}
      class="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-700 font-semibold py-2 px-4 rounded-lg transition-colors text-sm"
    >
      Reset
    </button>
  </div>
</div>
