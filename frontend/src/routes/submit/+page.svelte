<script>
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { api } from '$lib/api.js';
  import { authStore } from '$lib/stores/auth.js';

  let auth = { user: null, token: null };
  authStore.subscribe(v => auth = v);

  let title = '';
  let dealUrl = '';
  let price = '';
  let retailPrice = '';
  let retailer = '';
  let category = '';
  let description = '';
  let imageUrl = '';
  let cpuModel = '';
  let gpuModel = '';
  let specs = [{ key: '', value: '' }];
  let loading = false;
  let error = null;
  let benchmarkPreview = null;
  let searchingBenchmark = false;

  const categories = ['Laptops', 'CPUs', 'GPUs', 'Monitors', 'Storage', 'Peripherals', 'Pre-built PCs', 'Other'];
  const retailers = ['Amazon', 'Best Buy', 'Newegg', 'B&H', 'Micro Center', 'Other'];

  onMount(() => {
    if (!auth.token) goto('/login');
  });

  function addSpec() {
    specs = [...specs, { key: '', value: '' }];
  }

  function removeSpec(i) {
    specs = specs.filter((_, idx) => idx !== i);
  }

  async function lookupBenchmark() {
    const model = cpuModel || gpuModel;
    if (!model) return;
    searchingBenchmark = true;
    try {
      const res = await api.components.search(model);
      const data = res.data;
      const components = Array.isArray(data) ? data : (data.components || data.data || []);
      if (components.length > 0) benchmarkPreview = components[0];
    } catch {}
    searchingBenchmark = false;
  }

  async function handleSubmit(e) {
    e.preventDefault();
    if (!auth.token) { goto('/login'); return; }
    loading = true;
    error = null;

    const techSpecs = {};
    specs.filter(s => s.key && s.value).forEach(s => { techSpecs[s.key] = s.value; });

    try {
      const payload = {
        title,
        deal_url: dealUrl,
        price: parseFloat(price),
        retail_price: retailPrice ? parseFloat(retailPrice) : undefined,
        retailer,
        category,
        description,
        image_url: imageUrl || undefined,
        specs: techSpecs,
        cpu_model: cpuModel || undefined,
        gpu_model: gpuModel || undefined
      };
      const res = await api.deals.create(payload);
      const deal = res.data;
      goto(`/deals/${deal.id || deal.deal?.id}`);
    } catch (e) {
      error = e.response?.data?.message || 'Failed to submit deal';
    } finally {
      loading = false;
    }
  }
</script>

<svelte:head><title>Submit a Deal - TechDeals</title></svelte:head>

