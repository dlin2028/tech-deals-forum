<script>
  import { createEventDispatcher } from 'svelte';
  import VoteButtons from './VoteButtons.svelte';
  import BenchmarkBadge from './BenchmarkBadge.svelte';

  export let deal;
  const dispatch = createEventDispatcher();

  const categoryColors = {
    laptops: 'bg-blue-100 text-blue-800',
    cpus: 'bg-purple-100 text-purple-800',
    gpus: 'bg-green-100 text-green-800',
    monitors: 'bg-yellow-100 text-yellow-800',
    storage: 'bg-orange-100 text-orange-800',
    peripherals: 'bg-pink-100 text-pink-800',
    'pre-built-pcs': 'bg-indigo-100 text-indigo-800',
    other: 'bg-gray-100 text-gray-800'
  };

  $: catColor = categoryColors[deal.category?.toLowerCase().replace(' ', '-')] || 'bg-gray-100 text-gray-800';
  $: discount = deal.retail_price && deal.price
    ? Math.round((1 - deal.price / deal.retail_price) * 100)
    : null;

  function handleVote(e) {
    dispatch('vote', { dealId: deal.id, ...e.detail });
  }
</script>

<div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-md transition-all duration-200 flex flex-col">
  <a href="/deals/{deal.id}" class="block relative overflow-hidden group">
    <div class="h-48 bg-gradient-to-br from-gray-100 to-gray-200 flex items-center justify-center overflow-hidden">
      {#if deal.image_url}
        <img src={deal.image_url} alt={deal.title} class="w-full h-full object-contain p-2 group-hover:scale-105 transition-transform duration-300" />
      {:else}
        <svg class="w-16 h-16 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
        </svg>
      {/if}
    </div>
    {#if discount && discount > 0}
      <div class="absolute top-2 right-2 bg-red-500 text-white text-xs font-bold px-2 py-1 rounded-full">
        -{discount}%
      </div>
    {/if}
  </a>

  <div class="p-4 flex flex-col flex-1">
    <div class="flex items-start justify-between gap-2 mb-2">
      <span class="text-xs font-medium px-2 py-0.5 rounded-full {catColor}">
        {deal.category || 'Other'}
      </span>
      {#if deal.retailer}
        <span class="text-xs text-gray-500 truncate">{deal.retailer}</span>
      {/if}
    </div>

    <a href="/deals/{deal.id}" class="font-semibold text-gray-900 hover:text-blue-600 transition-colors mb-2 line-clamp-2 flex-1">
      {deal.title}
    </a>

    <div class="flex items-baseline gap-2 mb-3">
      <span class="text-2xl font-bold text-blue-600">${deal.price}</span>
      {#if deal.retail_price && deal.retail_price > deal.price}
        <span class="text-sm text-gray-400 line-through">${deal.retail_price}</span>
      {/if}
    </div>

    {#if deal.component}
      <div class="flex flex-wrap gap-1 mb-3">
        {#if deal.component.benchmark_score}
          <BenchmarkBadge score={deal.component.benchmark_score} type={deal.component.component_type === 'cpu' ? 'CPU' : 'GPU'} />
        {/if}
      </div>
    {/if}

    <div class="flex items-center justify-between mt-auto pt-3 border-t border-gray-100">
      <VoteButtons
        dealId={deal.id}
        upvotes={deal.upvotes || 0}
        downvotes={deal.downvotes || 0}
        userVote={deal.user_vote}
        on:vote={handleVote}
      />
      <a
        href={deal.deal_url}
        target="_blank"
        rel="noopener noreferrer"
        class="text-xs font-semibold bg-blue-600 hover:bg-blue-700 text-white px-3 py-1.5 rounded-lg transition-colors"
      >
        View Deal →
      </a>
    </div>
  </div>
</div>
