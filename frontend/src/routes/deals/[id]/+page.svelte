<script>
  import { onMount } from 'svelte';
  import { page } from '$app/stores';
  import { api } from '$lib/api.js';
  import { authStore } from '$lib/stores/auth.js';
  import VoteButtons from '$lib/components/VoteButtons.svelte';
  import BenchmarkBadge from '$lib/components/BenchmarkBadge.svelte';
  import PriceChart from '$lib/components/PriceChart.svelte';

  let deal = null;
  let priceHistory = [];
  let loading = true;
  let error = null;
  let auth = { user: null, token: null };
  authStore.subscribe(v => auth = v);

  $: dealId = $page.params.id;

  onMount(async () => {
    try {
      const [dealRes, histRes] = await Promise.allSettled([
        api.deals.get(dealId),
        api.deals.priceHistory(dealId)
      ]);
      if (dealRes.status === 'fulfilled') deal = dealRes.value.data;
      if (histRes.status === 'fulfilled') {
        const h = histRes.value.data;
        priceHistory = Array.isArray(h) ? h : (h.history || h.data || []);
      }
    } catch (e) {
      error = 'Failed to load deal';
    } finally {
      loading = false;
    }
  });

  function formatDate(dateStr) {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' });
  }

  $: specs = deal?.specs || deal?.tech_specs || {};
  $: specsEntries = Object.entries(specs);
</script>

<svelte:head>
  <title>{deal?.title || 'Deal'} - TechDeals</title>
</svelte:head>