<div class="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
  <div class="mb-8">
    <h1 class="text-3xl font-bold text-gray-900">Submit a Deal</h1>
    <p class="text-gray-500 mt-1">Share a great tech deal with the community</p>
  </div>

  {#if error}
    <div class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6">{error}</div>
  {/if}

  <form on:submit={handleSubmit} class="space-y-6">
    <!-- Basic Info -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6 space-y-5">
      <h2 class="font-bold text-gray-900 text-lg border-b border-gray-100 pb-3">Deal Information</h2>

      <div>
        <label class="block text-sm font-semibold text-gray-700 mb-1.5">Title *</label>
        <input type="text" bind:value={title} required
          class="w-full border border-gray-300 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          placeholder="e.g. ASUS ROG Zephyrus G14 2024 - RTX 4070, Ryzen 9" />
      </div>

      <div>
        <label class="block text-sm font-semibold text-gray-700 mb-1.5">Deal URL *</label>
        <input type="url" bind:value={dealUrl} required
          class="w-full border border-gray-300 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          placeholder="https://www.amazon.com/..." />
      </div>

      <div class="grid grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-semibold text-gray-700 mb-1.5">Deal Price ($) *</label>
          <input type="number" bind:value={price} required min="0" step="0.01"
            class="w-full border border-gray-300 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="999.99" />
        </div>
        <div>
          <label class="block text-sm font-semibold text-gray-700 mb-1.5">Retail Price ($)</label>
          <input type="number" bind:value={retailPrice} min="0" step="0.01"
            class="w-full border border-gray-300 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="1299.99" />
        </div>
      </div>

      <div class="grid grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-semibold text-gray-700 mb-1.5">Retailer *</label>
          <select bind:value={retailer} required
            class="w-full border border-gray-300 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
            <option value="">Select retailer</option>
            {#each retailers as r}<option value={r}>{r}</option>{/each}
          </select>
        </div>
        <div>
          <label class="block text-sm font-semibold text-gray-700 mb-1.5">Category *</label>
          <select bind:value={category} required
            class="w-full border border-gray-300 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
            <option value="">Select category</option>
            {#each categories as c}<option value={c}>{c}</option>{/each}
          </select>
        </div>
      </div>

      <div>
        <label class="block text-sm font-semibold text-gray-700 mb-1.5">Description</label>
        <textarea bind:value={description} rows="4"
          class="w-full border border-gray-300 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
          placeholder="Describe the deal, why it's great, what's included..."></textarea>
      </div>

      <div>
        <label class="block text-sm font-semibold text-gray-700 mb-1.5">Image URL</label>
        <input type="url" bind:value={imageUrl}
          class="w-full border border-gray-300 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          placeholder="https://..." />
      </div>
    </div>

    <!-- Tech Specs -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6 space-y-4">
      <div class="flex items-center justify-between border-b border-gray-100 pb-3">
        <h2 class="font-bold text-gray-900 text-lg">Tech Specs</h2>
        <button type="button" on:click={addSpec}
          class="text-sm text-blue-600 hover:text-blue-800 font-medium flex items-center gap-1">
          + Add Spec
        </button>
      </div>

      {#each specs as spec, i}
        <div class="flex gap-3 items-center">
          <input type="text" bind:value={spec.key}
            class="flex-1 border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="Spec name (e.g. RAM)" />
          <input type="text" bind:value={spec.value}
            class="flex-1 border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="Value (e.g. 16GB)" />
          {#if specs.length > 1}
            <button type="button" on:click={() => removeSpec(i)}
              class="text-red-400 hover:text-red-600 p-1 rounded-lg hover:bg-red-50 transition-colors shrink-0">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          {/if}
        </div>
      {/each}
    </div>

    <!-- Benchmark Lookup -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6 space-y-4">
      <h2 class="font-bold text-gray-900 text-lg border-b border-gray-100 pb-3">Benchmark Lookup (Optional)</h2>
      <div class="grid grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-semibold text-gray-700 mb-1.5">CPU Model</label>
          <input type="text" bind:value={cpuModel}
            class="w-full border border-gray-300 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="e.g. Ryzen 9 7940HS" />
        </div>
        <div>
          <label class="block text-sm font-semibold text-gray-700 mb-1.5">GPU Model</label>
          <input type="text" bind:value={gpuModel}
            class="w-full border border-gray-300 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="e.g. RTX 4070" />
        </div>
      </div>
      <button type="button" on:click={lookupBenchmark} disabled={searchingBenchmark || (!cpuModel && !gpuModel)}
        class="text-sm bg-gray-100 hover:bg-gray-200 disabled:opacity-50 text-gray-700 font-medium px-4 py-2 rounded-lg transition-colors">
        {searchingBenchmark ? 'Looking up...' : '🔍 Look Up Benchmarks'}
      </button>

      {#if benchmarkPreview}
        <div class="bg-blue-50 border border-blue-200 rounded-lg p-4 text-sm">
          <p class="font-semibold text-blue-900">Found: {benchmarkPreview.model || benchmarkPreview.name}</p>
          {#if benchmarkPreview.benchmark_score}
            <p class="text-blue-700 mt-1">Benchmark Score: <strong>{benchmarkPreview.benchmark_score.toLocaleString()}</strong></p>
          {/if}
        </div>
      {/if}
    </div>

    <button type="submit" disabled={loading}
      class="w-full bg-blue-600 hover:bg-blue-700 disabled:opacity-60 text-white font-bold py-4 px-6 rounded-xl transition-colors text-lg">
      {loading ? 'Submitting...' : '🚀 Submit Deal'}
    </button>
  </form>
</div>