<div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
  {#if loading}
    <div class="animate-pulse space-y-4">
      <div class="h-8 bg-gray-200 rounded w-3/4"></div>
      <div class="h-64 bg-gray-200 rounded"></div>
      <div class="h-4 bg-gray-200 rounded w-1/2"></div>
    </div>
  {:else if error || !deal}
    <div class="text-center py-16">
      <p class="text-red-600 text-lg">{error || 'Deal not found'}</p>
      <a href="/" class="mt-4 inline-block text-blue-600 hover:underline">← Back to deals</a>
    </div>
  {:else}
    <!-- Breadcrumb -->
    <nav class="mb-6 text-sm text-gray-500">
      <a href="/" class="hover:text-blue-600">Home</a>
      <span class="mx-2">/</span>
      <a href="/search?category={deal.category}" class="hover:text-blue-600">{deal.category}</a>
      <span class="mx-2">/</span>
      <span class="text-gray-900 truncate">{deal.title}</span>
    </nav>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <!-- Main -->
      <div class="lg:col-span-2 space-y-6">
        <!-- Image + Title -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
          {#if deal.image_url}
            <div class="h-64 bg-gray-50 flex items-center justify-center p-4">
              <img src={deal.image_url} alt={deal.title} class="max-h-full max-w-full object-contain" />
            </div>
          {/if}
          <div class="p-6">
            <div class="flex items-center gap-2 mb-3 flex-wrap">
              <span class="bg-blue-100 text-blue-800 text-xs font-semibold px-2.5 py-1 rounded-full">{deal.category}</span>
              {#if deal.retailer}
                <span class="text-sm text-gray-500">@ {deal.retailer}</span>
              {/if}
            </div>
            <h1 class="text-2xl font-bold text-gray-900 mb-4">{deal.title}</h1>

            <!-- Price -->
            <div class="flex items-baseline gap-3 mb-4">
              <span class="text-4xl font-extrabold text-blue-600">${deal.price}</span>
              {#if deal.retail_price && deal.retail_price > deal.price}
                <span class="text-xl text-gray-400 line-through">${deal.retail_price}</span>
                <span class="bg-red-100 text-red-700 text-sm font-bold px-2 py-0.5 rounded-full">
                  -{Math.round((1 - deal.price / deal.retail_price) * 100)}% OFF
                </span>
              {/if}
            </div>

            <!-- Votes + CTA -->
            <div class="flex items-center gap-4 flex-wrap">
              <VoteButtons
                dealId={deal.id}
                upvotes={deal.upvotes || 0}
                downvotes={deal.downvotes || 0}
                userVote={deal.user_vote}
              />
              <a
                href={deal.deal_url}
                target="_blank"
                rel="noopener noreferrer"
                class="flex-1 sm:flex-none inline-flex items-center justify-center gap-2 bg-blue-600 hover:bg-blue-700 text-white font-bold px-6 py-3 rounded-xl transition-colors"
              >
                Go to Deal
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"/>
                </svg>
              </a>
            </div>
          </div>
        </div>

        <!-- Description -->
        {#if deal.description}
          <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
            <h2 class="text-lg font-bold text-gray-900 mb-3">Description</h2>
            <p class="text-gray-700 leading-relaxed">{deal.description}</p>
          </div>
        {/if}

        <!-- Tech Specs -->
        {#if specsEntries.length > 0}
          <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
            <h2 class="text-lg font-bold text-gray-900 mb-4">Tech Specs</h2>
            <div class="overflow-x-auto">
              <table class="w-full text-sm">
                <tbody>
                  {#each specsEntries as [key, val]}
                    <tr class="border-b border-gray-100 last:border-0">
                      <td class="py-2.5 pr-4 font-semibold text-gray-600 whitespace-nowrap w-1/3">{key}</td>
                      <td class="py-2.5 text-gray-900">{val}</td>
                    </tr>
                  {/each}
                </tbody>
              </table>
            </div>
          </div>
        {/if}

        <!-- Price History -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
          <h2 class="text-lg font-bold text-gray-900 mb-4">Price History</h2>
          <PriceChart {priceHistory} />
        </div>
      </div>

      <!-- Sidebar -->
      <div class="space-y-6">
        <!-- Benchmark Info -->
        {#if deal.component}
          <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
            <h2 class="text-lg font-bold text-gray-900 mb-4">Benchmark Info</h2>
            <div class="space-y-4">
              {#if deal.component.benchmark_score}
                <div>
                  <div class="flex justify-between items-center mb-1">
                    <span class="text-sm font-medium text-gray-700">
                      {deal.component.component_type === 'cpu' ? 'CPU' : 'GPU'} Score
                    </span>
                    <BenchmarkBadge score={deal.component.benchmark_score} type={deal.component.component_type?.toUpperCase()} />
                  </div>
                  <div class="w-full bg-gray-100 rounded-full h-3 mt-2">
                    <div
                      class="h-3 rounded-full transition-all duration-500
                        {deal.component.benchmark_score >= 15000 ? 'bg-green-500' : deal.component.benchmark_score >= 8000 ? 'bg-yellow-500' : 'bg-red-500'}"
                      style="width: {Math.min(100, (deal.component.benchmark_score / 30000) * 100)}%"
                    ></div>
                  </div>
                  <p class="text-xs text-gray-500 mt-1">{deal.component.benchmark_score.toLocaleString()} / 30,000 (PassMark scale)</p>
                </div>
              {/if}
              {#if deal.component.passmark_score}
                <div class="flex justify-between text-sm">
                  <span class="text-gray-600">PassMark</span>
                  <span class="font-semibold">{deal.component.passmark_score?.toLocaleString()}</span>
                </div>
              {/if}
              {#if deal.component.geekbench_single}
                <div class="flex justify-between text-sm">
                  <span class="text-gray-600">Geekbench Single</span>
                  <span class="font-semibold">{deal.component.geekbench_single?.toLocaleString()}</span>
                </div>
              {/if}
              {#if deal.component.geekbench_multi}
                <div class="flex justify-between text-sm">
                  <span class="text-gray-600">Geekbench Multi</span>
                  <span class="font-semibold">{deal.component.geekbench_multi?.toLocaleString()}</span>
                </div>
              {/if}
              {#if deal.component.model}
                <div class="text-xs text-gray-500 pt-2 border-t border-gray-100">
                  Model: {deal.component.model}
                </div>
              {/if}
            </div>
          </div>
        {/if}

        <!-- Deal Meta -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6 space-y-3">
          <h2 class="text-lg font-bold text-gray-900 mb-3">Deal Info</h2>
          {#if deal.posted_by || deal.user}
            <div class="flex justify-between text-sm">
              <span class="text-gray-600">Posted by</span>
              <span class="font-medium text-gray-900">{deal.posted_by || deal.user?.username || 'Anonymous'}</span>
            </div>
          {/if}
          {#if deal.created_at}
            <div class="flex justify-between text-sm">
              <span class="text-gray-600">Posted</span>
              <span class="font-medium text-gray-900">{formatDate(deal.created_at)}</span>
            </div>
          {/if}
          {#if deal.hot_score !== undefined}
            <div class="flex justify-between text-sm">
              <span class="text-gray-600">Hot Score</span>
              <span class="font-semibold text-orange-600">{deal.hot_score?.toFixed(1)}</span>
            </div>
          {/if}
          <div class="pt-3 border-t border-gray-100">
            <button class="w-full text-sm text-red-500 hover:text-red-700 hover:bg-red-50 py-2 px-3 rounded-lg transition-colors">
              🚩 Report Deal
            </button>
          </div>
        </div>
      </div>
    </div>
  {/if}
</div>
